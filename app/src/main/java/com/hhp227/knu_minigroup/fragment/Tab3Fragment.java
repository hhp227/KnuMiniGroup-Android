package com.hhp227.knu_minigroup.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.GridView;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.MemberGridAdapter;
import com.hhp227.knu_minigroup.dto.MemberItem;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class Tab3Fragment extends BaseFragment {
    private static final String TAG = "맴버목록";
    private ProgressDialog progressDialog;
    private GridView gridView;
    private MemberGridAdapter memberGridAdapter;
    private List<MemberItem> memberItems;

    public Tab3Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab3, container, false);
        gridView = rootView.findViewById(R.id.gv_member);
        memberItems = new ArrayList<>();
        memberGridAdapter = new MemberGridAdapter(getActivity(), memberItems);
        gridView.setAdapter(memberGridAdapter);

        // Test
        memberItems.add(new MemberItem("테스트", null));
        memberGridAdapter.notifyDataSetChanged();

        return rootView;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return gridView != null && gridView.canScrollVertically(direction);
    }
}
