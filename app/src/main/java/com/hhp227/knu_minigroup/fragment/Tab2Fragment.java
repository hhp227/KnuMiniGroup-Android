package com.hhp227.knu_minigroup.fragment;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class Tab2Fragment extends BaseFragment {

    public Tab2Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab2, container, false);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return false;
    }
}
