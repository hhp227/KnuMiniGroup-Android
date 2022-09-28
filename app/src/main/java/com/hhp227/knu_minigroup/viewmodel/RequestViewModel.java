package com.hhp227.knu_minigroup.viewmodel;

import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.data.GroupRepository;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

public class RequestViewModel extends ViewModel {
    private final MutableLiveData<State> mState = new MutableLiveData<>(new State(false, Collections.emptyList(), 1, false, false, null));

    private static final int LIMIT = 100;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final GroupRepository mGroupRepository = new GroupRepository();

    public RequestViewModel() {
        fetchNextPage();
    }

    public LiveData<State> getState() {
        return mState;
    }

    public void fetchGroupList(int offset) {
        mGroupRepository.getJoinRequestGroupList(mCookieManager.getCookie(EndPoint.LOGIN), mPreferenceManager.getUser(), offset, LIMIT, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                List<Map.Entry<String, GroupItem>> groupItemList = (List<Map.Entry<String, GroupItem>>) data;

                if (mState.getValue() != null) {
                    if (mState.getValue().groupItemList.size() != groupItemList.size()) {
                        mState.postValue(new State(false, mergedList(mState.getValue().groupItemList, groupItemList), mState.getValue().offset + LIMIT, false, groupItemList.isEmpty(), null));
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                mState.postValue(new State(false, Collections.emptyList(), 0, false, false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                mState.postValue(new State(true, Objects.requireNonNull(mState.getValue()).groupItemList, offset, offset > 1, false, null));
            }
        });
    }

    public void fetchNextPage() {
        if (mState.getValue() != null && !mGroupRepository.isStopRequestMore()) {
            mState.postValue(new State(false, mState.getValue().groupItemList, mState.getValue().offset, true, false, null));
        }
    }

    public void refresh() {
        mGroupRepository.setMinId(0);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mState.postValue(new State(false, Collections.emptyList(), 1, true, false, null));
            }
        });
    }

    private List<Map.Entry<String, GroupItem>> mergedList(List<Map.Entry<String, GroupItem>> existingList, List<Map.Entry<String, GroupItem>> newList) {
        List<Map.Entry<String, GroupItem>> result = new ArrayList<>();

        result.addAll(existingList);
        result.addAll(newList);
        return result;
    }

    public static final class State {
        public boolean isLoading;

        public List<Map.Entry<String, GroupItem>> groupItemList;

        public int offset;

        public boolean hasRequestedMore;

        public boolean isEndReached;

        public String message;

        public State(boolean isLoading, List<Map.Entry<String, GroupItem>> groupItemList, int offset, boolean hasRequestedMore, boolean isEndReached, String message) {
            this.isLoading = isLoading;
            this.groupItemList = groupItemList;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.isEndReached = isEndReached;
            this.message = message;
        }
    }
}
