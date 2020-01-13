package skiedflakes.iBlind;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

import skiedflakes.iBlind.ui.Login.Login;


public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    public SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "userLogin";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_PASSWORD = "password";

    // Email address (make variable public to access from outside)
    public static final String KEY_NAME = "name";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_TYPE = "user_type";
    public static final String KEY_latest_location = "latest_location";
    public static final String KEY_sms_reciever = "sms_reciever";
    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(String name,String user_type,String user_id){

        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        // Storing email in pref
        editor.putString(KEY_NAME, name);
        // Storing branch id
        editor.putString(KEY_USER_TYPE, user_type);

        editor.putString(KEY_USER_ID, user_id);
        // commit changes
        editor.commit();
    }

    public void update_latest_location (String location){
        // Storing login value as TRUE
        editor.putString(KEY_latest_location, location);
        editor.commit();
    }

    public void set_sms_reciever(String sms_reciever){
        editor.putString(KEY_sms_reciever, sms_reciever);
        editor.commit();
    }

    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // Do nothing
        } else {
            // user is not logged in redirect him to Login Activity
//            Intent i = new Intent(_context, Main.class);
//            // Closing all the Activities
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            // Add new Flag to start new Activity
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            // Staring Login Activity
//            _context.startActivity(i);
        }
    }


    /*** Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();

        // name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        //user type
        user.put(KEY_USER_TYPE, pref.getString(KEY_USER_TYPE, null));

        //user type
        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loging Activity
        Intent i = new Intent(_context, Login.class);
        // Closing all the Activities

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    public String get_latest_location(){
        return pref.getString(KEY_latest_location, null);
    }

    public String get_sms_reciever(){
        return pref.getString(KEY_sms_reciever, null);
    }

}
