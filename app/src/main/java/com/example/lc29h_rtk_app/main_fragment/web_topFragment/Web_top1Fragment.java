package com.example.lc29h_rtk_app.main_fragment.web_topFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lc29h_rtk_app.MainActivity;
import com.example.lc29h_rtk_app.R;

public class Web_top1Fragment extends Fragment {

    private WebView webView;

    public Web_top1Fragment() {
        // Required empty public constructor
    }

    public static Web_top1Fragment newInstance() {
        return new Web_top1Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_top1, container, false);

        webView = view.findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);  // Enable JavaScript

        // Add JavaScript interface to communicate with the WebView
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // Ensure WebView stays inside the app instead of launching a browser
        webView.setWebViewClient(new WebViewClient());

        // Load the local HTML file
        webView.loadUrl("file:///android_asset/BaiduMap.html");

        return view;
    }

    // JavaScript Interface class to allow communication from the WebView to Android
    public class WebAppInterface {
        @android.webkit.JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
        }

        @android.webkit.JavascriptInterface
        public void sendDataToAndroid(String data) {
            // Log the data received from WebView
            Log.d("WebAppInterface", "Data received from WebView: " + data);
            // Display a toast message with the received data
            MainActivity.showToast(getActivity(),data);
        }
    }

    // Call this method to send data from Android to the WebView
    public void sendDataToWebView(String data) {
        webView.evaluateJavascript("javascript:receiveDataFromAndroid('" + data + "')", null);
    }
}
