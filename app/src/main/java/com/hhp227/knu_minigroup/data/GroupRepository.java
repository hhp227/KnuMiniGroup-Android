package com.hhp227.knu_minigroup.data;

import android.graphics.Bitmap;

import com.hhp227.knu_minigroup.data.remote.GroupRemoteDataSource;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;

public class GroupRepository {
    private final GroupRemoteDataSource mGroupRemoteDataSource = new GroupRemoteDataSource();

    public GroupRepository() {
    }

    public boolean isStopRequestMore() {
        return mGroupRemoteDataSource.isStopRequestMore();
    }

    public void setLastKey(String lastKey) {
        mGroupRemoteDataSource.setLastKey(lastKey);
    }

    public void getJoinedGroupList(User user, Callback callback) {
        mGroupRemoteDataSource.getJoinedGroupList(user, callback);
    }

    public void getNotJoinedGroupList(int offset, int limit, Callback callback) {
        mGroupRemoteDataSource.getNotJoinedGroupList(offset, limit, callback);
    }

    public void getJoinRequestGroupList(User user, int offset, int limit, Callback callback) {
        mGroupRemoteDataSource.getJoinRequestGroupList(user, offset, limit, callback);
    }

    public void getPopularGroupList(String cookie, Callback callback) {
        mGroupRemoteDataSource.getPopularGroupList(cookie, callback);
    }

    public void getGroup(String cookie, String groupId, String groupImage, Callback callback) {
        mGroupRemoteDataSource.getGroup(cookie, groupId, groupImage, callback);
    }

    public void addGroup(String cookie, User user, Bitmap bitmap, String title, String description, String type, Callback callback) {
        mGroupRemoteDataSource.addGroup(cookie, user, bitmap, title, description, type, callback);
    }

    public void setGroup(String cookie, String groupKey, String groupId, String groupName, String description, String joinType, Bitmap bitmap, Callback callback) {
        mGroupRemoteDataSource.setGroup(cookie, groupKey, groupId, groupName, description, joinType, bitmap, callback);
    }

    public void removeGroup(User user, boolean isAdmin, String key, Callback callback) {
        mGroupRemoteDataSource.removeGroup(user, isAdmin, key, callback);
    }
}
