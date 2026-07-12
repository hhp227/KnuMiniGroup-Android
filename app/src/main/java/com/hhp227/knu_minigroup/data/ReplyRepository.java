package com.hhp227.knu_minigroup.data;

import com.hhp227.knu_minigroup.data.remote.ReplyRemoteDataSource;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;

public class ReplyRepository {
    private final ReplyRemoteDataSource mReplyRemoteDataSource;

    public ReplyRepository(String articleKey) {
        this.mReplyRemoteDataSource = new ReplyRemoteDataSource(articleKey);
    }

    public void getReplyList(Callback callback) {
        mReplyRemoteDataSource.getReplyList(callback);
    }

    public void addReply(User user, String text, Callback callback) {
        mReplyRemoteDataSource.addReply(user, text, callback);
    }

    public void setReply(String replyKey, String text, Callback callback) {
        mReplyRemoteDataSource.setReply(replyKey, text, callback);
    }

    public void removeReply(String replyKey, Callback callback) {
        mReplyRemoteDataSource.removeReply(replyKey, callback);
    }
}
