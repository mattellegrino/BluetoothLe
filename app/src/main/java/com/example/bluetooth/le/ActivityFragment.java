package com.example.bluetooth.le;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActivityFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private String steps;
    private Integer distance;

    public ActivityFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters

    private final BroadcastReceiver NewDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Bundle extras = intent.getExtras();
                if(extras!=null) {
                    if (extras.containsKey("Steps")){
                        steps = extras.get("Steps").toString();
                        TextView textView = (TextView) getView().findViewById(R.id.steps_count);
                        textView.setText(steps);
                    }
                }
            }

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            steps = getArguments().getString("Steps","0");
        }
        else{
            steps = "0";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            steps = getArguments().getString("Steps","0");
        }
        else{
            steps = "0";
        }
        TextView textView = (TextView) getView().findViewById(R.id.steps_count);
        textView.setText(steps);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(NewDataReceiver,makeGattUpdateIntentFilter());

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(NewDataReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}