package com.hhp227.knu_minigroup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityMainBinding;
import com.hhp227.knu_minigroup.databinding.NavHeaderMainBinding;
import com.hhp227.knu_minigroup.fragment.BusFragment;
import com.hhp227.knu_minigroup.fragment.GroupMainFragment;
import com.hhp227.knu_minigroup.fragment.MealFragment;
import com.hhp227.knu_minigroup.fragment.SeatFragment;
import com.hhp227.knu_minigroup.fragment.TimetableFragment;
import com.hhp227.knu_minigroup.fragment.UnivNoticeFragment;
import com.hhp227.knu_minigroup.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;

    private MainViewModel mViewModel;

    private ActionBarDrawerToggle mDrawerToggle;

    private ActivityResultLauncher<Intent> mProfileActivityResultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mProfileActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    updateProfileImage();
                }
            }
        });

        setContentView(mBinding.getRoot());

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                getString(R.string.admob_app_id);
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new GroupMainFragment()).commit();
        mBinding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;

                switch (item.getItemId()) {
                    case R.id.nav_menu1:
                        fragment = GroupMainFragment.newInstance();
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
                        logout();
                        break;
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
        subscribeUi(NavHeaderMainBinding.bind(mBinding.navView.getHeaderView(0)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.drawerLayout.removeDrawerListener(mDrawerToggle);
        mBinding = null;
        mProfileActivityResultLauncher = null;
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

    public void updateProfileImage() {
        Glide.with(getApplicationContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mViewModel.getUser().getUid()), new LazyHeaders.Builder()
                        .addHeader("Cookie", mViewModel.getCookie())
                        .build()))
                .apply(new RequestOptions().circleCrop()
                        .error(R.drawable.user_image_view_circle)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(NavHeaderMainBinding.bind(mBinding.navView.getHeaderView(0)).ivProfileImage);
    }

    public void logout() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);

        mViewModel.logout();
        startActivity(intent);
        finish();
    }

    private void subscribeUi(NavHeaderMainBinding navHeaderMainBinding) {
        Glide.with(this)
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mViewModel.getUser().getUid()), new LazyHeaders.Builder()
                        .addHeader("Cookie", mViewModel.getCookie())
                        .build()))
                .apply(new RequestOptions().circleCrop()
                        .error(R.drawable.user_image_view_circle)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(navHeaderMainBinding.ivProfileImage);
        navHeaderMainBinding.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileActivityResultLauncher.launch(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });
        navHeaderMainBinding.tvName.setText(mViewModel.getUser().getName());
    }
}