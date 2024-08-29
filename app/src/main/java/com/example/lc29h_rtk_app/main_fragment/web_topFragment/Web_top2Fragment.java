package com.example.lc29h_rtk_app.main_fragment.web_topFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.lc29h_rtk_app.R;

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
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to open the URL in a browser
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://47.109.46.41/app-release.apk"));
                startActivity(intent);
            }
        });

        return view;
    }
}
