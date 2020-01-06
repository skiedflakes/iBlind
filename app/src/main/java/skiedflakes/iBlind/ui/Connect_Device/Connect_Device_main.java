package skiedflakes.iBlind.ui.Connect_Device;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.ahmedabdelmeged.bluetoothmc.BluetoothMC;
import com.ahmedabdelmeged.bluetoothmc.ui.BluetoothDevices;
import com.ahmedabdelmeged.bluetoothmc.util.BluetoothStates;

import skiedflakes.iBlind.Globals;
import skiedflakes.iBlind.MainActivity;
import skiedflakes.iBlind.R;

public class Connect_Device_main extends Fragment {
    Button btn_connect;
    Globals globals;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.connect_device_main, container, false);
        btn_connect = view.findViewById(R.id.btn_connect);



        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).start_connection();
            }
        });


        return view;
    }



}
