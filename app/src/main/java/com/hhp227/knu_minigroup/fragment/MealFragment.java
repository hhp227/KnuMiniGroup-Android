package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.hhp227.knu_minigroup.MainActivity;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.FragmentTabsBinding;

import java.util.List;
import java.util.Vector;

public class MealFragment extends Fragment {
    public static final String TAG = "식단표";
    private static final String[] TAB_NAMES = {"GP감꽃푸드코트", "GP일청담", "공학관 교직원식당", "공학관 학생식당", "복지관 교직원식당", "복지관 학생식당", "복현회관 교직원식당", "복현회관 학생식당", "정보센터", "상주 학식", "문화관", "BTL", "상주생활관"};

    private AppCompatActivity mActivity;

    private FragmentTabsBinding mBinding;

    public MealFragment() {
    }

    public static MealFragment newInstance() {
        return new MealFragment();
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
        mActivity = (AppCompatActivity) getActivity();

        if (mActivity != null) {
            mActivity.setTitle(getString(R.string.meal));
            mActivity.setSupportActionBar(mBinding.toolbar);
        }
        setDrawerToggle();
        fragments.add(StudentMealFragment.newInstance(46));
        fragments.add(StudentMealFragment.newInstance(57));
        fragments.add(StudentMealFragment.newInstance(85));
        fragments.add(StudentMealFragment.newInstance(86));
        fragments.add(StudentMealFragment.newInstance(36));
        fragments.add(StudentMealFragment.newInstance(37));
        fragments.add(StudentMealFragment.newInstance(39));
        fragments.add(StudentMealFragment.newInstance(56));
        fragments.add(StudentMealFragment.newInstance(35));
        fragments.add(StudentMealFragment.newInstance(49));
        fragments.add(new DCDormMealFragment());
        fragments.add(new BTLDormMealFragment());
        fragments.add(new SCDormMealFragment());
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

    private void setDrawerToggle() {
        DrawerLayout drawerLayout = ((MainActivity) mActivity).mBinding.drawerLayout;
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, drawerLayout, mBinding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }
}
