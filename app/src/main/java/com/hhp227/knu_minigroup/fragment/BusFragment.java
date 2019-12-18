package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TabHost;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.ui.tabhostviewpager.FakeContent;
import com.hhp227.knu_minigroup.ui.tabhostviewpager.TabsPagerAdapter;

import java.util.List;
import java.util.Vector;

public class BusFragment extends Fragment {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private android.widget.TabHost tabHost;

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
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);
        tabHost = rootView.findViewById(android.R.id.tabhost);
        viewPager = rootView.findViewById(R.id.view_pager);
        tabHost.setup();
        String[] tabNames = {"학교(대구)", "학교(상주)", "시외(대구→상주)"};
        for (int i = 0; i < tabNames.length; i++) {
            TabHost.TabSpec tabSpec;
            tabSpec = tabHost.newTabSpec(tabNames[i]);
            tabSpec.setIndicator(tabNames[i]);
            tabSpec.setContent(new FakeContent(getActivity()));
            tabHost.addTab(tabSpec);
        }
        tabHost.setOnTabChangedListener(new android.widget.TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                int selectedItem = tabHost.getCurrentTab();
                viewPager.setCurrentItem(selectedItem);
            }
        });
        viewPager.setOffscreenPageLimit(tabNames.length);
        List<Fragment> fragments = new Vector<>();
        fragments.add(new DCShuttleScheduleFragment());
        fragments.add(new SCShuttleScheduleFragment());
        fragments.add(new InterCityFragment());
        mAdapter = new TabsPagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int selectedItem) {
                tabHost.setCurrentTab(selectedItem);
            }
        });
        return rootView;
    }
}
