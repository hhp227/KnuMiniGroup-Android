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

public class TimetableFragment extends Fragment {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private android.widget.TabHost tabHost;

    public TimetableFragment() {
    }

    public static TimetableFragment newInstance() {
        TimetableFragment fragment = new TimetableFragment();
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
        String[] tabNames = {"학기시간표", "모의시간표 작성"};
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
        fragments.add(SemesterTimeTableFragment.newInstance());
        fragments.add(MockTimeTableFragment.newInstance());
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
