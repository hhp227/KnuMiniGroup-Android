package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.ui.tabhostviewpager.FakeContent;
import com.hhp227.knu_minigroup.ui.tabhostviewpager.TabsPagerAdapter;

import java.util.List;
import java.util.Vector;

public class SeatFragment extends Fragment {
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private android.widget.TabHost tabHost;

    public SeatFragment() {
    }

    public static SeatFragment newInstance() {
        SeatFragment fragment = new SeatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);
        String[] tabNames = {"대구 열람실", "상주 열람실"};
        tabHost = rootView.findViewById(android.R.id.tabhost);
        viewPager = rootView.findViewById(R.id.view_pager);
        tabHost.setup();
        for (int i = 0; i < tabNames.length; i++) {
            TabHost.TabSpec tabSpec;
            tabSpec = tabHost.newTabSpec(tabNames[i]);
            tabSpec.setIndicator(tabNames[i]);
            tabSpec.setContent(new FakeContent(getActivity()));
            tabHost.addTab(tabSpec);
        }
        tabHost.setOnTabChangedListener(new android.widget.TabHost.OnTabChangeListener() {
            // 탭호스트 리스너
            @Override
            public void onTabChanged(String tabId) {
                int selectedItem = tabHost.getCurrentTab();
                viewPager.setCurrentItem(selectedItem);
            }
        });
        List<Fragment> fragments = new Vector<>();
        fragments.add(new DaeguSeatFragment());
        fragments.add(new SangjuSeatFragment());
        mAdapter = new TabsPagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            // 뷰페이져 리스너
            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageSelected(int selectedItem) {
                tabHost.setCurrentTab(selectedItem);
            }
        });

        return rootView;
    }
}
