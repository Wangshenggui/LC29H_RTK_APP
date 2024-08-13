package com.example.lc29h_rtk_app.main_fragment.bluetooth_topfragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lc29h_rtk_app.R;
import com.example.lc29h_rtk_app.MainActivity;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Bluetooth_top2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Bluetooth_top2Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Handler handler;
    private Runnable updateDataRunnable;
    private static final int UPDATE_INTERVAL = 1000; // 1 second

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Bluetooth_top2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment main_top2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Bluetooth_top2Fragment newInstance(String param1, String param2) {
        Bluetooth_top2Fragment fragment = new Bluetooth_top2Fragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bluetooth_top2, container, false);
        TextView ggaTextView = view.findViewById(R.id.gga_text_view);
        TextView rmcTextView = view.findViewById(R.id.rmc_text_view);

        handler = new Handler();
        updateDataRunnable = new Runnable() {
            @Override
            public void run() {
                updateData(ggaTextView, rmcTextView);
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };

        handler.post(updateDataRunnable);
        return view;
    }
    private void updateData(TextView ggaTextView, TextView rmcTextView) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            String ggaData = activity.getReadGGAString();
            String rmcData = activity.getReadRMCString();
            String[] ggaParts = ggaData.split(",");
            String[] rmcParts = rmcData.split(",");

            if (ggaParts.length >= 15) {
                String ggaInfo = "GGA Data:\n" +
                        "标识符: " + ggaParts[0] + "\n" +
                        "时间: " + ggaParts[1] + "\n" +
                        "纬度: " + ggaParts[2] + " " + ggaParts[3] + "\n" +
                        "经度: " + ggaParts[4] + " " + ggaParts[5] + "\n" +
                        "标志位: " + ggaParts[6] + "\n" +
                        "卫星颗数: " + ggaParts[7] + "\n" +
                        "海拔高度: " + ggaParts[9] + " " + ggaParts[10];
                ggaTextView.setText(ggaInfo);
            } else {
                ggaTextView.setText("Invalid GGA data");
            }

            String rmcInfo = "RMC Data:\n" + String.join(", ", rmcParts);
            rmcTextView.setText(rmcInfo);
        }
    }
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacks(updateDataRunnable);
        }
    }
}