package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.hhp227.knu_minigroup.R;

import java.util.List;
import java.util.Vector;

public class BusFragment extends Fragment {
    public static final String TAG = "버스시간표";

    private static final String[] TAB_NAMES = {"학교(대구)", "학교(상주)"/*, "시외(대구→상주)"*/};
    private AppCompatActivity mActivity;
    private DrawerLayout mDrawerLayout;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;
    private ViewPager mViewPager;

    public BusFragment() {
    }

    public static BusFragment newInstance() {
        BusFragment fragment = new BusFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tabs, container, false);
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
        mDrawerLayout = mActivity.findViewById(R.id.drawer_layout);
        mToolbar = view.findViewById(R.id.toolbar);
        mTabLayout = view.findViewById(R.id.tab_layout);
        mViewPager = view.findViewById(R.id.view_pager);

        mActivity.setTitle(getString(R.string.shuttle_bus));
        mActivity.setSupportActionBar(mToolbar);
        setDrawerToggle();
        fragments.add(new DCShuttleScheduleFragment());
        fragments.add(new SCShuttleScheduleFragment());
        for (String s : TAB_NAMES)
            mTabLayout.addTab(mTabLayout.newTab().setText(s));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewPager.clearOnPageChangeListeners();
        mTabLayout.clearOnTabSelectedListeners();
        mTabLayout.removeAllTabs();
    }

    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }
}
