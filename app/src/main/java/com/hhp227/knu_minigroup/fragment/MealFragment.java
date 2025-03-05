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

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

// TODO
public class MealFragment extends Fragment {
    private static final String[] TAB_NAMES = {"GP감꽃푸드코트", "GP일청담", "공학관 교직원식당", "공학관 학생식당", "복지관 교직원식당", "복지관 학생식당", "복현회관 교직원식당", "복현회관 학생식당", "정보센터", "상주 학식", "문화관", "BTL", "상주생활관"};

    private FragmentTabsBinding mBinding;

    public static MealFragment newInstance() {
        return new MealFragment();
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

        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.meal));
        for (int num : new int[] {46, 57, 85, 86, 36, 37, 39, 56, 35, 49}) {
            fragments.add(StudentMealFragment.newInstance(num));
        }
        fragments.addAll(Arrays.asList(DCDormMealFragment.newInstance(), BTLDormMealFragment.newInstance(), SCDormMealFragment.newInstance()));
        for (String s : TAB_NAMES)
            mBinding.tabLayout.addTab(mBinding.tabLayout.newTab().setText(s));
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mBinding.viewPager));
        mBinding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mBinding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabLayout));
        mBinding.viewPager.setAdapter(adapter);
        mBinding.viewPager.setOffscreenPageLimit(fragments.size());
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
