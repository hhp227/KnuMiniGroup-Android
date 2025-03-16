package com.hhp227.knu_minigroup.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.viewmodel.SplashViewModel;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 1250;

    private SplashViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_splash);
        mViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        Window window = getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewModel.connection();
            }
        }, SPLASH_TIME_OUT);
        observeViewModelData();
    }

    private void observeViewModelData() {
        mViewModel.isSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSuccess) {
                if (isSuccess) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(R.anim.splash_in, R.anim.splash_out);
                    finish();
                }
            }
        });
        mViewModel.isPreferenceClear().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPreferenceClear) {
                if (isPreferenceClear) {
                    mViewModel.clearUser();
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
        mViewModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }
}