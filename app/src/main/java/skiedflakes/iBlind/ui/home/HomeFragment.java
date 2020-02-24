package skiedflakes.iBlind.ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import skiedflakes.iBlind.LocationUpdatesService;
import skiedflakes.iBlind.MainActivity;
import skiedflakes.iBlind.R;
import skiedflakes.iBlind.SessionManager;
import skiedflakes.iBlind.Utils;

public class HomeFragment extends Fragment {
    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;
    private HomeViewModel homeViewModel;
    LinearLayout btn_start,btn_stop,btn_logout;
    SessionManager session;
    String user_type;
    EditText et_N,et_value;
    Button btn_test;

    int N;
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

//        btn_test = root.findViewById(R.id.btn_test);
//        et_N =root.findViewById(R.id.et_N);
//        et_value =root.findViewById(R.id.et_value);


        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               N = Integer.valueOf(et_N.getText().toString());
//                String final_answer = test2(N);
//                et_value.setText(final_answer);

                Log.e("array: ", "test: "+ Arrays.toString(test4(N)));
             ;


            }
        });

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

    public String test2(int N){
        String test_return="";
        if(N>=1&N<=100){
            for(int i=0;i<=N;i++){
                if(i % 2==0){
                    test_return = test_return+"-";
                }
                else{
                    test_return = test_return+"+";
                }

            }
            return test_return;

        }else{
            return "Error";
        }
    }

    public void test3(int N){
        int max = 100;
        int min = 100;
        Random r = new Random();
        System.out.println("The ten random values are: ");
        int[] values = new int[10];
        for(int i = 0; i < 10; i++) {
            int randomInteger = r.nextInt(100);

            if(i==0){
                values[i] = randomInteger;
                System.out.print(" ," + randomInteger);
            }else{ //check if integer has double

                Boolean check_integer = check_integer(values,randomInteger);
                if(check_integer){
                    int randomInteger2 = r.nextInt(100);
                    values[i] = randomInteger2;
                    System.out.print(" ," + randomInteger2);
                }else{
                    values[i] = randomInteger;
                    System.out.print(" ," + randomInteger);
                }
            }


        }

        int sum = 0;
        for(int i : values) {
            sum += i;
        }
    Log.e("test ", "test "+sum);
    }

    public boolean check_integer(int[] arr, int toCheckValue){

        // sort given array
        Arrays.sort(arr);

        // check if the specified element
        // is present in the array or not
        // using Binary Search method
        int res = Arrays.binarySearch(arr, toCheckValue);

        boolean test = res > 0 ? true : false;

        return test;
    }

    public  int[] test4(int N){

        boolean isSumZero;
        //do while
        int max = 100;
        int min = -100;
        Random randy = new Random();
        int[] readArray = new int[N];
        do {

            for (int i = 0; i < readArray.length; i++) {
                int temp;
                boolean isExists;
                do {
                    isExists = false;
                    temp = randy.nextInt((max - min) + 1) + min;

                    for (int j = 0; j < i; j++) {
                        if (readArray[j] == temp) {
                            isExists = true;
                            break;
                        }
                    }
                } while (isExists);
                readArray[i] = temp;

            }

            int sum = 0;
            for (int i : readArray) {
                sum += i;
            }

            if (sum == 0) {
                isSumZero = false;

            } else {
                isSumZero = true;

            }
        }while(isSumZero);


        return readArray;
    }

    public int solution(int[] A) {
        int ans = 0;
        for (int i = 1; i < A.length; i++) {
            if (ans > A[i]) {
                ans = A[i];
            }
        }
        return ans;
    }


    public static int generateRandomIntIntRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
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