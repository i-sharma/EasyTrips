package com.example.testapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.testapp.R;

public class TimeFragment extends Fragment {

    TextView off_time,on_time,efficiency;
    String time_opt_off,time_opt_on;
    private static final String TAG = "TimeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.time_fragment, container, false);

        off_time = rootView.findViewById(R.id.off_time);
        on_time  = rootView.findViewById(R.id.on_time);
        efficiency = rootView.findViewById(R.id.efficiency);
        time_opt_off = getArguments().getString("time_opt_off");
        time_opt_on = getArguments().getString("time_opt_on");

        int off_seconds = Integer.parseInt(time_opt_off);
        int p1 = off_seconds % 60;
        int p2 = off_seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;

        off_time.setText("" + p2 + " hours " + p3 + " minutes " + p1 + " seconds");

        int on_seconds = Integer.parseInt(time_opt_on);
        p1 = on_seconds % 60;
        p2 = on_seconds / 60;
        p3 = p2 % 60;
        p2 = p2 / 60;

        on_time.setText("" + p2 + " hours " + p3 + " minutes " + p1 + " seconds");

        Double e = 1 - (double)(on_seconds)/(off_seconds);
        String eff = String.format("%.2f",e*100);
        efficiency.setText(eff + " %");

        return rootView;
    }


}
