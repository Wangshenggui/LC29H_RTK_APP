package com.example.lc29h_rtk_app.BtThread;

import android.bluetooth.BluetoothSocket;
import android.widget.Toast;


import com.example.lc29h_rtk_app.MainActivity;
import com.example.lc29h_rtk_app.main_fragment.bluetooth_topfragment.Bluetooth_top1Fragment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Handler;

//连接了蓝牙设备建立通信之后的数据交互线程类
public class ConnectedThread extends Thread{

    BluetoothSocket bluetoothSocket=null;

    private Handler handler;


    int[] lastData=new int[]{0,0};
    public ConnectedThread(BluetoothSocket bluetoothSocket){
        this.bluetoothSocket=bluetoothSocket;
        //先新建暂时的Stream
        InputStream inputTemp=null;
        OutputStream outputTemp=null;
        try {
            inputTemp=this.bluetoothSocket.getInputStream();
            outputTemp=this.bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            try {
                bluetoothSocket.close();//出错就关闭线程吧
            } catch (IOException ex) {}
        }

        MainActivity.inputStream=inputTemp;
        MainActivity.outputStream=outputTemp;
    }

    public void run() {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(MainActivity.inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while (true) {
                // Read from the input stream
                bytesRead = bufferedInputStream.read(buffer);

                if (bytesRead == -1) {
                    break; // End of stream reached
                }

                // Write the received data to a ByteArrayOutputStream
                byteArrayOutputStream.write(buffer, 0, bytesRead);

                // Convert the ByteArrayOutputStream to a string for easier manipulation
                String receivedData = byteArrayOutputStream.toString("UTF-8");

                // Check if we have a complete $GNGGA frame
                int gnggaIndex = receivedData.indexOf("$GNGGA");
                // Check if we have a complete $GNRMC frame
                int gnrmcIndex = receivedData.indexOf("$GNRMC");

                if (gnggaIndex != -1) {
                    // Find the end of the frame, assuming a newline character
                    int gnggaEndIndex = receivedData.indexOf("\r\n", gnggaIndex);

                    if (gnggaEndIndex != -1) {
                        String gnggaFrame = receivedData.substring(gnggaIndex, gnggaEndIndex);
                        String string = new String(gnggaFrame.getBytes());


                        MainActivity.setReadGGAString(string);

                        byteArrayOutputStream.reset();
                        receivedData = receivedData.substring(gnggaEndIndex + 2); // Skip \r\n
                        byteArrayOutputStream.write(receivedData.getBytes("UTF-8"));
                    }
                }
                if (gnrmcIndex != -1) {
                    // Find the end of the frame, assuming a newline character
                    int gnrmcEndIndex = receivedData.indexOf("\r\n", gnrmcIndex);

                    if (gnrmcEndIndex != -1) {
                        String gnrmcFrame = receivedData.substring(gnrmcIndex, gnrmcEndIndex);
                        String string = new String(gnrmcFrame.getBytes());


                        MainActivity.setReadRMCString(string);

                        byteArrayOutputStream.reset();
                        receivedData = receivedData.substring(gnrmcEndIndex + 2); // Skip \r\n
                        byteArrayOutputStream.write(receivedData.getBytes("UTF-8"));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void btWriteInt(int[] intData){
        for(int sendInt:intData){
            try {
                MainActivity.outputStream.write(sendInt);
            } catch (IOException e) {}
        }
    }

    //自定义的发送字符串的函数
    public void btWriteString(String string){
        for(byte sendData:string.getBytes()){
            try {
                MainActivity.outputStream.write(sendData);//outputStream发送字节的函数
            } catch (IOException e) {}
        }
    }

    //自定义的关闭Socket线程的函数
    public void cancel(){
        try {
            bluetoothSocket.close();
        } catch (IOException e) {}
    }
}
