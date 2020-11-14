package com.hhp227.knu_minigroup.adapter;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.hhp227.knu_minigroup.R;

import java.util.List;

public class LoopPagerAdapter extends PagerAdapter {
    private final List<String> mPagerItemList;

    private View.OnClickListener mOnClickListener;

    public LoopPagerAdapter(List<String> pagerItemList) {
        this.mPagerItemList = pagerItemList;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View rootView = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_main_pager, null);
        RelativeLayout rl_main = rootView.findViewById(R.id.rl_type_main);
        RelativeLayout rl_image = rootView.findViewById(R.id.rl_type_image);
        ImageView imageView = rootView.findViewById(R.id.iv_banner);
        TextView textType1 = rootView.findViewById(R.id.tv_type1);
        TextView textType2 = rootView.findViewById(R.id.tv_type2);

        switch (mPagerItemList.get(position)) {
            case "메인":
                Button findGroup = rootView.findViewById(R.id.b_find);
                Button createCroup = rootView.findViewById(R.id.b_create);

                textType1.setVisibility(View.GONE);
                textType2.setVisibility(View.GONE);
                rl_main.setVisibility(View.VISIBLE);
                rl_image.setVisibility(View.GONE);
                findGroup.setOnClickListener(mOnClickListener);
                createCroup.setOnClickListener(mOnClickListener);
                break;
            case "이미지1":
                textType1.setVisibility(View.VISIBLE);
                textType2.setVisibility(View.GONE);
                rl_main.setVisibility(View.GONE);
                rl_image.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.banner01);
                break;
            case "이미지2":
                textType1.setVisibility(View.GONE);
                textType2.setVisibility(View.VISIBLE);
                rl_main.setVisibility(View.GONE);
                rl_image.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.banner02);
                break;
        }

        container.addView(rootView, 0);

        return rootView;
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
