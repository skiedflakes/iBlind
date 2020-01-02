package skiedflakes.iBlind.ui.Login;

        import androidx.appcompat.app.AppCompatActivity;

        import android.app.ActivityOptions;
        import android.content.Intent;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;

        import com.android.volley.AuthFailureError;
        import com.android.volley.Request;
        import com.android.volley.Response;
        import com.android.volley.VolleyError;
        import com.android.volley.toolbox.StringRequest;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.util.HashMap;
        import java.util.Map;

        import skiedflakes.iBlind.AppController;
        import skiedflakes.iBlind.MainActivity;
        import skiedflakes.iBlind.R;
        import skiedflakes.iBlind.SessionManager;

public class Login extends AppCompatActivity {
    Button btn_login;
    EditText et_username,et_password;
    SessionManager session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        session = new SessionManager(getApplicationContext());
        btn_login = findViewById(R.id.btn_login);
        et_password = findViewById(R.id.et_password);
        et_username = findViewById(R.id.et_username);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_login.setEnabled(false);
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();


                if(username.equals("")||password.equals("")){
                    Toast.makeText(Login.this, "Please fill up required fields.", Toast.LENGTH_SHORT).show();
                }else{
                    get_events(username,password);
                }

            }
        });
        sessionCheck();
    }

    private void sessionCheck() {
        if (this.session.isLoggedIn()){
            Intent myIntent = new Intent(Login.this, MainActivity.class);

            ActivityOptions options =
                    ActivityOptions.makeCustomAnimation(Login.this, R.anim.slide_in, R.anim.slide_out);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Login.this.startActivity(myIntent, options.toBundle());
            finish();
        }
    }

    private void get_events(final String username,final String password) {
        String URL = getString(R.string.URL)+"login.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                try {
                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray  jsonArray = jsonObject.getJSONArray("data");
                    JSONObject jsonObject1 = (JSONObject)jsonArray.get(0);
                    String name= jsonObject1.getString("name");
                    String user_type=   jsonObject1.getString("user_type");

                    if(user_type.equals("1")||user_type.equals("2")){

                        session.createLoginSession(name,user_type);

                        Intent myIntent = new Intent(Login.this, MainActivity.class);

                        ActivityOptions options =
                                ActivityOptions.makeCustomAnimation(Login.this, R.anim.slide_in, R.anim.slide_out);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Login.this.startActivity(myIntent, options.toBundle());
                        finish();

                    }else{
                        Toast.makeText(Login.this, "Invalid User", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Toast.makeText(Login.this, "Error! Something went wrong.", Toast.LENGTH_SHORT).show();
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
