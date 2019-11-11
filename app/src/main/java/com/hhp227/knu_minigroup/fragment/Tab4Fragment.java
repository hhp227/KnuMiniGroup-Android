package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;
import com.hhp227.knu_minigroup.user.User;

public class Tab4Fragment extends BaseFragment implements View.OnClickListener {
    TextView name, knuId;

    public Tab4Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab4, container, false);
        name = rootView.findViewById(R.id.tv_name);
        knuId = rootView.findViewById(R.id.tv_knu_id);

        User user = app.AppController.getInstance().getPreferenceManager().getUser();
        String strKnuId = user.getUserId();

        knuId.setText(strKnuId);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return false;
    }
}
