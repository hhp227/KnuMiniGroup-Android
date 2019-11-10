package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hhp227.knu_minigroup.R;

public class MealFragment extends Fragment {

    public MealFragment() {
    }

    public static MealFragment newInstance () {
        MealFragment mealFragment = new MealFragment();
        return mealFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);
        return rootView;
    }
}
