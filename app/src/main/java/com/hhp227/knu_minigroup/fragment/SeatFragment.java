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

public class SeatFragment extends Fragment {
    private TabHost mTabHost;
    private ViewPager mViewPager;

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
        List<Fragment> fragments = new Vector<>();
        String[] tabNames = {"대구 열람실", "상주 열람실"};
        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getChildFragmentManager(), fragments);
        mTabHost = rootView.findViewById(android.R.id.tabhost);
        mViewPager = rootView.findViewById(R.id.view_pager);
        mTabHost.setup();
        for (int i = 0; i < tabNames.length; i++) {
            TabHost.TabSpec tabSpec;
            tabSpec = mTabHost.newTabSpec(tabNames[i]);
            tabSpec.setIndicator(tabNames[i]);
            tabSpec.setContent(new FakeContent(getActivity()));
            mTabHost.addTab(tabSpec);
        }
        mTabHost.setOnTabChangedListener(new android.widget.TabHost.OnTabChangeListener() {
            // 탭호스트 리스너
            @Override
            public void onTabChanged(String tabId) {
                int selectedItem = mTabHost.getCurrentTab();
                mViewPager.setCurrentItem(selectedItem);
            }
        });
        fragments.add(new DaeguSeatFragment());
        fragments.add(new SangjuSeatFragment());
        mViewPager.setAdapter(tabsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                mTabHost.setCurrentTab(selectedItem);
            }
        });

        return rootView;
    }
}
