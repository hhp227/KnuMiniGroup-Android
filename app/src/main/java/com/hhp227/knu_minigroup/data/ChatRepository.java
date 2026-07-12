package com.hhp227.knu_minigroup.data;

import com.hhp227.knu_minigroup.data.remote.ChatRemoteDataSource;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;

public class ChatRepository {
    private final ChatRemoteDataSource mChatRemoteDataSource = new ChatRemoteDataSource();

    public void fetchMessageList(String currentUserUid, String receiver, boolean isGroupChat, String cursor, int limit, Callback callback) {
        mChatRemoteDataSource.fetchMessageList(currentUserUid, receiver, isGroupChat, cursor, limit, callback);
    }

    public void sendMessage(User user, String receiver, boolean isGroupChat, String text) {
        mChatRemoteDataSource.sendMessage(user, receiver, isGroupChat, text);
    }
}
