package com.example.lc29h_rtk_app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.lc29h_rtk_app.main_fragment.BluetoothFragment;
import com.example.lc29h_rtk_app.main_fragment.FloatingWidgetService;
import com.example.lc29h_rtk_app.main_fragment.NtripFragment;
import com.example.lc29h_rtk_app.main_fragment.WebFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GaodeMapActivity";
    private MapView mapView;

    private AMap aMap = null;
    private double lat;
    private double lon;
    public AMapLocationClient mLocationClient = null;
    public AMapLocationClientOption mLocationOption = null;
    private Button btn_search;

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

//        startRequiredServices();
//        setupNavigationView();
//
//        // 延迟加载其他Fragment以避免崩溃
//        new Handler().postDelayed(this::loadOtherFragments, 500);

//        init();

//        btn_search = findViewById(R.id.btn_search);
//        //这地方就是下面要讲的搜索导航功能
//        btn_search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
////                startActivity(intent);
//            }
//        });
        MapsInitializer.updatePrivacyShow(this,true,true);
        MapsInitializer.updatePrivacyAgree(this,true);
        //初始化地图控件
        mapView = (MapView) findViewById(R.id.gaode_map);
        try {
            mLocationClient = new AMapLocationClient(MainActivity.this);
        }catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            //展示地图
            aMap = mapView.getMap();
            Log.i(TAG,"展示地图");
        }
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE); //持续定位
        //设置连续定位模式下定位间隔
        myLocationStyle.interval(2000);
        myLocationStyle.strokeWidth(20f);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.showMyLocation(true);

        // 检查电池优化设置
        checkBatteryOptimization();
    }

    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    amapLocation.getLatitude();//获取纬度
                    amapLocation.getLongitude();//获取经度
                    amapLocation.getAccuracy();//获取精度信息
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(amapLocation.getTime());
                    df.format(date);//定位时间
                    amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    amapLocation.getCountry();//国家信息
                    amapLocation.getProvince();//省信息
                    amapLocation.getCity();//城市信息
                    amapLocation.getDistrict();//城区信息
                    amapLocation.getStreet();//街道信息
                    amapLocation.getStreetNum();//街道门牌号信息
                    amapLocation.getCityCode();//城市编码
                    amapLocation.getAdCode();//地区编码
                    amapLocation.getAoiName();//获取当前定位点的AOI信息
                    lat = amapLocation.getLatitude();
                    lon = amapLocation.getLongitude();
                    Log.v("pcw","lat : "+lat+" lon : "+lon);

                    // 设置当前地图显示为当前位置
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(lat, lon));
                    markerOptions.title("当前位置");
                    markerOptions.visible(true);
                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background));
                    markerOptions.icon(bitmapDescriptor);
                    aMap.addMarker(markerOptions);

                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Toast.makeText(MainActivity.this,
                            amapLocation.getErrorCode() + ", errInfo:"
                                    + amapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    private void init() {
        // 初始化定位
        try {
            AMapLocationClient.updatePrivacyShow(this, true, true);
            AMapLocationClient.updatePrivacyAgree(this, true);
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        // 初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 获取一次定位结果
        mLocationOption.setOnceLocation(false);
        // 获取最近3s内精度最高的一次定位结果
        mLocationOption.setOnceLocationLatest(true);
        // 设置是否返回地址信息
        mLocationOption.setNeedAddress(true);
        // 设置是否允许模拟位置
        mLocationOption.setMockEnable(false);
        // 关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        // 给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }

    private class MyAMapLocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    Log.e("位置：", aMapLocation.getAddress());
                    showToast(MainActivity.this, "位置：" + aMapLocation.getAddress());
                } else {
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                    showToast(MainActivity.this, "AmapError" + "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    }

    private void startRequiredServices() {
        Intent socketServiceIntent = new Intent(this, SocketService.class);
        startService(socketServiceIntent);

        Intent webSocketServiceIntent = new Intent(this, WebSocketService.class);
        startService(webSocketServiceIntent);

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
//        mNavigationView = findViewById(R.id.main_navigation_bar);
        initFragment();
        initListener();
    }

    private void initFragment() {
        BluetoothFragment mBluetoothFragment = new BluetoothFragment();
        NtripFragment mNtripFragment = new NtripFragment();
        WebFragment mWebFragment = new WebFragment();
        fragments = new Fragment[]{mBluetoothFragment, mNtripFragment, mWebFragment};
        mFragmentManager = getSupportFragmentManager();
        lastFragment = 0;
        mFragmentManager.beginTransaction()
//                .replace(R.id.main_page_controller, mBluetoothFragment)
                .commitNow();
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
            }
            return false;
        });
    }

    private void switchFragment(int lastFragment, int index) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.hide(fragments[lastFragment]);
        if (!fragments[index].isAdded()) {
//            transaction.add(R.id.main_page_controller, fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }

    private void loadOtherFragments() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (int i = 1; i < fragments.length; i++) {
            if (!fragments[i].isAdded()) {
//                transaction.add(R.id.main_page_controller, fragments[i]);
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
