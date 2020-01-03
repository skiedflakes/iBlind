package skiedflakes.iBlind.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import skiedflakes.iBlind.AppController;
import skiedflakes.iBlind.R;

public class DashboardFragment extends Fragment  implements OnMapReadyCallback {

    private DashboardViewModel dashboardViewModel;
    private GoogleMap mMap;
    Fragment map_frag;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

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

        plot_location();
    }


    public void plot_location() {
        mMap.clear();

        String URL = getString(R.string.URL)+"get_gps_logs.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(getActivity(), response, Toast.LENGTH_SHORT).show();
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
                        LatLng sydney = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                        mMap.addMarker(new MarkerOptions().position(sydney).title(dates));
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                        if(jsonArray_salesman.length()-1 == i){

                            LatLng end_latlng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                            moveToCurrentLocation(end_latlng);
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
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 1000, null);
    }
}