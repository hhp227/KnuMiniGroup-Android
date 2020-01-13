package com.hhp227.knu_minigroup.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TabHost;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;
import com.hhp227.knu_minigroup.ui.scrollable.CanScrollVerticallyDelegate;
import com.hhp227.knu_minigroup.ui.scrollable.ScrollableLayout;
import com.hhp227.knu_minigroup.ui.tabhostviewpager.FakeContent;

import java.util.List;
import java.util.Vector;

public class TabHostLayoutFragment extends Fragment {
    private static final String IS_ADMIN = "admin";
    private static final String GROUP_ID = "grp_id";
    private static final String GROUP_NAME = "grp_nm";
    private static final String POSITION = "position";
    private static final String KEY = "key";
    private static final String[] TAB_NAMES = {"소식", "일정", "맴버", "설정"};
    private TabHost tabHost;
    private TabsPagerAdapter tabsPagerAdapter;
    private ViewPager viewPager;

    private boolean isAdmin;
    private int position;
    private String groupId, groupName, key;

    public TabHostLayoutFragment() {
    }

    public static TabHostLayoutFragment newInstance(boolean isAdmin, String groupId, String groupName, int position, String key) {
        TabHostLayoutFragment fragment = new TabHostLayoutFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_ADMIN, isAdmin);
        args.putString(GROUP_ID, groupId);
        args.putString(GROUP_NAME, groupName);
        args.putInt(POSITION, position);
        args.putString(KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAdmin = getArguments().getBoolean(IS_ADMIN);
            groupId = getArguments().getString(GROUP_ID);
            groupName = getArguments().getString(GROUP_NAME);
            position = getArguments().getInt(POSITION);
            key = getArguments().getString(KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_host_layout, container, false);
        ScrollableLayout scrollableLayout = rootView.findViewById(R.id.scrollable_layout);
        tabHost = rootView.findViewById(android.R.id.tabhost);
        viewPager = rootView.findViewById(R.id.view_pager);

        viewPager.setOffscreenPageLimit(TAB_NAMES.length);
        tabHost.setup();

        for (int i = 0; i < TAB_NAMES.length; i++) {
            TabHost.TabSpec tabSpec;
            tabSpec = tabHost.newTabSpec(TAB_NAMES[i]);
            tabSpec.setIndicator(TAB_NAMES[i]);
            tabSpec.setContent(new FakeContent(getActivity()));
            tabHost.addTab(tabSpec);
            TextView textView = tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
        }

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                int selectedItem = tabHost.getCurrentTab();
                viewPager.setCurrentItem(selectedItem);
            }
        });

        List<BaseFragment> fragments = new Vector<>();
        fragments.add(Tab1Fragment.newInstance(isAdmin, groupId, groupName, key));
        fragments.add(new Tab2Fragment());
        fragments.add(Tab3Fragment.newInstance(groupId));
        fragments.add(Tab4Fragment.newInstance(isAdmin, groupId, position, key));
        tabsPagerAdapter = new TabsPagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(tabsPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            @Override
            public boolean canScrollVertically(int direction) {
                return tabsPagerAdapter.canScrollVertically(viewPager.getCurrentItem(), direction);
            }
        });

        return rootView;
    }

    public class TabsPagerAdapter extends FragmentPagerAdapter {
        private List<BaseFragment> fragments;

        public TabsPagerAdapter(FragmentManager fm, List<BaseFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        // 스크롤 관련
        boolean canScrollVertically(int position, int direction) {
            return getItem(position).canScrollVertically(direction);
        }

        @Override
        public BaseFragment getItem(int position) {
            BaseFragment getItem = fragments.get(position);
            return getItem;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
