package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

public class ProfileActivity extends Activity {
    private ActionBar actionBar;
    private ImageView profileImage;
    private PreferenceManager preferenceManager;
    private TextView name, knuId, department, number, grade, email;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileImage = findViewById(R.id.iv_profile_image);
        name = findViewById(R.id.tv_name);
        knuId = findViewById(R.id.tv_knu_id);
        department = findViewById(R.id.tv_dept);
        number = findViewById(R.id.tv_stu_num);
        grade = findViewById(R.id.tv_grade);
        email = findViewById(R.id.tv_email);
        preferenceManager = app.AppController.getInstance().getPreferenceManager();
        user = preferenceManager.getUser();
        actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });
        Glide.with(getApplicationContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{IMAGE_ID}", user.getImageId()), new LazyHeaders.Builder().addHeader("Cookie", preferenceManager.getCookie()).build()))
                .into(profileImage);
        name.setText(user.getName());
        knuId.setText(user.getUserId());
        department.setText(user.getDepartment());
        number.setText(user.getNumber());
        grade.setText(user.getGrade());
        email.setText(user.getEmail());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
