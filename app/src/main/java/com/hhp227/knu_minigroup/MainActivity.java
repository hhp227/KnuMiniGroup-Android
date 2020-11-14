package com.hhp227.knu_minigroup;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.fragment.*;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import static com.hhp227.knu_minigroup.fragment.GroupFragment.UPDATE_GROUP;

public class MainActivity extends AppCompatActivity {
    private CookieManager mCookieManager;

    private DrawerLayout mDrawerLayout;

    private PreferenceManager mPreferenceManager;

    private ImageView mProfileImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavigationView navigationView = findViewById(R.id.nav_view);
        TextView name = navigationView.getHeaderView(0).findViewById(R.id.tv_name);
        mProfileImage = navigationView.getHeaderView(0).findViewById(R.id.iv_profile_image);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mCookieManager = AppController.getInstance().getCookieManager();

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new GroupFragment()).commit();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
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
                mDrawerLayout.closeDrawer(GravityCompat.START);
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
                .into(mProfileImage);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);

                startActivityForResult(intent, UPDATE_GROUP);
            }
        });
        name.setText(mPreferenceManager.getUser().getName());
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
                    .into(mProfileImage);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
}