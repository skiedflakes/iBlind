package skiedflakes.iBlind;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.ahmedabdelmeged.bluetoothmc.BluetoothMC;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

import static skiedflakes.iBlind.Utils.get_lat;
import static skiedflakes.iBlind.Utils.get_lon;
import static skiedflakes.iBlind.Utils.requestingLocationUpdates;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class LocationUpdatesService extends Service {
    BluetoothMC bluetoothMC;
    private static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationupdatesforegroundservice";

    private static final String TAG = LocationUpdatesService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */

    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;
    Globals globals;


    public LocationUpdatesService() {
    }

    Boolean get_first = false;
    SessionManager session;
    String user_id;
    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        session = new SessionManager(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        session = new SessionManager(getBaseContext().getApplicationContext());
        HashMap<String, String> user_account = session.getUserDetails();
        user_id = user_account.get(SessionManager.KEY_USER_ID);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }



        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");

            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */


    public void requestLocationUpdates(BluetoothMC bluetooth) {
       try {
           bluetoothMC = bluetooth;

           bluetoothMC.setOnDataReceivedListener(new BluetoothMC.onDataReceivedListener() {
               @Override
               public void onDataReceived(String data) {
                   if(data.equals("1")){
                       sendSMS();
                       startCamera();
                   }else if(data.equals("2")){
                       MediaPlayer mediaPlayer= MediaPlayer.create(getBaseContext(),R.raw.left);
                       mediaPlayer.start();
                   }else if(data.equals("3")){
                       MediaPlayer mediaPlayer= MediaPlayer.create(getBaseContext(),R.raw.right);
                       mediaPlayer.start();
                   }else if(data.equals("4")){
                       MediaPlayer mediaPlayer= MediaPlayer.create(getBaseContext(),R.raw.front);
                       mediaPlayer.start();
                   }

               }
           });

           bluetoothMC.setOnBluetoothConnectionListener(new BluetoothMC.BluetoothConnectionListener() {
               @Override
               public void onDeviceConnecting() {
                   //this method triggered during the connection processes


               }

               @Override
               public void onDeviceConnected() {

               }

               @Override
               public void onDeviceDisconnected() {
                   send_disconnected_SMS();

               }

               @Override
               public void onDeviceConnectionFailed() {

               }
           });

           //set listener to keep track the communication errors
           bluetoothMC.setOnBluetoothErrorsListener(new BluetoothMC.BluetoothErrorsListener() {
               @Override
               public void onSendingFailed() {
                   Log.e("status", "onSendingFailed");
                   //this method triggered if the app failed to send data
                   send_disconnected_SMS();
                   startCamera();
               }

               @Override
               public void onReceivingFailed() {
                   Log.e("status", "onReceivingFailed");
                   //this method triggered if the app failed to receive data
                   send_disconnected_SMS();
                   startCamera();
               }

               @Override
               public void onDisconnectingFailed() {
                   Log.e("status", "onDisconnectingFailed");
                   //this method triggered if the app failed to disconnect to the bluetooth device
               }

               @Override
               public void onCommunicationFailed() {
                   Log.e("status", "onCommunicationFailed");
                   //this method triggered if the app connect and unable to send and receive data
                   //from the bluetooth device

               }
           });

           Log.i(TAG, "Requesting location updates");
           Utils.setRequestingLocationUpdates(this, true);
           startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
           try {
               mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                       mLocationCallback, Looper.myLooper());
           } catch (SecurityException unlikely) {
               Utils.setRequestingLocationUpdates(this, false);
               Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
           }
       }catch (Exception e){
           Toast.makeText(this, "Something went wrong. Please connect device.", Toast.LENGTH_SHORT).show();
       }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = Utils.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.start, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.stop, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.iblind_foreground)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        try{
            if(!get_first){
                save_location(user_id,Utils.convert_loc(location));
            }
            Double dist = calculateDistance(get_lat(mLocation),get_lon(mLocation),
                    get_lat(location),get_lon(location),"K");
            Log.e("test distance / user id",dist +" / "+user_id);
            if(dist>0.030){
                save_location(user_id,Utils.convert_loc(location));
            }else{

            }

           //save_location(user_id,Utils.convert_loc(location));
            mLocation = location;

           // Log.e(TAG, "New distance: " + dist);
            String update_sms = "http://maps.google.com/maps?daddr=" +  get_lat(location) + "," + get_lon(location) + " (" + "Current Loc" + ")";;
            session.update_latest_location(update_sms);
        }catch (Exception e){

        }

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2, String unit)
    {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "M") {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    private double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    private void save_location(final String user_id,final String location) {
        String URL = getString(R.string.URL)+"save_gps_logs.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!get_first){
                    get_first = true;
                }
               Log.e(TAG, "response save: " + response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {

                }catch (Exception e){}
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("user_id", user_id);
                hashMap.put("gps_location", location);
                return hashMap;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
        AppController.getInstance().setVolleyDuration(stringRequest);
    }

    public void sendSMS(){
        try{
            String rec =  session.get_sms_reciever();
            String updated_lcoation =  session.get_latest_location();
            SmsManager smsMan =  SmsManager.getDefault();
            smsMan.sendTextMessage(rec, null, updated_lcoation, null, null);
            Toast.makeText(LocationUpdatesService.this,
                    "SMS send to " +rec, Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(LocationUpdatesService.this,
                    "Something went wrong. Please check sms settings", Toast.LENGTH_LONG).show();
        }
    }

    public void send_disconnected_SMS(){
        try {
            startCamera();
            MediaPlayer mediaPlayer= MediaPlayer.create(getBaseContext(),R.raw.btdisconnect);
            mediaPlayer.start();
            String rec = session.get_sms_reciever();
            String updated_lcoation = session.get_latest_location();
            SmsManager smsMan = SmsManager.getDefault();
            smsMan.sendTextMessage(rec, null, "iBlind: device disconnected.  " + updated_lcoation, null, null);
            Toast.makeText(LocationUpdatesService.this,
                    "Device disconnected" + rec, Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Toast.makeText(LocationUpdatesService.this,
                    "Something went wrong. Please check sms settings", Toast.LENGTH_LONG).show();
        }
    }

    public void startCamera(){
        Intent dialogIntent = new Intent(this, CameraActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }
}