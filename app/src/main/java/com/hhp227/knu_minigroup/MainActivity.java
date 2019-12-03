package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.hhp227.knu_minigroup.fragment.*;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import com.hhp227.knu_minigroup.ui.navigationdrawer.ActionBarDrawerToggle;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity {
    private ActionBar actionBar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private CharSequence titleSection;
    private DrawerArrowDrawable drawerArrow;
    private DrawerLayout drawerLayout;
    private ImageView profileImage;
    private RelativeLayout drawerRelativeLayout;
    private ListView drawerList;
    private PreferenceManager preferenceManager;
    private TextView knuId;

    String[] menu = {"메인화면", "본관게시판", "시간표", "통학버스시간표", "식단보기", "도서관좌석", "로그아웃"};

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

        actionBar = getActionBar();
        drawerLayout = findViewById(R.id.dl_group);
        drawerRelativeLayout = findViewById(R.id.rl_left_drawer);
        drawerList = findViewById(R.id.lv_drawer);
        knuId = findViewById(R.id.tv_knu_id);
        profileImage = findViewById(R.id.iv_profile_image);

        fragMain = GroupFragment.newInstance();
        fragUnivNotice = UnivNoticeFragment.newInstance();
        fragTimetable = TimetableFragment.newInstance();
        fragBus = BusFragment.newInstance();
        fragMeal = MealFragment.newInstance();
        fragSeat = SeatFragment.newInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());
        titleSection = "메인화면";
        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, drawerArrow, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle("메뉴목록");
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                actionBar.setTitle(titleSection);
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }
        };

        actionBar.setDisplayShowHomeEnabled(false); // 제목앞에 아이콘 안보이기
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(titleSection);

        knuId.setText(app.AppController.getInstance().getPreferenceManager().getUser().getUserId());

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        drawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menu));

        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragMain).commit();

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0 :
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragMain).commit();
                        break;
                    case 1 :
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragUnivNotice).commit();
                        break;
                    case 2 :
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragTimetable).commit();
                        break;
                    case 3 :
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragBus).commit();
                        break;
                    case 4 :
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragMeal).commit();
                        break;
                    case 5 :
                        getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragSeat).commit();
                        break;
                    case 6 :
                        logoutUser();
                        break;
                }
                drawerList.setItemChecked(position, true);
                titleSection = menu[position];
                drawerLayout.closeDrawer(drawerRelativeLayout);
            }
        });
        drawerList.setItemChecked(0, true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    private void logoutUser() {
        preferenceManager.clear();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
