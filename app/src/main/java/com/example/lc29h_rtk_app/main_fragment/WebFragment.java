package com.example.lc29h_rtk_app.main_fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.lc29h_rtk_app.BtThread.ConnectThread;
import com.example.lc29h_rtk_app.BtThread.ConnectedThread;
import com.example.lc29h_rtk_app.R;
import com.example.lc29h_rtk_app.main_fragment.bluetooth_topfragment.Bluetooth_top1Fragment;
import com.example.lc29h_rtk_app.main_fragment.bluetooth_topfragment.Bluetooth_top2Fragment;
import com.example.lc29h_rtk_app.main_fragment.web_topFragment.Web_top1Fragment;
import com.example.lc29h_rtk_app.main_fragment.web_topFragment.Web_top2Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WebFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private BottomNavigationView mNavigationView;
    private FragmentManager mFragmentManager;
    private int lastFragment;
    private Fragment[] fragments;

    public static ConnectThread connectThread;
    public static ConnectedThread connectedThread;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public WebFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WebFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WebFragment newInstance(String param1, String param2) {
        WebFragment fragment = new WebFragment();
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
        View view = inflater.inflate(R.layout.fragment_web, container, false);

        // Initialize BottomNavigationView
        mNavigationView = view.findViewById(R.id.main_web_top_navigation_bar);

        // Initialize fragments and listeners
        initFragment();
        initListener();

        // Delay loading other fragments to avoid crash
        new Handler().postDelayed(this::loadOtherFragments, 500);

        return view;
    }

    // Initialize and add the fragments to the FragmentManager
    private void initFragment() {
        Web_top1Fragment mWeb_top1Fragment = new Web_top1Fragment();
        Web_top2Fragment mWeb_top2Fragment = new Web_top2Fragment();

        // Store fragments in an array
        fragments = new Fragment[]{mWeb_top1Fragment, mWeb_top2Fragment};

        // Get the FragmentManager
        mFragmentManager = getChildFragmentManager();

        // Show the first fragment by default
        lastFragment = 0;
        mFragmentManager.beginTransaction()
                .replace(R.id.main_web_top_page_controller, mWeb_top1Fragment)
                .show(mWeb_top1Fragment)
                .commit();
    }

    // Initialize the BottomNavigationView listener
    private void initListener() {
        mNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.web_top1) {
                    if (lastFragment != 0) {
                        switchFragment(lastFragment, 0);
                        lastFragment = 0;
                    }
                    return true;
                } else if (i == R.id.web_top2) {
                    if (lastFragment != 1) {
                        switchFragment(lastFragment, 1);
                        lastFragment = 1;
                    }
                    return true;
                }
                return false;
            }
        });
    }

    // Switch between fragments
    private void switchFragment(int lastFragment, int index) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.hide(fragments[lastFragment]);
        if (!fragments[index].isAdded()) {
            transaction.add(R.id.main_web_top_page_controller, fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }

    // Load other fragments and hide them
    private void loadOtherFragments() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (int i = 1; i < fragments.length; i++) {
            if (!fragments[i].isAdded()) {
                transaction.add(R.id.main_web_top_page_controller, fragments[i]);
                transaction.hide(fragments[i]);
            }
        }
        transaction.commitAllowingStateLoss();
    }
}