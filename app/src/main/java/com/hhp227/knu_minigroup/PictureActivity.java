package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.hhp227.knu_minigroup.adapter.PicturePagerAdapter;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

import java.util.List;

public class PictureActivity extends Activity {
    private TextView count;
    private ViewPager viewPager;
    private PicturePagerAdapter pagerAdapter;
    private List<String> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 상단 타이틀바를 투명하게
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_picture);
        ActionBar actionBar = getActionBar();
        viewPager = findViewById(R.id.view_pager);
        count = findViewById(R.id.tv_count);
        int position = 0;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            images = b.getStringArrayList("images");
            position = b.getInt("position");
        }
        pagerAdapter = new PicturePagerAdapter(this, images);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                count.setText((position + 1) + " / " + images.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setCurrentItem(position, false);
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
        count.setVisibility(images.size() > 1 ? View.VISIBLE : View.GONE);
        count.setText((position + 1) + " / " + images.size());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
