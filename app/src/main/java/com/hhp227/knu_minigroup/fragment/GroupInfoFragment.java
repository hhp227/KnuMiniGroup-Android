package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.hhp227.knu_minigroup.R;

public class GroupInfoFragment extends DialogFragment {

    public static GroupInfoFragment newInstance() {
        Bundle args = new Bundle();

        GroupInfoFragment fragment = new GroupInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        View rootView = inflater.inflate(R.layout.fragment_group_info, container, false);
        return rootView;
    }
}
