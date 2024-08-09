package com.example.lc29h_rtk_app.main_fragment.ntrip_topfragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lc29h_rtk_app.MainActivity;
import com.example.lc29h_rtk_app.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Ntrip_top1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Ntrip_top1Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Button connectButton;
    Button sendButton;
    Button sendggaButton;
    TextView showGGA;

    boolean NtripStartFlag= false;

    // Timer variables
    private Handler handler;
    private Runnable timerRunnable;
    private static final int TIMER_INTERVAL = 1000; // 1 seconds


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Ntrip_top1Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NtripFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Ntrip_top1Fragment newInstance(String param1, String param2) {
        Ntrip_top1Fragment fragment = new Ntrip_top1Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Initialize Handler
        handler = new Handler(Looper.getMainLooper());

        // Define the task to be run periodically
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Your periodic task
                String message = MainActivity.getReadGGAString() + "\r\n";
                showGGA.setText(message + MainActivity.getReadRMCString());

                if (MainActivity.isBound) {
                    if(message.length()>50 && NtripStartFlag){
                        MainActivity.socketService.sendMessage(message);
                    }
                }

                // Repeat the task every TIMER_INTERVAL milliseconds
                handler.postDelayed(this, TIMER_INTERVAL);
            }
        };
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ntrip_top1, container, false);

        connectButton = view.findViewById(R.id.connectButton);
        sendButton = view.findViewById(R.id.sendButton);
        sendggaButton = view.findViewById(R.id.sendggaButton);
        showGGA = view.findViewById(R.id.showGGA);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = "120.253.239.161";
                String portString = "8002";
                if (ipAddress.isEmpty() || portString.isEmpty()) {
                    MainActivity.showToast(getActivity(), "请输入IP地址");
                } else {
                    int portNumber = Integer.parseInt(portString);
                    if (MainActivity.isBound) {
                        MainActivity.socketService.connectToServer(ipAddress, portNumber);
                    }
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "GET /" +
                        "RTCM33_GRCEpro" +
                        " HTTP/1.0\r\nUser-Agent: NTRIP GNSSInternetRadio/1.4.10\r\nAccept: */*\r\nConnection: close\r\nAuthorization: Basic " +
                        "Y2VkcjIxNTEzOmZ5eDY5NzQ2" +
                        "\r\n\r\n";

                if (MainActivity.isBound) {
                    MainActivity.socketService.sendMessage(message);
                }
            }
        });

        sendggaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = MainActivity.getReadGGAString() + "\r\n";

                showGGA.setText(message);

                if (MainActivity.isBound) {
                    MainActivity.socketService.sendMessage(message);
                }

                NtripStartFlag=!NtripStartFlag;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start the timer when the fragment is resumed
        startTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop the timer when the fragment is paused to prevent memory leaks
        stopTimer();
    }
    /**
     * Start the timer to execute the periodic task.
     */
    private void startTimer() {
        handler.postDelayed(timerRunnable, TIMER_INTERVAL);
    }

    /**
     * Stop the timer to prevent further execution.
     */
    private void stopTimer() {
        handler.removeCallbacks(timerRunnable);
    }
}
