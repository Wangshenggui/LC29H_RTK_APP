package com.example.lc29h_rtk_app.main_fragment.web_topFragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.example.lc29h_rtk_app.MainActivity;
import com.example.lc29h_rtk_app.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Web_top1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Web_top1Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private WebView webView;
    Button WebTestButton;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Web_top1Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Web_top1Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Web_top1Fragment newInstance(String param1, String param2) {
        Web_top1Fragment fragment = new Web_top1Fragment();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web_top1, container, false);

        // Initialize WebView
        webView = view.findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/BaiduMap.html"); // 载入网页的URL

        WebTestButton = view.findViewById(R.id.WebTestButton);

        WebTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject data = new JSONObject();
                String[] variables = {"a","b","c"};

                try {
                    data.put(variables[0], 12);
                    data.put(variables[1], 34);
                    data.put(variables[2], 56);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonMessage = data.toString();

                Intent intent = new Intent("SendWebSocketMessage");
                intent.putExtra("message", jsonMessage);
                requireContext().sendBroadcast(intent);
            }
        });

        return view;
    }
    private void putToJsonObject(JSONObject jsonObject, String key, Object value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException("Could not put key '" + key + "' into JSONObject", e);
        }
    }
}