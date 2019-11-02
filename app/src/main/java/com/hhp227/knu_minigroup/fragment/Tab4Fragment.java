package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;

public class Tab4Fragment extends BaseFragment {

    public Tab4Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab4, container, false);
        return rootView;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return false;
    }
}
