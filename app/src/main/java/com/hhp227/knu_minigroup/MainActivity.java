package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.MobileAds;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.fragment.*;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import com.hhp227.knu_minigroup.ui.navigationdrawer.ActionBarDrawerToggle;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

import static com.hhp227.knu_minigroup.fragment.GroupFragment.UPDATE_GROUP;

public class MainActivity extends FragmentActivity {
    private ActionBar mActionBar;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private CharSequence mTitleSection;
    private DrawerArrowDrawable mDrawerArrow;
    private DrawerLayout mDrawerLayout;
    private ImageView mProfileImage;
    private ListView mDrawerList;
    private PreferenceManager mPreferenceManager;
    private RelativeLayout mDrawerRelativeLayout;
    private TextView mKnuId;

    GroupFragment fragMain;
    UnivNoticeFragment fragUnivNotice;
    TimetableFragment fragTimetable;
    BusFragment fragBus;
    MealFragment fragMeal;
    SeatFragment fragSeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String[] menu = {"메인화면", "본관게시판", "시간표", "통학버스시간표", "식단보기", "도서관좌석", "로그아웃"};
        mActionBar = getActionBar();
        mDrawerLayout = findViewById(R.id.dl_group);
        mDrawerRelativeLayout = findViewById(R.id.rl_left_drawer);
        mDrawerList = findViewById(R.id.lv_drawer);
        mProfileImage = findViewById(R.id.iv_profile_image);
        mKnuId = findViewById(R.id.tv_knu_id);

        fragMain = GroupFragment.newInstance();
        fragUnivNotice = UnivNoticeFragment.newInstance();
        fragTimetable = TimetableFragment.newInstance();
        fragBus = BusFragment.newInstance();
        fragMeal = MealFragment.newInstance();
        fragSeat = SeatFragment.newInstance();

        mPreferenceManager = app.AppController.getInstance().getPreferenceManager();
        mTitleSection = "메인화면";
        mDrawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mDrawerArrow, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                mActionBar.setTitle("메뉴목록");
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mActionBar.setTitle(mTitleSection);
                invalidateOptionsMenu();
            }
        };

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mActionBar.setDisplayShowHomeEnabled(false); // 제목앞에 아이콘 안보이기
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setTitle(mTitleSection);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menu));
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragMain).commit();
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragMain).commit();
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragUnivNotice).commit();
                        break;
                    case 2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragTimetable).commit();
                        break;
                    case 3:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragBus).commit();
                        break;
                    case 4:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragMeal).commit();
                        break;
                    case 5:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragSeat).commit();
                        break;
                    case 6:
                        logoutUser();
                        break;
                }
                mDrawerList.setItemChecked(position, true);
                mTitleSection = menu[position];
                mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
            }
        });
        mDrawerList.setItemChecked(0, true);
        Glide.with(getApplicationContext())
                    .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mPreferenceManager.getUser().getUid()), new LazyHeaders.Builder().addHeader("Cookie", mPreferenceManager.getCookie()).build()))
                    .apply(new RequestOptions().circleCrop().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(mProfileImage);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });
        mKnuId.setText(mPreferenceManager.getUser().getUserId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Glide.with(getApplicationContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mPreferenceManager.getUser().getUid()), new LazyHeaders.Builder().addHeader("Cookie", mPreferenceManager.getCookie()).build()))
                .apply(new RequestOptions().circleCrop().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(mProfileImage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDrawerLayout.removeDrawerListener(mActionBarDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logoutUser() {
        mPreferenceManager.clear();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
