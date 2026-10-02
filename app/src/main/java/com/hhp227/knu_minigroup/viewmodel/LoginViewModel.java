package com.hhp227.knu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.knu_minigroup.data.AuthRepository;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;

public class LoginViewModel extends ViewModel {
    private final AuthRepository mAuthRepository = new AuthRepository();

    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<User> mUser = new MutableLiveData<>();

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    private final MutableLiveData<String> mEmailError = new MutableLiveData<>();

    private final MutableLiveData<String> mPasswordError = new MutableLiveData<>();

    public MutableLiveData<String> id = new MutableLiveData<>("");

    public MutableLiveData<String> password = new MutableLiveData<>("");

    public LiveData<Boolean> isLoading() {
        return mLoading;
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    public LiveData<String> getEmailError() {
        return mEmailError;
    }

    public LiveData<String> getPasswordError() {
        return mPasswordError;
    }

    public void login(String id, String password) {
        if (!id.isEmpty() && !password.isEmpty()) {
            mAuthRepository.login(id, password, new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    mLoading.postValue(false);
                    mUser.postValue((User) data);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    mLoading.postValue(false);
                    mMessage.postValue(throwable.getMessage());
                }

                @Override
                public void onLoading() {
                    mLoading.postValue(true);
                }
            });
        } else {
            mEmailError.postValue(id.isEmpty() ? "아이디를 입력하세요." : null);
            mPasswordError.postValue(password.isEmpty() ? "패스워드를 입력하세요." : null);
        }
    }
}
