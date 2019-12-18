package com.hhp227.knu_minigroup.ui.tabhostviewpager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class TabsPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public TabsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment getItem = fragments.get(position);
        return getItem;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
