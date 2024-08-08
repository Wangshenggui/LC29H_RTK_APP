package com.example.lc29h_rtk_app;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lc29h_rtk_app.main_fragment.BluetoothFragment;
import com.example.lc29h_rtk_app.main_fragment.NtripFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public static OutputStream outputStream=null;//获取输出数据
    public static InputStream inputStream=null;//获取输入数据

    public static String ReadGGSString=" ";
    public static String ReadRMCString=" ";

    private static final Object lock = new Object();
    // 定义一个静态的Toast对象
    private static Toast toast;

    // 获取 ReadGGSString
    public static String getReadGGAString() {
        synchronized (lock) {
            return ReadGGSString;
        }
    }

    // 设置 ReadGGSString
    public static void setReadGGAString(String newString) {
        synchronized (lock) {
            ReadGGSString = newString;
        }
    }

    // 获取 ReadRMCString
    public static String getReadRMCString() {
        synchronized (lock) {
            return ReadRMCString;
        }
    }

    // 设置 ReadRMCString
    public static void setReadRMCString(String newString) {
        synchronized (lock) {
            ReadRMCString = newString;
        }
    }


    private BottomNavigationView mNavigationView;
    private FragmentManager mFragmentManager;
    private int lastFragment;
    private Fragment[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationView = findViewById(R.id.main_navigation_bar);

        initFragment();
        initListener();

        // Delay loading other fragments to avoid crash
        new Handler().postDelayed(this::loadOtherFragments, 500);
    }

    private void initFragment() {
        BluetoothFragment mBluetoothFragment = new BluetoothFragment();
        NtripFragment mNtripFragment = new NtripFragment();
        fragments = new Fragment[]{mBluetoothFragment, mNtripFragment};
        mFragmentManager = getSupportFragmentManager();
        // 默认显示HomeFragment
        lastFragment = 0;
        mFragmentManager.beginTransaction()
                .replace(R.id.main_page_controller, mBluetoothFragment)
                .show(mBluetoothFragment)
                .commit();
    }
    private void initListener() {
        mNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.Bluetooth) {
                    if (lastFragment != 0) {
                        switchFragment(lastFragment, 0);
                        lastFragment = 0;
                    }
                    return true;
                } else if (i == R.id.Ntrip) {
                    if (lastFragment != 1) {
                        switchFragment(lastFragment, 1);
                        lastFragment = 1;
                    }
                    return true;
                }
                return false;
            }
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

    // 在需要显示Toast消息的地方调用这个方法
    public static void showToast(Context context, String message) {
        // 如果toast不为null，则取消当前Toast
        if (toast != null) {
            toast.cancel();
        }

        // 创建新的Toast实例
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}