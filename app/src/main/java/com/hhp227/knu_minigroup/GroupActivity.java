package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import com.hhp227.knu_minigroup.fragment.TabHostLayoutFragment;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

public class GroupActivity extends FragmentActivity {
    private ActionBar actionBar;
    TabHostLayoutFragment fragMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Intent intent = getIntent();
        boolean isAdmin = intent.getBooleanExtra("admin", false);
        int groupId = intent.getIntExtra("grp_id", 0);
        String groupName = intent.getStringExtra("grp_nm");
        fragMain = TabHostLayoutFragment.newInstance(isAdmin, groupId, groupName);
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle(groupName);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragMain).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
