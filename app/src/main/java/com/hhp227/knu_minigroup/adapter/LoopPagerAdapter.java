package com.hhp227.knu_minigroup.adapter;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.hhp227.knu_minigroup.databinding.FragmentMainPagerBinding;

import java.util.List;

public class LoopPagerAdapter extends PagerAdapter {
    private final List<String> mPagerItemList;

    private View.OnClickListener mOnClickListener;

    public LoopPagerAdapter(List<String> pagerItemList) {
        this.mPagerItemList = pagerItemList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        FragmentMainPagerBinding binding = FragmentMainPagerBinding.inflate(LayoutInflater.from(container.getContext()), container, false);

        binding.setType(mPagerItemList.get(position));
        binding.setOnClickListener(mOnClickListener);
        binding.executePendingBindings();
        container.addView(binding.getRoot(), 0);
        return binding.getRoot();
    }

    @Override
    public int getCount() {
        return mPagerItemList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view == obj;
    }

    @Override
    public void destroyItem(ViewGroup pager, int position, @NonNull Object view) {
        pager.removeView((View) view);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) { }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) { }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {}

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }
}
