package com.example.lc29h_rtk_app.main_fragment.ntrip_topfragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lc29h_rtk_app.MainActivity;
import com.example.lc29h_rtk_app.R;
import com.example.lc29h_rtk_app.WebSocketService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Ntrip_top1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Ntrip_top1Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Button ConnectCORSserverButton;
    Button SendCORSHTTPButton;
    Button sendggaButton;
    EditText CORSip;
    EditText CORSport;
    Spinner CORSmount;
    EditText CORSAccount;
    EditText CORSPassword;
    CheckBox RememberTheServerIP;
    CheckBox RememberTheCORSInformation;


    String MountPoint=" ";

    boolean NtripStartFlag= false;


    private WebSocketServiceReceiver webSocketReceiver;

    // Timer variables
    private Handler handler;
    private Runnable timerRunnable;
    private static final int TIMER_INTERVAL = 1000; // 1 seconds


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Ntrip_top1Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NtripFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Ntrip_top1Fragment newInstance(String param1, String param2) {
        Ntrip_top1Fragment fragment = new Ntrip_top1Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Initialize Handler
        handler = new Handler(Looper.getMainLooper());

        // Define the task to be run periodically
        timerRunnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                // Your periodic task
                String message = MainActivity.getReadGGAString() + "\r\n";
//                String messageWebGGA = MainActivity.getReadGGAString();
//                String messageWebRMC = MainActivity.getReadRMCString();

                if (MainActivity.isBound) {
                    if(message.length()>50 && NtripStartFlag){
                        MainActivity.socketService.sendMessage(message);
//
//                        JSONObject data = new JSONObject();
//                        String[] variables = {"GGA"};
//
//                        try {
//                            data.put(variables[0], messageWebGGA);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        String jsonMessage = data.toString();
//
//                        Intent intent = new Intent("SendWebSocketMessage");
//                        intent.putExtra("message", jsonMessage);
//                        requireContext().sendBroadcast(intent);
//
//
//                        JSONObject data1 = new JSONObject();
//                        String[] variables1 = {"RMC"};
//
//                        try {
//                            data1.put(variables1[0], messageWebRMC);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        jsonMessage = data1.toString();
//
//                        intent = new Intent("SendWebSocketMessage");
//                        intent.putExtra("message", jsonMessage);
//                        requireContext().sendBroadcast(intent);
                    }
                }

                // Repeat the task every TIMER_INTERVAL milliseconds
                handler.postDelayed(this, TIMER_INTERVAL);
            }
        };
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ntrip_top1, container, false);

        ConnectCORSserverButton = view.findViewById(R.id.ConnectCORSserverButton);
        SendCORSHTTPButton = view.findViewById(R.id.SendCORSHTTPButton);
        sendggaButton = view.findViewById(R.id.sendggaButton);
        CORSip = view.findViewById(R.id.CORSip);
        CORSport = view.findViewById(R.id.CORSport);
        CORSmount = view.findViewById(R.id.CORSmount);
        CORSAccount = view.findViewById(R.id.CORSAccount);
        CORSPassword = view.findViewById(R.id.CORSPassword);
        RememberTheServerIP = view.findViewById(R.id.RememberTheServerIP);
        RememberTheCORSInformation = view.findViewById(R.id.RememberTheCORSInformation);



        // 文件名
        String fileName = "RememberTheServerIPFile";
        // 获取应用的文件目录
        File file = new File(getActivity().getFilesDir(), fileName);

        if (!file.exists()) {
            // 文件不存在，创建并写入内容
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("1:1");  // 写入文本内容
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 文件存在，检查是否为空
            if (file.length() == 0) {
                // 文件为空，写入内容
                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write("1:1");  // 写入文本内容
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // 文件不为空，执行其他操作或不操作
//                Toast.makeText(getActivity(), "File already contains content", Toast.LENGTH_SHORT).show();
            }
        }

        // 文件名
        fileName = "RememberTheCORSInformationFile";
        // 获取应用的文件目录
        file = new File(getActivity().getFilesDir(), fileName);

        if (!file.exists()) {
            // 文件不存在，创建并写入内容
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);
                writer.write("1:1");  // 写入文本内容
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 文件存在，检查是否为空
            if (file.length() == 0) {
                // 文件为空，写入内容
                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write("1:1");  // 写入文本内容
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // 文件不为空，执行其他操作或不操作
//                Toast.makeText(getActivity(), "File already contains content", Toast.LENGTH_SHORT).show();
            }
        }



        FileInputStream fis = null;
        try {
            fis = getActivity().openFileInput("RememberFlag1");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            // Read each line from the file
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            // Show the contents in a Toast message
            String fileContents = sb.toString();
            if(fileContents.equals("1")){
                RememberTheServerIP.setChecked(true);
                readFillIPportFile("RememberTheServerIPFile" + ".csv",1);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fis = null;
        try {
            fis = getActivity().openFileInput("RememberFlag2");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            // Read each line from the file
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            // Show the contents in a Toast message
            String fileContents = sb.toString();
            if(fileContents.equals("1")){
                RememberTheCORSInformation.setChecked(true);
                readFillIPportFile("RememberTheCORSInformationFile" + ".csv",2);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        RememberTheServerIP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isCheck1 = RememberTheServerIP.isChecked();

                String sIP = CORSip.getText().toString().trim();
                String sPort = CORSport.getText().toString().trim();

                if(isCheck1){
                    if(TextUtils.isEmpty(sIP) || TextUtils.isEmpty(sPort)){

                    }else{
                        StringBuilder Data = new StringBuilder();
                        Data.append("1");
                        FileOutputStream fos = null;
                        try {
                            fos = getActivity().openFileOutput("RememberFlag1", Context.MODE_PRIVATE);
                            fos.write(Data.toString().getBytes());
                            //Toast.makeText(this, "CSV file saved successfully", Toast.LENGTH_SHORT).show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (fos != null) {
                                    fos.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        writeFileIPport("RememberTheServerIPFile" + ".csv", CORSip.getText().toString(),CORSport.getText().toString());
                    }
                }else {
                    StringBuilder Data = new StringBuilder();
                    Data.append("0");
                    FileOutputStream fos = null;
                    try {
                        fos = getActivity().openFileOutput("RememberFlag1", Context.MODE_PRIVATE);
                        fos.write(Data.toString().getBytes());
                        //Toast.makeText(this, "CSV file saved successfully", Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        RememberTheCORSInformation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isCheck = RememberTheCORSInformation.isChecked();
                String sAccount = CORSAccount.getText().toString().trim();
                String sPassword = CORSPassword.getText().toString().trim();

                if(isCheck){
                    if(TextUtils.isEmpty(sAccount) || TextUtils.isEmpty(sPassword)){

                    }else{
                        StringBuilder Data = new StringBuilder();
                        Data.append("1");
                        FileOutputStream fos = null;
                        try {
                            fos = getActivity().openFileOutput("RememberFlag2", Context.MODE_PRIVATE);
                            fos.write(Data.toString().getBytes());
                            //Toast.makeText(this, "CSV file saved successfully", Toast.LENGTH_SHORT).show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (fos != null) {
                                    fos.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        writeFileAccountPassword("RememberTheCORSInformationFile" + ".csv", CORSAccount.getText().toString(),CORSPassword.getText().toString());
                    }
                }else {
                    StringBuilder Data = new StringBuilder();
                    Data.append("0");
                    FileOutputStream fos = null;
                    try {
                        fos = getActivity().openFileOutput("RememberFlag2", Context.MODE_PRIVATE);
                        fos.write(Data.toString().getBytes());
                        //Toast.makeText(this, "CSV file saved successfully", Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        ConnectCORSserverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = CORSip.getText().toString();
                String portString = CORSport.getText().toString();
                if (ipAddress.isEmpty() || portString.isEmpty()) {
                    MainActivity.showToast(getActivity(), "请输入IP地址");
                } else {
                    int portNumber = Integer.parseInt(portString);
                    if (MainActivity.isBound) {
                        MainActivity.socketService.connectToServer(ipAddress, portNumber);
                    }
                }
            }
        });

        SendCORSHTTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.getBluetoothConFlag()){
                    String originalInput = CORSAccount.getText().toString() + ":" + CORSPassword.getText().toString();
                    // 编码字符串
                    String encodedString = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
                    }

                    String message = "GET /" +
                            MountPoint +
                            " HTTP/1.0\r\nUser-Agent: NTRIP GNSSInternetRadio/1.4.10\r\nAccept: */*\r\nConnection: close\r\nAuthorization: Basic " +
                            encodedString +
                            "\r\n\r\n";

                    if (MainActivity.isBound) {
                        MainActivity.socketService.sendMessage(message);
                    }
                }else{
                    MainActivity.showToast(getActivity(),"蓝牙未连接");
                }
            }
        });

        sendggaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = MainActivity.getReadGGAString() + "\r\n";

//                showGGA.setText(message);

                if (MainActivity.isBound) {
                    MainActivity.socketService.sendMessage(message);
                }

                NtripStartFlag=!NtripStartFlag;
            }
        });

        // 创建一个选项列表（数据源）
        List<String> categories = new ArrayList<>();
