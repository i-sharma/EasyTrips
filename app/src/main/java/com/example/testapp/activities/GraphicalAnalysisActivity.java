package com.example.testapp.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.testapp.R;
import com.example.testapp.adapters.TabAdapter;
import com.example.testapp.fragment.DistanceFragment;
import com.example.testapp.fragment.TimeFragment;
import com.example.testapp.utils.MapsDataParser;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class GraphicalAnalysisActivity extends AppCompatActivity {

    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private long dist_opt_off=0,dist_opt_on=0,time_opt_off=0,time_opt_on=0;
    private String opt_on="",opt_off="";
    private static final String TAG = "GraphicalAnalysis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphical_analysis);
        getDistanceAndTime();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        TimeFragment timeFragment = new TimeFragment();
        Bundle bundle_time = new Bundle();
        bundle_time.putString("time_opt_off",time_opt_off+"");
        bundle_time.putString("time_opt_on",time_opt_on+"");
        timeFragment.setArguments(bundle_time);

        DistanceFragment distanceFragment = new DistanceFragment();
        Bundle bundle_dist = new Bundle();
        bundle_dist.putString("dist_opt_off",dist_opt_off+"");
        bundle_dist.putString("dist_opt_on",dist_opt_on+"");
        distanceFragment.setArguments(bundle_dist);

        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(timeFragment, "Time-wise");
        adapter.addFragment(distanceFragment, "Distance-wise");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        setIcon();

}

    private void setIcon() {

        int[] tabIcons = {
                R.drawable.clock_icon,
                R.drawable.distance_icon,
        };

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);

    }

    private void getDistanceAndTime() {
        JSONObject jObject_on,jObject_off;
        if(opt_off.isEmpty() && opt_on.isEmpty()){
            loadApiResult(false);
            loadApiResult(true);
        }
        try {
            jObject_off = new JSONObject(opt_off);
            MapsDataParser parser_off = new MapsDataParser(jObject_off);
            dist_opt_off = parser_off.getTotalDistanceAndTime().get(0);
            time_opt_off = parser_off.getTotalDistanceAndTime().get(1);
            //Log.d(TAG, "getDistanceAndTime: opt_off is" + opt_off);

            if(!opt_on.isEmpty()){
                jObject_on = new JSONObject(opt_on);
                MapsDataParser parser_on = new MapsDataParser(jObject_on);
                dist_opt_on = parser_on.getTotalDistanceAndTime().get(0);
                time_opt_on = parser_on.getTotalDistanceAndTime().get(1);
            }

            Log.d(TAG, "off distance is "+dist_opt_off);
            Log.d(TAG, "on distance is "+dist_opt_on);
            Log.d(TAG, "off time is "+time_opt_off);
            Log.d(TAG, "on time is "+time_opt_on);
        }catch (Exception e)    {
            e.printStackTrace();
        }
    }

    private void loadApiResult(Boolean opt) {
        if (opt) {
            try {
                File file = new File(getDir("apiResponse", MODE_PRIVATE), "opt_true");
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                opt_on = (String) ois.readObject();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                opt_on = "";
            }
        }else{
            try {
                File file = new File(getDir("apiResponse", MODE_PRIVATE), "opt_false");
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                opt_off = (String) ois.readObject();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                opt_off = "";
            }
        }
    }

}
