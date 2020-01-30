package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import androidx.viewpager.widget.ViewPager;
import com.hhp227.knu_minigroup.adapter.PicturePagerAdapter;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

import java.util.List;

public class PictureActivity extends Activity {
    private TextView mCount;
    private List<String> mImages;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 상단 타이틀바를 투명하게
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_picture);
        ActionBar actionBar = getActionBar();
        PicturePagerAdapter pagerAdapter = new PicturePagerAdapter(this, mImages);
        mViewPager = findViewById(R.id.view_pager);
        mCount = findViewById(R.id.tv_count);
        int position = 0;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mImages = b.getStringArrayList("images");
            position = b.getInt("position");
        }

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCount.setText((position + 1) + " / " + mImages.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewPager.setCurrentItem(position, false);
        if (actionBar != null) {

            // 뒤로가기버튼
            actionBar.setDisplayHomeAsUpEnabled(true);

            // 앱 아이콘 숨기기
            actionBar.setDisplayShowHomeEnabled(false);

            // 액션바 투명
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
            actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
                @Override
                public boolean isLayoutRtl() {
                    return false;
                }
            });
        }
        mCount.setVisibility(mImages.size() > 1 ? View.VISIBLE : View.GONE);
        mCount.setText((position + 1) + " / " + mImages.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.clearOnPageChangeListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
