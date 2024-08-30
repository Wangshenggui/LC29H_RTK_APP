package com.example.lc29h_rtk_app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lc29h_rtk_app.main_fragment.BluetoothFragment;
import com.example.lc29h_rtk_app.main_fragment.FloatingWidgetService;
import com.example.lc29h_rtk_app.main_fragment.NtripFragment;
import com.example.lc29h_rtk_app.main_fragment.SettingFragment;
import com.example.lc29h_rtk_app.main_fragment.WebFragment;
import com.example.lc29h_rtk_app.main_fragment.ntrip_topfragment.Ntrip_top1Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public static OutputStream outputStream = null;
    public static InputStream inputStream = null;

    public static String ReadGGSString = " ";
    public static String ReadRMCString = " ";
    public static boolean BluetoothConFlag = false;

    public static double last_lon = 106.36;
    public static double last_lat = 26.23;
    public static double new_lon = 0;
    public static double new_lat = 0;

    public static double getlast_lon() {
        synchronized (last_lon_lock) {
            return last_lon;
        }
    }

    public static void setlast_lon(double n) {
        synchronized (last_lon_lock) {
            last_lon = n;
        }
    }

    public static double getlast_lat() {
        synchronized (last_lat_lock) {
            return last_lat;
        }
    }

    public static void setlast_lat(double n) {
        synchronized (last_lat_lock) {
            last_lat = n;
        }
    }

    public static double getnew_lon() {
        synchronized (new_lon_lock) {
            return new_lon;
        }
    }

    public static void setnew_lon(double n) {
        synchronized (new_lon_lock) {
            new_lon = n;
        }
    }

    public static double getnew_lat() {
        synchronized (new_lat_lock) {
            return new_lat;
        }
    }

    public static void setnew_lat(double n) {
        synchronized (new_lat_lock) {
            new_lat = n;
        }
    }

    public static boolean getBluetoothConFlag() {
        synchronized (BluetoothCon_lock) {
            return BluetoothConFlag;
        }
    }

    public static void setBluetoothConFlag(boolean flag) {
        synchronized (BluetoothCon_lock) {
            BluetoothConFlag = flag;
        }
    }

    // 获取 ReadGGSString
    public static String getReadGGAString() {
        synchronized (GGA_lock) {
            return ReadGGSString;
        }
    }

    // 设置 ReadGGSString
    public static void setReadGGAString(String newString) {
        synchronized (GGA_lock) {
            ReadGGSString = newString;
        }
    }

    // 获取 ReadRMCString
    public static String getReadRMCString() {
        synchronized (RMC_lock) {
            return ReadRMCString;
        }
    }

    // 设置 ReadRMCString
    public static void setReadRMCString(String newString) {
        synchronized (RMC_lock) {
            ReadRMCString = newString;
        }
    }

    public static SocketService socketService;
    public static boolean isBound = false;

    public static final Object GGA_lock = new Object();
    public static final Object RMC_lock = new Object();
    public static final Object BluetoothCon_lock = new Object();
    public static final Object last_lon_lock = new Object();
    public static final Object last_lat_lock = new Object();
    public static final Object new_lon_lock = new Object();
    public static final Object new_lat_lock = new Object();

    private static Toast toast;
    private static final int REQUEST_CODE = 101;

    private BottomNavigationView mNavigationView;
    private FragmentManager mFragmentManager;
    private int lastFragment;
    private Fragment[] fragments;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            socketService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // 处理接收到的消息
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startRequiredServices();
        setupNavigationView();

//        //播放音乐
//        MediaPlayer mediaPlayer = MediaPlayer.create(this,R.raw.music_test);
//        mediaPlayer.start();


        // 延迟加载其他Fragment以避免崩溃
        new Handler().postDelayed(this::loadOtherFragments, 500);


        // 检查电池优化设置
        checkBatteryOptimization();
    }

    private void startRequiredServices() {
        // 启动Socket服务
        Intent socketServiceIntent = new Intent(this, SocketService.class);
        startService(socketServiceIntent);

        // 检查并请求悬浮控件权限
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        } else {
            startService(new Intent(this, FloatingWidgetService.class));
        }
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Settings.canDrawOverlays(this)) {
            startService(new Intent(this, FloatingWidgetService.class));
        } else {
            showToast(this, "Permission denied. Cannot display floating widget.");
        }
    }

    private void setupNavigationView() {
        mNavigationView = findViewById(R.id.main_navigation_bar);
        initFragment();
        initListener();
    }

    private void initFragment() {
        BluetoothFragment mBluetoothFragment = new BluetoothFragment();
        NtripFragment mNtripFragment = new NtripFragment();
        WebFragment mWebFragment = new WebFragment();
        SettingFragment mSettingFragment = new SettingFragment();

        fragments = new Fragment[]{mBluetoothFragment, mNtripFragment, mWebFragment, mSettingFragment};
        mFragmentManager = getSupportFragmentManager();
        lastFragment = 0;
        mFragmentManager.beginTransaction()
                .replace(R.id.main_page_controller, mBluetoothFragment)
                .show(mBluetoothFragment)
                .commit();
    }

    private void initListener() {
        mNavigationView.setOnNavigationItemSelectedListener(item -> {
            int i = item.getItemId();
            if (i == R.id.Bluetooth && lastFragment != 0) {
                switchFragment(lastFragment, 0);
                lastFragment = 0;
                return true;
            } else if (i == R.id.Ntrip && lastFragment != 1) {
                switchFragment(lastFragment, 1);
                lastFragment = 1;
                return true;
            } else if (i == R.id.Web && lastFragment != 2) {
                switchFragment(lastFragment, 2);
                lastFragment = 2;
                return true;
            } else if (i == R.id.Set && lastFragment != 3) {
                switchFragment(lastFragment, 3);
                lastFragment = 3;
                return true;
            }
            return false;
        });
    }

    private void switchFragment(int lastFragment, int index) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.hide(fragments[lastFragment]);
        if (!fragments[index].isAdded()) {
            transaction.add(R.id.main_page_controller, fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }

    private void loadOtherFragments() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (int i = 1; i < fragments.length; i++) {
            if (!fragments[i].isAdded()) {
                transaction.add(R.id.main_page_controller, fragments[i]);
                transaction.hide(fragments[i]);
            }
        }
        transaction.commitAllowingStateLoss();
    }

    public static void showToast(Context context, String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SocketService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(messageReceiver, new IntentFilter("com.example.ble.RECEIVE_MESSAGE"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        unregisterReceiver(messageReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BluetoothFragment.connectThread != null) {
            BluetoothFragment.connectThread.cancel();
        }
        if (BluetoothFragment.connectedThread != null) {
            BluetoothFragment.connectedThread.cancel();
        }
    }

    private void checkBatteryOptimization() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

}
