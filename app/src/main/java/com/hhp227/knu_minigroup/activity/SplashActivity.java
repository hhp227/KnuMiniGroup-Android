package com.hhp227.knu_minigroup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.databinding.ActivitySplashBinding;
import com.hhp227.knu_minigroup.viewmodel.SplashViewModel;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 1250;

    private ActivitySplashBinding mBinding;

    private SplashViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 액션바 안보이기
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        setContentView(mBinding.getRoot());
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewModel.connection();
            }
        }, SPLASH_TIME_OUT);
        mViewModel.mState.observe(this, new Observer<SplashViewModel.State>() {
            @Override
            public void onChanged(SplashViewModel.State state) {
                if (state != null) {
                    if (state.isSuccess) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.splash_in, R.anim.splash_out);
                        finish();
                    } else {
                        if (state.isPreferenceClear) {
                            AppController.getInstance().getPreferenceManager().clear();
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        } else if (state.message != null) {
                            Toast.makeText(getApplicationContext(), state.message, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }
}
