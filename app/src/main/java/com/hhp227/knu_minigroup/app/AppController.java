package com.hhp227.knu_minigroup.app;

import android.app.Application;
import android.text.TextUtils;
import android.webkit.CookieManager;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

public class AppController extends Application {
    private static final String TAG = AppController.class.getSimpleName();

    private static AppController mInstance;

    private CookieManager mCookieManager;

    private PreferenceManager mPreferenceManager;

    private RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());

        return mRequestQueue;
    }

    public PreferenceManager getPreferenceManager() {
        if (mPreferenceManager == null)
            mPreferenceManager = new PreferenceManager(this);

        return mPreferenceManager;
    }

    public CookieManager getCookieManager() {
        if (mCookieManager == null)
            mCookieManager = CookieManager.getInstance();

        return mCookieManager;
    }

    public void setCookieManager(CookieManager cookieManager) {
        this.mCookieManager = cookieManager;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // 태그가 비어 있으면 기본 태그 세트
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null)
            mRequestQueue.cancelAll(tag);
    }
}
