package com.hhp227.knu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.knu_minigroup.data.UserRepository;
import com.hhp227.knu_minigroup.dto.MemberItem;

import com.hhp227.knu_minigroup.helper.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tab3ViewModel extends ViewModel {
    private static final int LIMIT = 40;

    private static final String TAG = Tab3ViewModel.class.getSimpleName(), LOADING = "loading", OFFSET = "offset", REQUEST_MORE = "requestMore", END_REACHED = "endReached", MESSAGE = "message";

    private final MutableLiveData<List<MemberItem>> mItemList = new MutableLiveData<>(Collections.emptyList());

    private int offset = 1;

    private final String mGroupId, mKey;

    private final SavedStateHandle mSavedStateHandle;

    private final UserRepository mUserRepository;

    public Tab3ViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGroupId = savedStateHandle.get("grp_id");
        mKey = savedStateHandle.get("key");
        mUserRepository = new UserRepository(mKey);

        setLoading(false);
        setOffset(1);
        setRequestMore(false);
        setEndReached(false);
        fetchNextPage();
    }

    public void setLoading(boolean bool) {
        mSavedStateHandle.set(LOADING, bool);
    }

    public LiveData<Boolean> isLoading() {
        return mSavedStateHandle.getLiveData(LOADING);
    }

    public void setItemList(List<MemberItem> list) {
        mItemList.postValue(list);
    }

    public LiveData<List<MemberItem>> getItemList() {
        return mItemList;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int value) {
        offset = value;
    }

    public void setRequestMore(boolean bool) {
        mSavedStateHandle.set(REQUEST_MORE, bool);
    }

    public LiveData<Boolean> hasRequestMore() {
        return mSavedStateHandle.getLiveData(REQUEST_MORE);
    }

    public void setEndReached(boolean bool) {
        mSavedStateHandle.set(END_REACHED, bool);
    }

    public LiveData<Boolean> isEndReached() {
        return mSavedStateHandle.getLiveData(END_REACHED);
    }

    public void setMessage(String message) {
        mSavedStateHandle.set(MESSAGE, message);
    }

    public LiveData<String> getMessage() {
        return mSavedStateHandle.getLiveData(MESSAGE);
    }

    public void fetchMemberList(int offset) {
        // 스크롤 바닥에서 계속 호출되므로 중복 요청을 여기서 막지 않으면 같은 멤버가 겹쳐 쌓인다.
        if (Boolean.TRUE.equals(isLoading().getValue()) || Boolean.TRUE.equals(isEndReached().getValue())) {
            return;
        }
        mUserRepository.getUserList(LIMIT, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                List<MemberItem> memberItemList = (List<MemberItem>) data;

                setLoading(false);
                setItemList(mergedList(getItemList().getValue(), memberItemList));
                setOffset(getOffset() + LIMIT);
                setEndReached(mUserRepository.isStopRequestMore());
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setLoading(true);
            }
        });
    }

    public void fetchNextPage() {
        setRequestMore(true);
        fetchMemberList(getOffset());
    }

    public void refresh() {
        setRequestMore(true);
        setItemList(Collections.emptyList());
        setOffset(1);
        setEndReached(false);
        mUserRepository.setLastKey(null);
        fetchMemberList(getOffset());
    }

    private List<MemberItem> mergedList(List<MemberItem> existingList, List<MemberItem> newList) {
        return new ArrayList<MemberItem>() {
            {
                addAll(existingList);
                addAll(newList);
            }
        };
    }
}