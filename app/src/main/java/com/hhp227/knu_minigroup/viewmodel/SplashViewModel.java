package com.hhp227.knu_minigroup.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.knu_minigroup.data.AuthRepository;
import com.hhp227.knu_minigroup.helper.Callback;

public class SplashViewModel extends ViewModel {
    private final AuthRepository mAuthRepository = new AuthRepository();

    private final MutableLiveData<Boolean> mSuccess = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> mPreferenceClear = new MutableLiveData<>(false);

    public MutableLiveData<Boolean> isSuccess() {
        return mSuccess;
    }

    public MutableLiveData<Boolean> isPreferenceClear() {
        return mPreferenceClear;
    }

    // KNU SSO 재검증을 제거하고 Firebase 세션 확인으로 대체 — 오프라인이면 통과, 세션 무효면 로그인 화면으로
    public void connection() {
        mAuthRepository.loginSilently(new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                mSuccess.postValue(true);
            }

            @Override
            public void onFailure(Throwable throwable) {
                mPreferenceClear.postValue(true);
            }

            @Override
            public void onLoading() {
            }
        });
    }

    public void clearUser() {
        mAuthRepository.logout();
    }
}
