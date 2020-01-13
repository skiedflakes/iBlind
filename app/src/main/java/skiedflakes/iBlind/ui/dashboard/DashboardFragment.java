package skiedflakes.iBlind.ui.dashboard;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import skiedflakes.iBlind.AppController;
import skiedflakes.iBlind.HttpsTrustManager;
import skiedflakes.iBlind.R;

public class DashboardFragment extends Fragment  implements OnMapReadyCallback {

    private DashboardViewModel dashboardViewModel;
    private GoogleMap mMap;
    Fragment map_frag;
    static String selected_date="";

    static Button btn_find,btn_date;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        btn_find = root.findViewById(R.id.btn_find);
        btn_date = root.findViewById(R.id.btn_date);



        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selected_date.equals("")){
                    Toast.makeText(getContext(), "Please Select date", Toast.LENGTH_SHORT).show();
                }else{
                    plot_location("1");
                }

            }
        });


        initializeMap();
        return root;
    }
    private void initializeMap() {
        if (mMap == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        plot_location("0");
    }

    public void plot_location(final String btn_status) {
        HttpsTrustManager.allowAllSSL();
        mMap.clear();

        String URL = getString(R.string.URL)+"get_gps_logs.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject  jsonObject = new JSONObject(response);
                    JSONArray jsonArray_salesman = jsonObject.getJSONArray("responseList");
                    String latlng, dates;
                    for (int i = 0; i < jsonArray_salesman.length(); i++) {
                        JSONObject jo = jsonArray_salesman.getJSONObject(i);
                        latlng = jo.getString("gps_loc");
                        dates = jo.getString("date");
                        String[] separated = latlng.split(",");
                        String latitude = separated[0];
                        String longitude = separated[1];

                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                        if(jsonArray_salesman.length()-1 == i){

                            LatLng end_latlng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                            moveToCurrentLocation(end_latlng);
                            LatLng sydney = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                            mMap.addMarker(new MarkerOptions().position(sydney).title(dates).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }else if(i==0){
                            LatLng sydney = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                            mMap.addMarker(new MarkerOptions().position(sydney).title(dates).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }else{
                            LatLng sydney = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                            mMap.addMarker(new MarkerOptions().position(sydney).title(dates) .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.dote_blue)));
                        }
                    }

                }catch (Exception e){}

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
                hashMap.put("user_id","2");
                hashMap.put("selected_date",selected_date);
                hashMap.put("btn",btn_status);
                return hashMap;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
        AppController.getInstance().setVolleyDuration(stringRequest);
    }

    private void moveToCurrentLocation(LatLng currentLocation) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 50000));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //sartdate datepicker
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            return dialog;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            month ++;
            NumberFormat formatter = new DecimalFormat("00");
            String date = String.valueOf(year) + "-" + String.valueOf(formatter.format(month)) + "-" + String.valueOf(dayOfMonth);
            Toast.makeText(getContext(), date, Toast.LENGTH_SHORT).show();
            selected_date = date;
            btn_date.setText(date);
        }
    }
}