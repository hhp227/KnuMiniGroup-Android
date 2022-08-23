package com.hhp227.knu_minigroup.viewmodel;

import android.os.CountDownTimer;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.data.GroupRepository;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.dto.ReplyItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupMainViewModel extends ViewModel {
    private static final String TAG = GroupMainViewModel.class.getSimpleName();

    private final MutableLiveData<Long> mTick = new MutableLiveData<>();

    private final MutableLiveData<State> mState = new MutableLiveData<>();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final CountDownTimer mCountDownTimer = new CountDownTimer(80000, 8000) {
        @Override
        public void onTick(long millisUntilFinished) {
            mTick.postValue(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            start();
        }
    };

    private final GroupRepository mGroupRepository = new GroupRepository();

    public GroupMainViewModel() {
        fetchDataTask();
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public void startCountDownTimer() {
        mCountDownTimer.start();
    }

    public void cancelCountDownTimer() {
        mCountDownTimer.cancel();
    }

    public LiveData<Long> getTick() {
        return mTick;
    }

    public LiveData<State> getState() {
        return mState;
    }

    public void refresh() {
        fetchDataTask();
    }

    private void fetchDataTask() {
        mGroupRepository.getJoinedGroupList(mCookieManager.getCookie(EndPoint.LOGIN), getUser(), new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                mState.postValue(new State(false, (List<Map.Entry<String, Object>>) data, null));
            }

            @Override
            public void onFailure(Throwable throwable) {
                mState.postValue(new State(false, Collections.emptyList(), throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                mState.postValue(new State(true, Collections.emptyList(), null));
            }
        });
    }

    public static final class State {
        public boolean isLoading;

        public List<Map.Entry<String, Object>> groupItemList;

        public String message;

        public State(boolean isLoading, List<Map.Entry<String, Object>> groupItemList, String message) {
            this.isLoading = isLoading;
            this.groupItemList = groupItemList;
            this.message = message;
        }
    }
}
