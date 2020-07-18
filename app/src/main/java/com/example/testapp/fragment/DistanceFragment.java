package com.example.testapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.testapp.R;

public class DistanceFragment extends Fragment {

    TextView off_dist,on_dist;
    String dist_opt_off,dist_opt_on;
    private static final String TAG = "DistFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.distance_fragment, container, false);

        off_dist = rootView.findViewById(R.id.off_dist);
        on_dist  = rootView.findViewById(R.id.on_dist);
        dist_opt_off = getArguments().getString("dist_opt_off");
        dist_opt_on = getArguments().getString("dist_opt_on");

        int off_meters = Integer.parseInt(dist_opt_off);
        int on_meters  = Integer.parseInt(dist_opt_on);

        off_dist.setText(off_meters/1000 + " kms " + off_meters % 1000 + " meters");
        on_dist.setText(on_meters/1000 + " kms " + on_meters % 1000 + " meters");

        return rootView;
    }

}
