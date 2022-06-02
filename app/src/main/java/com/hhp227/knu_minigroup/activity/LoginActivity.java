package com.hhp227.knu_minigroup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.databinding.ActivityLoginBinding;
import com.hhp227.knu_minigroup.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding mBinding;

    private LoginViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 액션바 없음
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setContentView(mBinding.getRoot());

        // 사용자가 이미 로그인되어있는지 아닌지 확인
        if (mViewModel.getUser() != null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }

        // 로그인 버튼 클릭 이벤트
        mBinding.bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = mBinding.etId.getText().toString();
                final String password = mBinding.etPassword.getText().toString();

                mViewModel.login(id, password);
            }
        });
        mViewModel.getState().observe(this, new Observer<LoginViewModel.State>() {
            @Override
            public void onChanged(LoginViewModel.State state) {
                if (state.isLoading) {
                    showProgressBar();
                } else if (state.user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    mViewModel.storeUser(state.user);

                    // 화면이동
                    hideProgressBar();
                    startActivity(intent);
                    finish();
                } else if (state.message != null && !TextUtils.isEmpty(state.message)) {
                    hideProgressBar();
                    Snackbar.make(getCurrentFocus(), state.message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
        mViewModel.getLoginFormState().observe(this, new Observer<LoginViewModel.LoginFormState>() {
            @Override
            public void onChanged(LoginViewModel.LoginFormState loginFormState) {
                mBinding.etId.setError(loginFormState.emailError);
                mBinding.etPassword.setError(loginFormState.passwordError);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    private void showProgressBar() {
        if (mBinding.pbLogin.getVisibility() == View.GONE)
            mBinding.pbLogin.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbLogin.getVisibility() == View.VISIBLE)
            mBinding.pbLogin.setVisibility(View.GONE);
    }
}
