package com.example.testapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.testapp.R;

public class DistanceFragment extends Fragment {

    TextView off_dist,on_dist,efficiency;
    String dist_opt_off,dist_opt_on;
    private static final String TAG = "DistFragment";
    RelativeLayout distance_fragment_layout;
    int off_meters,on_meters;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.distance_fragment, container, false);

        distance_fragment_layout = rootView.findViewById(R.id.distance_fragment);
        off_dist = rootView.findViewById(R.id.off_dist);
        on_dist  = rootView.findViewById(R.id.on_dist);
        efficiency = rootView.findViewById(R.id.efficiency);

        dist_opt_off = getArguments().getString("dist_opt_off");
        dist_opt_on = getArguments().getString("dist_opt_on");

        off_meters = Integer.parseInt(dist_opt_off);
        on_meters  = Integer.parseInt(dist_opt_on);
        if(on_meters == 0) on_meters = off_meters;

        off_dist.setText(off_meters/1000 + " kms " + off_meters % 1000 + " meters");
        on_dist.setText(on_meters/1000 + " kms " + on_meters % 1000 + " meters");

        Double e = 1 - (double)(on_meters)/(off_meters);
        String eff = String.format("%.2f",e*100);
        efficiency.setText(eff + "%");

        setUI();

        return rootView;
    }

    private void setUI() {
        if(on_meters == 0 || on_meters == off_meters){
            efficiency.setVisibility(View.GONE);
            distance_fragment_layout.setBackgroundResource(R.drawable.yellow_analysis);
        }
        else if(on_meters < off_meters){
            distance_fragment_layout.setBackgroundResource(R.drawable.green_analysis);
        }
        else {
            efficiency.setVisibility(View.GONE);
            distance_fragment_layout.setBackgroundResource(R.drawable.red_distance_analysis);
        }
    }

}
