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
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.activity.MainActivity;
import com.hhp227.knu_minigroup.databinding.FragmentTabsBinding;

import java.util.List;
import java.util.Vector;

public class TimetableFragment extends Fragment {
    public static final String TAG = "시간표";
    private static final String[] TAB_NAMES = {"학기시간표", "모의시간표 작성"};

    private FragmentTabsBinding mBinding;

    public TimetableFragment() {
    }

    public static TimetableFragment newInstance() {
        return new TimetableFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTabsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final List<Fragment> fragments = new Vector<>();
        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };

        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.timetable));
        fragments.add(SemesterTimeTableFragment.newInstance());
        fragments.add(MockTimeTableFragment.newInstance());
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
