package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hhp227.knu_minigroup.R;

public class DCShuttleScheduleFragment extends Fragment {

    public static DCShuttleScheduleFragment newInstance() {
        DCShuttleScheduleFragment fragment = new DCShuttleScheduleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shuttle_schedule, container, false);
        return rootView;
    }
}