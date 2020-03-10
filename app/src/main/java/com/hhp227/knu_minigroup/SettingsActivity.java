package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import com.hhp227.knu_minigroup.fragment.DefaultSettingFragment;
import com.hhp227.knu_minigroup.fragment.MemberManagementFragment;
import com.hhp227.knu_minigroup.fragment.Tab1Fragment;
import com.hhp227.knu_minigroup.fragment.Tab2Fragment;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;
import com.hhp227.knu_minigroup.ui.tabhostviewpager.FakeContent;
import com.hhp227.knu_minigroup.ui.tabhostviewpager.TabsPagerAdapter;

import java.util.List;
import java.util.Vector;

public class SettingsActivity extends FragmentActivity {
    private static final String[] TAB_NAMES = {"회원관리", "기본정보"};
    private TabHost mTabHost;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        String groupId = getIntent().getStringExtra("grp_id");
        String groupImage = getIntent().getStringExtra("grp_img");
        String key = getIntent().getStringExtra("key");
        ActionBar actionBar = getActionBar();
        List<Fragment> fragments = new Vector<>();
        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager(), fragments);
        mTabHost = findViewById(android.R.id.tabhost);
        mViewPager = findViewById(R.id.view_pager);

        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("소모임 설정");
            actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
                @Override
                public boolean isLayoutRtl() {
                    return false;
                }
            });
        }
        mViewPager.setOffscreenPageLimit(TAB_NAMES.length);
        mTabHost.setup();
        for (String tabName : TAB_NAMES) {
            TabHost.TabSpec tabSpec;
            tabSpec = mTabHost.newTabSpec(tabName);
            tabSpec.setIndicator(tabName);
            tabSpec.setContent(new FakeContent(this));
            mTabHost.addTab(tabSpec);
        }
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                mViewPager.setCurrentItem(mTabHost.getCurrentTab());
            }
        });
        fragments.add(MemberManagementFragment.newInstance(groupId));
        fragments.add(DefaultSettingFragment.newInstance(groupId, groupImage, key));
        mViewPager.setAdapter(tabsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTabHost.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.clearOnPageChangeListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
