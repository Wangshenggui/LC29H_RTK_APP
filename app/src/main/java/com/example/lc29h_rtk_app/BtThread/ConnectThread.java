package com.example.lc29h_rtk_app.BtThread;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

//连接蓝牙设备的操作线程类
public class ConnectThread extends Thread{

    public static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//符合UUID格式就行。

    BluetoothDevice bluetoothDevice=null;
    public static BluetoothSocket bluetoothSocket=null;
    public ConnectThread(BluetoothDevice bluetoothDevice){
        this.bluetoothDevice=bluetoothDevice;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        super.run();
        try {
            bluetoothSocket=this.bluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            //连接
            bluetoothSocket.connect();
        } catch (IOException e) {
            try {
                bluetoothSocket.close();//出错就断开连接
            } catch (IOException ex) {}
        }
    }
    //自定义的断开连接
    public void cancel(){
        if(bluetoothSocket!=null){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {}
            bluetoothSocket=null;
        }
    }
}
