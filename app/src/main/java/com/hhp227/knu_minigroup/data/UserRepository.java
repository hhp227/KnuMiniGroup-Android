package com.hhp227.knu_minigroup.data;

import com.hhp227.knu_minigroup.data.remote.UserRemoteDataSource;
import com.hhp227.knu_minigroup.helper.Callback;

public class UserRepository {
    private final UserRemoteDataSource mUserRemoteDataSource;

    public UserRepository() {
        this.mUserRemoteDataSource = new UserRemoteDataSource();
    }

    public UserRepository(String mGroupKey) {
        this.mUserRemoteDataSource = new UserRemoteDataSource(mGroupKey);
    }

    public boolean isStopRequestMore() {
        return mUserRemoteDataSource.isStopRequestMore();
    }

    public void setLastKey(String lastKey) {
        mUserRemoteDataSource.setLastKey(lastKey);
    }

    public void getManagedMemberList(String cookie, String groupId, Callback callback) {
        mUserRemoteDataSource.getManagedMemberList(cookie, groupId, callback);
    }

    public void getUserList(int limit, Callback callback) {
        mUserRemoteDataSource.getUserList(limit, callback);
    }
}
