package com.hhp227.knu_minigroup.data;

import android.graphics.Bitmap;

import com.hhp227.knu_minigroup.data.remote.ArticleRemoteDataSource;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.helper.Callback;

import java.util.List;

public class ArticleRepository {
    private final ArticleRemoteDataSource mArticleRemoteDataSource;

    public ArticleRepository(String groupId, String key) {
        this.mArticleRemoteDataSource = new ArticleRemoteDataSource(groupId, key);
    }

    public boolean isStopRequestMore() {
        return mArticleRemoteDataSource.isStopRequestMore();
    }

    public void setLastKey(String lastKey) {
        mArticleRemoteDataSource.setLastKey(lastKey);
    }

    public void getArticleList(String cookie, int limit, Callback callback) {
        mArticleRemoteDataSource.getArticleList(cookie, limit, callback);
    }

    public void getArticleData(String articleKey, Callback callback) {
        mArticleRemoteDataSource.getArticleData(articleKey, callback);
    }

    public void addArticle(User user, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        mArticleRemoteDataSource.addArticle(user, title, content, imageList, youTubeItem, callback);
    }

    public void setArticle(String articleKey, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        mArticleRemoteDataSource.setArticle(articleKey, title, content, imageList, youTubeItem, callback);
    }

    public void removeArticle(String articleKey, Callback callback) {
        mArticleRemoteDataSource.removeArticle(articleKey, callback);
    }

    public void addArticleImage(String cookie, Bitmap bitmap, Callback callback) {
        mArticleRemoteDataSource.addArticleImage(cookie, bitmap, callback);
    }
}
