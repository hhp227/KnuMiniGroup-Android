package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.tabs.TabLayout;
import com.hhp227.knu_minigroup.activity.MainActivity;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.FragmentTabsBinding;

import java.util.List;
import java.util.Vector;

public class SeatFragment extends Fragment {
    public static final String TAG = "도서관 좌석";

    private static final String[] TAB_NAMES = {"대구 열람실", "상주 열람실"};

    private FragmentTabsBinding mBinding;

    public static SeatFragment newInstance() {
        return new SeatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTabsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Fragment[] fragments = new Fragment[] {DaeguSeatFragment.newInstance(), SangjuSeatFragment.newInstance()};
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return fragments.length;
            }
        };

        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.library_seat));
        for (String s : TAB_NAMES)
            mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(s));
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mBinding.viewPager));
        mBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabLayout));
        mBinding.viewPager.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.viewPager.clearOnPageChangeListeners();
        mBinding.tabLayout.clearOnTabSelectedListeners();
        mBinding.tabLayout.removeAllTabs();
        mBinding = null;
    }
}
