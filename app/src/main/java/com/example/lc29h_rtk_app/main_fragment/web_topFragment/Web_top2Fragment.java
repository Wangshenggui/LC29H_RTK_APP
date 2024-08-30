package com.example.lc29h_rtk_app.main_fragment.web_topFragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.example.lc29h_rtk_app.MainActivity;
import com.example.lc29h_rtk_app.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Web_top2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Web_top2Fragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Web_top2Fragment() {
        // Required empty public constructor
    }

    public static Web_top2Fragment newInstance(String param1, String param2) {
        Web_top2Fragment fragment = new Web_top2Fragment();
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
        View view = inflater.inflate(R.layout.fragment_web_top2, container, false);

        Button downloadButton = view.findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(v -> checkForUpdate());

        return view;
    }

    private void checkForUpdate() {
        new Thread(() -> {
            try {
                // 获取服务器上的版本号
                URL url = new URL("http://47.109.46.41/app/version.txt"); // 替换为实际的 URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String serverVersion = reader.readLine().trim(); // 获取版本号
                reader.close();

                // 获取当前应用的版本号
                String currentVersion = getCurrentAppVersion();

                // 对比版本号
                if (currentVersion != null && compareVersionStrings(currentVersion, serverVersion) < 0) {
                    // 当前版本小于服务器版本，提示用户更新
                    requireActivity().runOnUiThread(() -> showUpdateDialog(serverVersion));
                } else {
                    // 当前版本已是最新
                    requireActivity().runOnUiThread(() -> MainActivity.showToast(getActivity(), "当前已是最新版本"));
                }

            } catch (IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> MainActivity.showToast(getActivity(), "检查更新失败"));
            }
        }).start();
    }

    private String getCurrentAppVersion() {
        try {
            PackageManager packageManager = requireContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(requireContext().getPackageName(), 0);
            return packageInfo.versionName;  // 获取应用的版本名
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showUpdateDialog(String serverVersion) {
        new AlertDialog.Builder(requireContext())
                .setTitle("更新可用")
                .setMessage("发现新版本 " + serverVersion + "，是否下载更新？")
                .setPositiveButton("下载", (dialog, which) -> startDownload())
                .setNegativeButton("取消", null)
                .show();
    }

    private void startDownload() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://47.109.46.41/app/app-release.apk")); // 替换为实际的 APK URL
        startActivity(intent);
    }

    private int compareVersionStrings(String version1, String version2) {
        String[] version1Parts = version1.split("\\.");
        String[] version2Parts = version2.split("\\.");

        for (int i = 0; i < Math.min(version1Parts.length, version2Parts.length); i++) {
            int v1 = Integer.parseInt(version1Parts[i]);
            int v2 = Integer.parseInt(version2Parts[i]);

            if (v1 != v2) {
                return Integer.compare(v1, v2);
            }
        }

        return Integer.compare(version1Parts.length, version2Parts.length);
    }
}
