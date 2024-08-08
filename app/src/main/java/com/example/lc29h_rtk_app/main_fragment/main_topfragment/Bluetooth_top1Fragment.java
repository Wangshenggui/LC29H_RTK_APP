package com.example.lc29h_rtk_app.main_fragment.main_topfragment;

import static com.example.lc29h_rtk_app.BtThread.ConnectThread.bluetoothSocket;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lc29h_rtk_app.MainActivity;
import com.example.lc29h_rtk_app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Bluetooth_top1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class Bluetooth_top1Fragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    // Bluetooth operation variables
    ListView BtList = null;
    Intent intent = null;
    BluetoothAdapter bluetoothAdapter = null;
    List<String> devicesNames = new ArrayList<>();
    ArrayList<BluetoothDevice> readyDevices = null;
    ArrayAdapter<String> btNames = null;

    // Custom thread variables
    static com.example.lc29h_rtk_app.BtThread.ConnectThread connectThread = null;
    static com.example.lc29h_rtk_app.BtThread.ConnectedThread connectedThread = null;

    // Timer-related variables
    private Handler timerHandler;
    private Runnable timerRunnable;

    public static Bluetooth_top1Fragment newInstance(String param1, String param2) {
        Bluetooth_top1Fragment fragment = new Bluetooth_top1Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public Bluetooth_top1Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bluetooth_top1, container, false);

        BtList = view.findViewById(R.id.BtList);

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }
        @SuppressLint("MissingPermission")
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        readyDevices = new ArrayList<>();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                readyDevices.add(device);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Request permissions if not granted
                }
                devicesNames.add(device.getName());
            }
            ArrayAdapter<String> btNames = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, devicesNames);
            BtList.setAdapter(btNames);
        } else {
            Toast.makeText(getActivity(), "没有设备已经配对！", Toast.LENGTH_SHORT).show();
        }

        BtList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (connectThread != null) {
                    connectThread.cancel();
                    connectThread = null;
                }
                connectThread = new com.example.lc29h_rtk_app.BtThread.ConnectThread(readyDevices.get(position));
                connectThread.start();

                int delayCount = 0;
                while (true) {
                    try {
                        Thread.sleep(100); // Delay 100ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                        connectedThread = new com.example.lc29h_rtk_app.BtThread.ConnectedThread(bluetoothSocket);
                        connectedThread.start();
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Toast.makeText(getActivity(), "已连接" + readyDevices.get(position).getName() + "\r\n开启数据线程", Toast.LENGTH_SHORT).show();

                        connectedThread.btWriteString("$PAIR062,3,0*3D\r\n");
                        connectedThread.btWriteString("$PAIR062,2,0*3C\r\n");
                        connectedThread.btWriteString("$PAIR062,5,0*3B\r\n");
                        connectedThread.btWriteString("$PAIR062,1,0*3F\r\n");

                        connectedThread.btWriteString("$PAIR062,3,0*3D\r\n");
                        connectedThread.btWriteString("$PAIR062,2,0*3C\r\n");
                        connectedThread.btWriteString("$PAIR062,5,0*3B\r\n");
                        connectedThread.btWriteString("$PAIR062,1,0*3F\r\n");

                        connectedThread.btWriteString("$PAIR062,3,0*3D\r\n");
                        connectedThread.btWriteString("$PAIR062,2,0*3C\r\n");
                        connectedThread.btWriteString("$PAIR062,5,0*3B\r\n");
                        connectedThread.btWriteString("$PAIR062,1,0*3F\r\n");

                        connectedThread.btWriteString("$PAIR062,3,0*3D\r\n");
                        connectedThread.btWriteString("$PAIR062,2,0*3C\r\n");
                        connectedThread.btWriteString("$PAIR062,5,0*3B\r\n");
                        connectedThread.btWriteString("$PAIR062,1,0*3F\r\n");

                        break;
                    }
                    delayCount++;
                    if (delayCount >= 50) {
                        break;
                    }
                }
            }
        });

        // Initialize and start the timer
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Timer task code here
                // For example, update UI or perform periodic checks
                // Example: Toast message
//                MainActivity.showToast(getActivity(),MainActivity.getReadGGAString());

                // Schedule the next execution
                timerHandler.postDelayed(this, 1000); // Repeat every 1 seconds
            }
        };
        timerHandler.post(timerRunnable); // Start the timer

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the timer
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}
