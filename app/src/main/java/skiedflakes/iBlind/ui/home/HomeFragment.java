package skiedflakes.iBlind.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.HashMap;

import skiedflakes.iBlind.LocationUpdatesService;
import skiedflakes.iBlind.MainActivity;
import skiedflakes.iBlind.R;
import skiedflakes.iBlind.SessionManager;
import skiedflakes.iBlind.Utils;

public class HomeFragment extends Fragment {
    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;
    private HomeViewModel homeViewModel;
    Button btn_start,btn_stop,btn_logout;
    SessionManager session;
    String user_type;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        session = new SessionManager(getActivity());
        session = new SessionManager(getActivity().getApplicationContext());
        HashMap<String, String> user_account = session.getUserDetails();
        user_type = user_account.get(SessionManager.KEY_USER_TYPE);


        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        myReceiver = new MyReceiver();
        btn_start = root.findViewById(R.id.btn_start);
        btn_stop = root.findViewById(R.id.btn_stop);
        btn_logout =  root.findViewById(R.id.btn_logout);


        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).stop();
            }
        });
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).start();

            }
        });



        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                ((MainActivity)getActivity()).stop();
                ((MainActivity)getActivity()).logout();
            }
        });


        if(user_type.equals("1")){ //blind
            btn_start.setVisibility(View.VISIBLE);
            btn_stop.setVisibility(View.VISIBLE);
        }else if(user_type.equals("2")){ //relative
            btn_start.setVisibility(View.GONE);
            btn_stop.setVisibility(View.GONE);
        }


        return root;
    }


    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(getActivity(), Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}