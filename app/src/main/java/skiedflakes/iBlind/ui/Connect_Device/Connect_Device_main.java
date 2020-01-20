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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmedabdelmeged.bluetoothmc.BluetoothMC;
import com.ahmedabdelmeged.bluetoothmc.ui.BluetoothDevices;
import com.ahmedabdelmeged.bluetoothmc.util.BluetoothStates;

import skiedflakes.iBlind.Globals;
import skiedflakes.iBlind.MainActivity;
import skiedflakes.iBlind.R;
import skiedflakes.iBlind.SessionManager;

public class Connect_Device_main extends Fragment {
    LinearLayout btn_connect,btn_send,btn_check_sms,btn_check_video;
    Globals globals;
    BluetoothMC bluetoothMC;

    TextView tv_device,tv_app,tv_status;
    EditText et_receiver;

    SessionManager session;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.connect_device_main, container, false);
        btn_connect = view.findViewById(R.id.btn_connect);
        tv_device = view.findViewById(R.id.tv_device);

        tv_status =view.findViewById(R.id.tv_status);
        btn_send =view.findViewById(R.id.btn_send);
        btn_check_sms =view.findViewById(R.id.btn_check_sms);
        et_receiver =view.findViewById(R.id.et_receiver);
        btn_check_video=view.findViewById(R.id.btn_check_video);

        session = new SessionManager(getActivity());
        session = new SessionManager(getActivity().getApplicationContext());

        String reciv = session.get_sms_reciever();
        if (!reciv.equals("")){
            et_receiver.setText(reciv);
        }

        btn_check_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).startCamera();
            }
        });

        btn_check_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_receiver.getText().toString().equals("")){
                    Toast.makeText(getContext(), "Please fill up number", Toast.LENGTH_SHORT).show();
                }{
                    ((MainActivity)getActivity()).sendSMS();
                }

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String recev = et_receiver.getText().toString();
                Toast.makeText(getContext(), recev, Toast.LENGTH_SHORT).show();
                if(recev.equals("")){
                    Toast.makeText(getContext(), "Please fill up field", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Successfully saved number", Toast.LENGTH_SHORT).show();
                    session.set_sms_reciever(recev);
                }
            }
        });

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
                        tv_status.setText("Status: Device Connecting");
                    }

                    @Override
                    public void onDeviceConnected() {
                    tv_status.setText("Status: Device Connected");
                    bluetoothMC.send("1");


                    }

                    @Override
                    public void onDeviceDisconnected() {
                        Log.e("status","onDeviceDisconnected");
                        tv_status.setText("Status: Device Disconnected ");
                    }

                    @Override
                    public void onDeviceConnectionFailed() {
                        //this method triggered if the connection failed
                        tv_status.setText("Status: Device Connection Failed");
                    }
                });

                bluetoothMC.setOnDataReceivedListener(new BluetoothMC.onDataReceivedListener() {
                    @Override
                    public void onDataReceived(String data) {
                       if(data.equals("con1")){
                           tv_status.setText("Status: Device Connected");
                       }
                    }
                });

                //set listener to keep track the communication errors
                bluetoothMC.setOnBluetoothErrorsListener(new BluetoothMC.BluetoothErrorsListener() {
                    @Override
                    public void onSendingFailed() {
                        Log.e("status","onSendingFailed");
                        //this method triggered if the app failed to send data
                    }

                    @Override
                    public void onReceivingFailed() {
                        Log.e("status","onReceivingFailed");
                        //this method triggered if the app failed to receive data
                        tv_status.setText("Status: Device Disconnected");
                    }

                    @Override
                    public void onDisconnectingFailed() {
                        Log.e("status","onDisconnectingFailed");
                        //this method triggered if the app failed to disconnect to the bluetooth device
                    }

                    @Override
                    public void onCommunicationFailed() {
                        Log.e("status","onCommunicationFailed");
                        //this method triggered if the app connect and unable to send and receive data
                        //from the bluetooth device
                    }
                });


            }
        });
        return view;
    }



}