//        categories.add("选项 1");
//        categories.add("选项 2");
//        categories.add("选项 3");

        // 创建一个适配器（Adapter），用于将数据与 Spinner 关联起来
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // 获取默认视图
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                // 设置字体大小
                textView.setTextSize(16); // 根据需要调整字体大小
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                // 获取默认下拉视图
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                // 设置字体大小
                textView.setTextSize(16); // 根据需要调整字体大小
                return view;
            }
        };

        // 设置下拉列表框的样式
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 将适配器设置到 Spinner
        CORSmount.setAdapter(dataAdapter);
        // 设置 Spinner 的选择监听器
        CORSmount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 当用户选择某个选项时触发
                String item = parent.getItemAtPosition(position).toString();
                MountPoint = item;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 当没有选项被选择时触发

            }
        });

        // 根据 Port 的值更新 Spinner 的选项内容
        updateSpinnerOptions();

        webSocketReceiver = new WebSocketServiceReceiver();

        return view;
    }

    // 更新 Spinner 的选项内容方法
    private void updateSpinnerOptions() {
        // 根据 Port 的值选择合适的数据源
        List<String> currentCategories = new ArrayList<>();

        currentCategories.add("RTCM33_GRCEpro");//9
        currentCategories.add("RTCM33_GRCEJ");//5
        currentCategories.add("RTCM33_GRCE");//6
        currentCategories.add("RTCM33_GRC");//7
        currentCategories.add("RTCM30_GR");//8
        currentCategories.add("RTCM32_GGB");//10
        currentCategories.add("RTCM30_GG");//11

        // 更新适配器的数据源，并通知适配器数据已改变
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) CORSmount.getAdapter();
        adapter.clear();
        adapter.addAll(currentCategories);
        adapter.notifyDataSetChanged();

        // 选择数据源之后默认选中第一个
        CORSmount.setSelection(0);
    }

    private void writeFileIPport(String filename, String ip,String port) {
        StringBuilder Data = new StringBuilder();

        Data.append(ip).append(":").append(port);

        FileOutputStream fos = null;
        try {
            fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(Data.toString().getBytes());
            //Toast.makeText(this, "CSV file saved successfully", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void writeFileAccountPassword(String filename, String Account,String Password) {
        StringBuilder Data = new StringBuilder();

        Data.append(Account).append(":").append(Password);

        FileOutputStream fos = null;
        try {
            fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(Data.toString().getBytes());
            //Toast.makeText(this, "CSV file saved successfully", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void readFillIPportFile(String fileName,int num) {
        FileInputStream fis = null;
        try {
            fis = getActivity().openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            // Read each line from the file
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            // Show the contents in a Toast message
            String fileContents = sb.toString();
            String[] strarray=fileContents.split("[:]");
//            MainActivity.showToast(getActivity(),strarray[0] + strarray[1]);

            if(num==1){
                CORSip.setText(strarray[0]);
                CORSport.setText(strarray[1]);
            } else if (num==2) {
                CORSAccount.setText(strarray[0]);
                CORSPassword.setText(strarray[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class WebSocketServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("WebSocketMessage".equals(intent.getAction())) {
//                String message = intent.getStringExtra("message");
//
//                try {
//                    JSONObject jsonObject = new JSONObject(message);
//
//                    if (jsonObject.has("GGA")) {
//                        showGGA.setText(jsonObject.getString("GGA"));
//                        String str = jsonObject.getString("GGA");
//                        if(str.length()>50){
//                            // 分割NMEA语句，获取各字段
//                            String[] fields = str.split(",");
//
//                            MainActivity.setnew_lat(Ntrip_top1Fragment.convertToDecimal(fields[2]));
//                            MainActivity.setnew_lon(Ntrip_top1Fragment.convertToDecimal(fields[4]));
//                        }
//                    } else if (jsonObject.has("RMC")){
//                        DistanceText.setText(jsonObject.getString("RMC"));
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start the timer when the fragment is resumed
        startTimer();
        IntentFilter filter = new IntentFilter("WebSocketMessage");
        requireContext().registerReceiver(webSocketReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop the timer when the fragment is paused to prevent memory leaks
        stopTimer();
        requireContext().unregisterReceiver(webSocketReceiver);
    }
    /**
     * Start the timer to execute the periodic task.
     */
    private void startTimer() {
        handler.postDelayed(timerRunnable, TIMER_INTERVAL);
    }

    /**
     * Stop the timer to prevent further execution.
     */
    private void stopTimer() {
        handler.removeCallbacks(timerRunnable);
    }
}
