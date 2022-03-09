package com.hhp227.knu_minigroup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityMainBinding;
import com.hhp227.knu_minigroup.databinding.NavHeaderMainBinding;
import com.hhp227.knu_minigroup.fragment.*;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import static com.hhp227.knu_minigroup.fragment.GroupFragment.UPDATE_GROUP;

public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding mBinding;

    private CookieManager mCookieManager;

    private PreferenceManager mPreferenceManager;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(mBinding.getRoot());
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mCookieManager = AppController.getInstance().getCookieManager();

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new GroupFragment()).commit();
        mBinding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                switch (item.getItemId()) {
                    case R.id.nav_menu1:
                        fragment = GroupFragment.newInstance();
                        break;
                    case R.id.nav_menu2:
                        fragment = UnivNoticeFragment.newInstance();
                        break;
                    case R.id.nav_menu3:
                        fragment = TimetableFragment.newInstance();
                        break;
                    case R.id.nav_menu4:
                        fragment = SeatFragment.newInstance();
                        break;
                    case R.id.nav_menu5:
                        fragment = BusFragment.newInstance();
                        break;
                    case R.id.nav_menu6:
                        fragment = MealFragment.newInstance();
                        break;
                    case R.id.nav_menu7:
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);

                        mPreferenceManager.clear();
                        startActivity(intent);
                        finish();
                }
                if (fragment != null) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                    fragmentTransaction.replace(R.id.content_frame, fragment);
                    fragmentTransaction.commit();
                }
                mBinding.drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        Glide.with(this)
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mPreferenceManager.getUser().getUid()), new LazyHeaders.Builder()
                        .addHeader("Cookie", mCookieManager.getCookie(EndPoint.LOGIN))
                        .build()))
                .apply(new RequestOptions().circleCrop()
                        .error(R.drawable.user_image_view_circle)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(NavHeaderMainBinding.bind(mBinding.navView.getHeaderView(0)).ivProfileImage);
        NavHeaderMainBinding.bind(mBinding.navView.getHeaderView(0)).ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);

                startActivityForResult(intent, UPDATE_GROUP);
            }
        });
        NavHeaderMainBinding.bind(mBinding.navView.getHeaderView(0)).tvName.setText(mPreferenceManager.getUser().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.drawerLayout.removeDrawerListener(mDrawerToggle);
        mBinding = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Glide.with(getApplicationContext())
                    .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mPreferenceManager.getUser().getUid()), new LazyHeaders.Builder()
                            .addHeader("Cookie", mCookieManager.getCookie(EndPoint.LOGIN))
                            .build()))
                    .apply(new RequestOptions().circleCrop()
                            .error(R.drawable.user_image_view_circle)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(NavHeaderMainBinding.bind(mBinding.navView.getHeaderView(0)).ivProfileImage);
        }
    }

    @Override
    public void onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START))
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    public void setAppBar(Toolbar toolbar, String title) {
        setTitle(title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mBinding.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        }
        mBinding.drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }
}