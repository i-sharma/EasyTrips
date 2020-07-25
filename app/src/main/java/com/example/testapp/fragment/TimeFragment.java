package com.example.testapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.example.testapp.R;

public class TimeFragment extends Fragment {

    TextView off_time,on_time,efficiency;
    String time_opt_off,time_opt_on;
    private static final String TAG = "TimeFragment";
    RelativeLayout time_fragment_layout;
    int off_seconds,on_seconds;
    Double e;
    private static final Double THRESHOLD = 0.05;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.time_fragment, container, false);

        time_fragment_layout = rootView.findViewById(R.id.time_fragment);
        off_time = rootView.findViewById(R.id.off_time);
        on_time  = rootView.findViewById(R.id.on_time);
        efficiency = rootView.findViewById(R.id.efficiency);
        time_opt_off = getArguments().getString("time_opt_off");
        time_opt_on = getArguments().getString("time_opt_on");

        off_seconds = Integer.parseInt(time_opt_off);
        int p1 = off_seconds % 60;
        int p2 = off_seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;

        String off_time_text = "";

        if(p2 != 0)
            off_time_text += p2 + " hours ";

        if(p3 != 0)
            off_time_text += p3 + " minutes ";

        if(p1 != 0)
            off_time_text += p1 + " seconds ";

        off_time.setText(off_time_text);

        on_seconds = Integer.parseInt(time_opt_on);
        if(on_seconds == 0) on_seconds = off_seconds;

        p1 = on_seconds % 60;
        p2 = on_seconds / 60;
        p3 = p2 % 60;
        p2 = p2 / 60;

        String on_time_text = "";

        if(p2 != 0)
            on_time_text += p2 + " hours ";

        if(p3 != 0)
            on_time_text += p3 + " minutes ";

        if(p1 != 0)
            on_time_text += p1 + " seconds ";

        on_time.setText(on_time_text);

        e = 1 - (double)(on_seconds)/(off_seconds);
        String eff = String.format("%.2f",e*100);
        String preEff = (String) efficiency.getText();
        preEff += eff;
        efficiency.setText(preEff + "%");

        setUI();

        return rootView;
    }

    void setUI(){
        if(e < THRESHOLD && e > 0){
            efficiency.setVisibility(View.GONE);
            time_fragment_layout.setBackgroundResource(R.drawable.blue_analysis);
        }
        else{
            if(on_seconds == 0 || on_seconds == off_seconds){
                efficiency.setVisibility(View.GONE);
                time_fragment_layout.setBackgroundResource(R.drawable.yellow_analysis);
            }
            else if(on_seconds < off_seconds){
                time_fragment_layout.setBackgroundResource(R.drawable.green_analysis);
            }
            else {
                efficiency.setVisibility(View.GONE);
                time_fragment_layout.setBackgroundResource(R.drawable.red_time_analysis);
            }
        }
    }

}
