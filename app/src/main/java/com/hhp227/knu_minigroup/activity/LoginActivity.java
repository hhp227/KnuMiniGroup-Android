package com.hhp227.knu_minigroup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.databinding.ActivityLoginBinding;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding mBinding;

    private LoginViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 액션바 없음
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);

        // 사용자가 이미 로그인되어있는지 아닌지 확인
        if (AppController.getInstance().getPreferenceManager().getUser() != null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }
        mViewModel.getUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    mViewModel.storeUser(user);

                    // 화면이동
                    startActivity(intent);
                    finish();
                }
            }
        });
        mViewModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !TextUtils.isEmpty(message)) {
                    Snackbar.make(getCurrentFocus(), message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
        mViewModel.getEmailError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String emailError) {
                mBinding.etId.setError(emailError);
            }
        });
        mViewModel.getPasswordError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String passwordError) {
                mBinding.etPassword.setError(passwordError);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }
}