package com.example.lc29h_rtk_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class SocketService extends Service {

    private static final String TAG = "SocketService";

    private final IBinder binder = new LocalBinder();
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread socketThread;

    private final ExecutorService executorService = Executors.newCachedThreadPool();  // 线程池管理
    private final ReentrantLock sendlock = new ReentrantLock();  // 发送锁
    private final ReentrantLock readlock = new ReentrantLock();  // 读取锁

    private Toast currentToast;
    private Handler mainHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mainHandler = new Handler(Looper.getMainLooper());
        startForegroundService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "socket_channel",
                    "Socket Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new Notification.Builder(this, "socket_channel")
                .setContentTitle("Socket Service")
                .setContentText("Service is running in the background")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        startForeground(1, notification);
    }

    public void connectToServer(String ipAddress, int port) {
        socketThread = new Thread(() -> {
            while (true) {
                try {
                    socket = new Socket(ipAddress, port);
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();

                    showToast("已连接服务器");
                    //CORS服务器已连接
                    MediaPlayer mediaPlayer = MediaPlayer.create(this,R.raw.the_cors_server_is_connected);
                    mediaPlayer.start();

                    readFromSocket();
                    break;  // 连接成功后退出循环

                } catch (IOException e) {
                    Log.e(TAG, "连接失败: " + e.getMessage());
                    showToast("连接失败，正在重试...");

                    // 等待5秒后重新尝试连接
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        });
        socketThread.start();
    }

    public void sendMessage(String message) {
        sendlock.lock();  // 获取发送锁
        try {
            if (socket != null && outputStream != null) {
                executorService.submit(() -> {
                    try {
                        outputStream.write(message.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    } catch (IOException e) {
                        Log.e(TAG, "消息发送失败: " + e.getMessage());
                        showToast("消息发送失败");
                    }
                });
            } else {
                showToast("套接字未连接");
            }
        } finally {
            sendlock.unlock();  // 释放发送锁
        }
    }

    private void readFromSocket() {
        readlock.lock();  // 获取读取锁
        try {
            if (inputStream != null) {
                try {
                    byte[] buffer = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        byte[] rawMessage = Arrays.copyOf(buffer, bytesRead);

                        // 将字节数组转换为字符串
                        String receivedMessage = new String(rawMessage, StandardCharsets.UTF_8);

                        // 同步发送数据到其他地方 (比如蓝牙设备)
                        MainActivity.outputStream.write(rawMessage);
                        MainActivity.outputStream.flush();

                        // 显示接收的消息长度
                        showToast("Received data, length: " + rawMessage.length);

                        // 定义预期的消息字符串
                        String expectedMessageOK = "ICY 200 OK\r\n";
                        String expectedMessageERROR = "ERROR - Bad Password\r\n";

                        // 比较接收到的消息与预期消息
                        if (receivedMessage.equals(expectedMessageOK)) {
                            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.cors_http_request_succeeded_rocedure);
                            mediaPlayer.start();
                        } else if (receivedMessage.equals(expectedMessageERROR)){
                            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.account_or_password_incorrec);
                            mediaPlayer.start();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "读取失败: " + e.getMessage());
                    showToast("读取失败");
                }
            }
        } finally {
            readlock.unlock();  // 释放读取锁
        }
    }

    private void showToast(final String message) {
        mainHandler.post(() -> {
            if (currentToast != null) {
                currentToast.cancel();  // 取消前一个Toast
            }
            currentToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
            currentToast.show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (socketThread != null && socketThread.isAlive()) {
            socketThread.interrupt();  // 停止Socket线程
        }

        if (executorService != null) {
            executorService.shutdown();  // 关闭线程池
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();  // 关闭Socket连接
                showToast("连接已关闭");
            }
        } catch (IOException e) {
            Log.e(TAG, "关闭套接字失败: " + e.getMessage());
            showToast("关闭套接字失败");
        }
    }
}
