package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final MutableLiveData<User> mUser = new MutableLiveData<>(mPreferenceManager.getUser());

    public void logout() {
        mPreferenceManager.clear();
        mCookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                Log.d(TAG, "onReceiveValue " + value);
            }
        });
    }

    public void setUser(User user) {
        mUser.postValue(user);
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN);
    }
}
