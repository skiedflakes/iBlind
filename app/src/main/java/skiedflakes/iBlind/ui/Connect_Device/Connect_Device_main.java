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
import android.widget.TextView;
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
    BluetoothMC bluetoothMC;

    TextView tv_device,tv_app,tv_status;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.connect_device_main, container, false);
        btn_connect = view.findViewById(R.id.btn_connect);
        tv_device = view.findViewById(R.id.tv_device);

        tv_status =view.findViewById(R.id.tv_status);



        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothMC = new BluetoothMC();
                ((MainActivity)getActivity()).start_connection(bluetoothMC);

                bluetoothMC.setOnBluetoothConnectionListener(new BluetoothMC.BluetoothConnectionListener() {
                    @Override
                    public void onDeviceConnecting() {
                        //this method triggered during the connection processes
                        Log.e("status","onDeviceConnecting");
                        tv_status.setText("Status: Connecting Device . . .");
                    }

                    @Override
                    public void onDeviceConnected() {
                    tv_status.setText("Status: Connected...Sending con");
                    bluetoothMC.send("con");

                    }

                    @Override
                    public void onDeviceDisconnected() {
                        tv_status.setText("Status: Disconnected Device . . .");
                    }

                    @Override
                    public void onDeviceConnectionFailed() {
                        //this method triggered if the connection failed
                        tv_status.setText("Status: Connection Failed Device . . .");
                    }
                });

                bluetoothMC.setOnDataReceivedListener(new BluetoothMC.onDataReceivedListener() {
                    @Override
                    public void onDataReceived(String data) {
                       tv_device.setText("Device: "+data);
                    }
                });
            }
        });




        return view;
    }



}
