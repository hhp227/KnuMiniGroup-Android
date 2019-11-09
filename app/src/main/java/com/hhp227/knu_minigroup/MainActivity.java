package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.hhp227.knu_minigroup.fragment.GroupFragment;
import com.hhp227.knu_minigroup.fragment.UnivNoticeFragment;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import com.hhp227.knu_minigroup.ui.navigationdrawer.ActionBarDrawerToggle;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

public class MainActivity extends FragmentActivity {
    private ActionBar actionBar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private CharSequence titleSection;
    private DrawerArrowDrawable drawerArrow;
    private DrawerLayout drawerLayout;
    private RelativeLayout drawerRelativeLayout;
    private ListView drawerList;
    private PreferenceManager preferenceManager;
    private TextView knuId;

    String[] menu = {"메인화면", "본관게시판"};

    GroupFragment fragMain;
    UnivNoticeFragment fragUnivNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getActionBar();
        drawerLayout = findViewById(R.id.dl_group);
        drawerRelativeLayout = findViewById(R.id.rl_left_drawer);
        drawerList = findViewById(R.id.lv_drawer);
        knuId = findViewById(R.id.tv_knu_id);

        fragMain = GroupFragment.newInstance();
        fragUnivNotice = UnivNoticeFragment.newInstance();

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

        knuId.setText("임시");

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        drawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menu));

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
}
