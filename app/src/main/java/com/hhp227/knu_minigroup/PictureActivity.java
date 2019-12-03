package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.Window;
import com.hhp227.knu_minigroup.helper.ZoomImageView;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

public class PictureActivity extends Activity {
    private ZoomImageView zoomImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 상단 타이틀바를 투명하게
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_picture);
        zoomImageView = findViewById(R.id.ziv_image);
        ActionBar actionBar = getActionBar();

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

        zoomImageView.setAdjustViewBounds(false);

        String imageUrl = null;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            imageUrl = b.getString("image_url");
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mScreenWidth = metrics.widthPixels;
        int mScreenHeight = metrics.heightPixels;

        zoomImageView.setImageResource(R.drawable.bg_no_image);
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
