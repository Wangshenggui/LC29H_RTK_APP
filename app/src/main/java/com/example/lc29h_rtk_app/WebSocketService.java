package com.example.lc29h_rtk_app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketService extends Service {

    private static final String CHANNEL_ID = "WebSocketChannel";
    private static final String CHANNEL_NAME = "WebSocket Service Channel";
    private static final int NOTIFICATION_ID = 1;

    private WebSocket webSocket;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String TAG = "WebSocketService";
    private SendMessageReceiver sendMessageReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化WebSocket连接
        reconnectWebSocket();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // 注册广播接收器
        sendMessageReceiver = new SendMessageReceiver();
        IntentFilter filter = new IntentFilter("SendWebSocketMessage");
        registerReceiver(sendMessageReceiver, filter);

        startForegroundService();
    }

    private void reconnectWebSocket() {
        if (webSocket != null) {
            webSocket.cancel(); // 取消当前连接
        }

        // 重新创建WebSocket连接
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://47.109.46.41:8001")
                .build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket connected");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                mainHandler.post(() -> {
                    Log.d(TAG, "Received: " + text);
                    Intent intent = new Intent("WebSocketMessage");
                    intent.putExtra("message", text);
                    sendBroadcast(intent);
                });
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Handle binary message
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
                Log.d(TAG, "WebSocket closing: " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d(TAG, "WebSocket failure", t);
                // 在这里实现重连逻辑
                mainHandler.postDelayed(() -> reconnectWebSocket(), 100); // 5秒后重连
            }
        });
    }

    private void startForegroundService() {
        // 创建前台服务通知
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WebSocket Service")
                .setContentText("Running in background...")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // 使用系统默认小图标
                .build();

        // 启动服务并进入前台状态
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(100, "Service destroyed");
        }
        if (sendMessageReceiver != null) {
            unregisterReceiver(sendMessageReceiver);
        }

        // 使用AlarmManager在服务被销毁时重启服务
        Intent intent = new Intent(this, WebSocketService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, pendingIntent); // 5秒后重启服务
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class SendMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("SendWebSocketMessage".equals(intent.getAction())) {
                String message = intent.getStringExtra("message");
                if (webSocket != null) {
                    webSocket.send(message);
                }
            }
        }
    }
}
