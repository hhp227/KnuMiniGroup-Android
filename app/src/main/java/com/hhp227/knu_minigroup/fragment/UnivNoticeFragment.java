package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hhp227.knu_minigroup.R;

public class UnivNoticeFragment extends Fragment {
    public UnivNoticeFragment() {
    }

    public static UnivNoticeFragment newInstance() {
        UnivNoticeFragment fragment = new UnivNoticeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_univ_notice, container, false);
        return rootView;
    }
}
