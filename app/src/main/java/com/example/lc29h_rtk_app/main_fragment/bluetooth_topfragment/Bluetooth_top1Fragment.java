package com.example.lc29h_rtk_app.main_fragment.bluetooth_topfragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lc29h_rtk_app.MainActivity;
import com.example.lc29h_rtk_app.R;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Bluetooth_top1Fragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    // Bluetooth operation variables
    private ListView BtList;
    private Button btnScan;
    private Intent intent;
    private BluetoothAdapter bluetoothAdapter;
    private List<String> devicesNames;
    private ArrayList<BluetoothDevice> readyDevices;
    private ArrayAdapter<String> btNames;

    // BLE connection variables
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;

    // BLE scanning variables
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean isScanning = false;
    private Set<String> deviceNamesSet;

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
        btnScan = view.findViewById(R.id.btn_scan);

        // Initialize Bluetooth adapter and scanner
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        deviceNamesSet = new HashSet<>();

        if (!bluetoothAdapter.isEnabled()) {
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }

        readyDevices = new ArrayList<>();
        devicesNames = new ArrayList<>();
        btNames = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, devicesNames);
        BtList.setAdapter(btNames);

        // Load paired devices initially
        loadPairedDevices();

        // Set up the scan button
        btnScan.setOnClickListener(v -> {
            if (isScanning) {
                stopScan();
            } else {
                startScan();
            }
        });

        BtList.setOnItemClickListener((parent, view1, position, id) -> {
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                bluetoothGatt = null;
            }

            BluetoothDevice device = readyDevices.get(position);
            bluetoothGatt = device.connectGatt(getActivity(), false, gattCallback);

            Toast.makeText(getActivity(), "正在连接 " + device.getName(), Toast.LENGTH_SHORT).show();
        });

        // Initialize and start the timer
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Timer task code here
                // Example: Toast message
                // MainActivity.showToast(getActivity(), MainActivity.getReadGGAString());

                // Schedule the next execution
                timerHandler.postDelayed(this, 1000); // Repeat every 1 seconds
            }
        };
        timerHandler.post(timerRunnable); // Start the timer

        return view;
    }

    @SuppressLint("MissingPermission")
    private void loadPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                readyDevices.add(device);
                devicesNames.add(device.getName());
                deviceNamesSet.add(device.getName());
            }
            btNames.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), "没有设备已经配对！", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                    1);
            return;
        }

        devicesNames.clear();
        readyDevices.clear();
        deviceNamesSet.clear();
        btNames.notifyDataSetChanged();

        isScanning = true;
        btnScan.setText("Stop Scanning");
        bluetoothLeScanner.startScan(null, buildScanSettings(), leScanCallback);
        Toast.makeText(getActivity(), "开始扫描...", Toast.LENGTH_SHORT).show();
    }

    private void stopScan() {
        isScanning = false;
        btnScan.setText("Scan BLE");
        bluetoothLeScanner.stopScan(leScanCallback);
        Toast.makeText(getActivity(), "扫描停止", Toast.LENGTH_SHORT).show();
    }

    private ScanSettings buildScanSettings() {
        return new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
    }

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null && device.getName() != null) {
                String deviceName = device.getName();
                if (!deviceNamesSet.contains(deviceName)) {
                    deviceNamesSet.add(deviceName);
                    devicesNames.add(deviceName);
                    readyDevices.add(device);
                    getActivity().runOnUiThread(() -> {
                        btNames.notifyDataSetChanged(); // Update ListView
                        Toast.makeText(getActivity(), "发现设备: " + deviceName, Toast.LENGTH_SHORT).show(); // Show Toast
                    });
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BluetoothScan", "Scan failed with error: " + errorCode);
            Toast.makeText(getActivity(), "扫描失败: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "已连接 " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();
                });
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "蓝牙已断开", Toast.LENGTH_SHORT).show();
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 获取服务
                BluetoothGattService service = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
                if (service != null) {
                    // 获取特征
                    characteristic = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                    if (characteristic != null) {
                        // 启用通知
                        gatt.setCharacteristicNotification(characteristic, true);
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }

                        // 发送示例命令
                        characteristic.setValue("$PAIR062,3,0*3D\r\n");
                        gatt.writeCharacteristic(characteristic);

                        // 播放声音
                        MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), R.raw.bluetooth_connected);
                        mediaPlayer.start();
                    }
                }
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Handle successful write
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            String receivedData = new String(data, StandardCharsets.UTF_8);

            // Handle received data
            getActivity().runOnUiThread(() -> {
//                Toast.makeText(getActivity(), "收到消息\n" + receivedData, Toast.LENGTH_LONG).show();
                MainActivity.setReadGGAString(receivedData);
//                MainActivity.showToast(getActivity(), "Received data: " + receivedData);
            });
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        if (isScanning) {
            stopScan();
        }
    }
}
