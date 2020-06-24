package com.hhp227.knu_minigroup;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.hhp227.knu_minigroup.fragment.TabHostLayoutFragment;

public class GroupActivity extends AppCompatActivity {
    private boolean mIsAdmin;
    private int mPosition;
    private String mGroupId, mGroupName, mGroupImage, mKey;
    private TabHostLayoutFragment mFragMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Intent intent = getIntent();
        mIsAdmin = intent.getBooleanExtra("admin", false);
        mGroupId = intent.getStringExtra("grp_id");
        mGroupName = intent.getStringExtra("grp_nm");
        mGroupImage = intent.getStringExtra("grp_img");
        mPosition = intent.getIntExtra("pos", 0);
        mKey = intent.getStringExtra("key");
        mFragMain = TabHostLayoutFragment.newInstance(mIsAdmin, mGroupId, mGroupName, mGroupImage, mPosition, mKey);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mFragMain).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        else if (item.getItemId() == R.id.action_chat) {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra("grp_chat", true);
            intent.putExtra("chat_nm", mGroupName);
            intent.putExtra("uid", mKey);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragMain.onActivityResult(requestCode, resultCode, data);
    }
}
