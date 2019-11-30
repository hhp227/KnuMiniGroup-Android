package com.hhp227.knu_minigroup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import com.hhp227.knu_minigroup.fragment.TabHostLayoutFragment;

public class GroupActivity extends FragmentActivity {
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

        getActionBar().setTitle(groupName);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragMain).commit();
    }
}
