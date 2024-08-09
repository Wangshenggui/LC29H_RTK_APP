package com.example.lc29h_rtk_app.main_fragment;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.lc29h_rtk_app.MainActivity; // 如果需要从 MainActivity 获取参数
import com.example.lc29h_rtk_app.R;

public class FloatingWidgetService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private TextView parameterText; // 用于显示参数的TextView
    private boolean isTextVisible = false; // 用于标识文本是否可见
    private static final int CLICK_THRESHOLD = 10; // 设置点击事件的移动阈值
    private Handler handler; // 用于定时更新参数信息的Handler
    private Runnable updateTask; // 定时更新任务

    int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 将悬浮控件布局膨胀为View
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget, null);

        // 初始化 TextView
        parameterText = mFloatingView.findViewById(R.id.parameterText);

        // 设置悬浮控件的布局参数
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // 初始化位置
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = getScreenWidth(FloatingWidgetService.this) - getScreenWidth(FloatingWidgetService.this)/5;
        params.y = 100;

        // 获取 WindowManager 服务
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        // 设置控件的拖动功能
        mFloatingView.findViewById(R.id.floating_icon).setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录初始位置
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        lastAction = motionEvent.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        // 计算手指抬起时的移动距离
                        float deltaX = motionEvent.getRawX() - initialTouchX;
                        float deltaY = motionEvent.getRawY() - initialTouchY;

                        // 如果移动距离小于阈值，触发点击事件
                        if (Math.abs(deltaX) < CLICK_THRESHOLD && Math.abs(deltaY) < CLICK_THRESHOLD) {
                            toggleParameterVisibility(); // 切换文本可见性
                        }
                        lastAction = motionEvent.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // 计算移动距离
                        params.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                        params.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);

                        // 更新悬浮控件位置
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        lastAction = motionEvent.getAction();
                        return true;
                }
                return false;
            }
        });

        // 初始化 Handler 和 定时任务
        handler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                if (isTextVisible) {
                    showParameterInfo(); // 刷新参数信息
                }
                handler.postDelayed(this, 1000); // 每1秒更新一次
            }
        };
    }

    /**
     * 切换参数信息的可见性
     */
    private void toggleParameterVisibility() {
        if (isTextVisible) {
            // 隐藏参数信息
            parameterText.setVisibility(View.GONE);
            isTextVisible = false;
            handler.removeCallbacks(updateTask); // 移除定时任务
        } else {
            // 显示参数信息
            showParameterInfo();
            parameterText.setVisibility(View.VISIBLE);
            isTextVisible = true;
            handler.post(updateTask); // 开始定时任务

            // 自动隐藏参数信息
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    parameterText.setVisibility(View.GONE);
                    isTextVisible = false;
                    handler.removeCallbacks(updateTask); // 移除定时任务
                }
            }, 3000); // 10秒后自动隐藏
        }
    }


    /**
     * 显示参数信息
     */
    private void showParameterInfo() {
        // 示例：从 MainActivity 中获取参数
        String ggaString = MainActivity.getReadGGAString();
        String rmcString = MainActivity.getReadRMCString();

        // 在TextView上展示参数信息
        String info = "GGA: " + ggaString + "\nRMC: " + rmcString;
        parameterText.setText(info);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // 仅当绑定时才实现此方法
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
        handler.removeCallbacks(updateTask); // 移除定时任务
    }
}
