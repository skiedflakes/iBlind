package skiedflakes.iBlind.ui.Login;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import skiedflakes.iBlind.AppController;
import skiedflakes.iBlind.R;


public class Login extends Fragment {
    Button btn_login;
    EditText et_username,et_password;
    String username,password;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.login, container, false);
        et_username = view.findViewById(R.id.et_username);
        et_password = view.findViewById(R.id.et_password);
        btn_login = view.findViewById(R.id.btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        return view;
    }


    private void login() {
        username= et_username.getText().toString();
        password= et_password.getText().toString();
        if(username.equals("")||password.equals("")){
            Toast.makeText(getContext(), "Please fill-up required fields", Toast.LENGTH_SHORT).show();
        }else{
            String URL = "events/get_all_events4.php";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

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
                    hashMap.put("username", username);
                    hashMap.put("password", password);
                    return hashMap;
                }
            };
            AppController.getInstance().addToRequestQueue(stringRequest);
            AppController.getInstance().setVolleyDuration(stringRequest);
        }

    }
}