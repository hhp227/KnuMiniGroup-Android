package com.hhp227.knu_minigroup.viewmodel;

import android.webkit.CookieManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Header;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashViewModel extends ViewModel {
    public MutableLiveData<State> mState = new MutableLiveData<>();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    public void connection() {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (!jsonObject.getBoolean("isError")) {
                        mState.postValue(new State(true, false, null));
                    } else {
                        mState.postValue(new State(false, true, "패스워드가 변경되었습니다. 다시 로그인해주세요."));
                    }
                } catch (JSONException e) {
                    mState.postValue(new State(false, false, "학습관리시스템(LMS) 서버에 접속할수 없습니다."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, false, "네트워크 연결을 확인해주세요."));
                VolleyLog.e(SplashViewModel.class.getSimpleName(), error.getMessage());
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                if (response.allHeaders != null) {
                    for (Header header : response.allHeaders)
                        if (header.getName().equals("Set-Cookie") && header.getValue().contains("SESSION_NEWLMS"))
                            mCookieManager.setCookie(EndPoint.LOGIN, header.getValue());
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            protected Map<String, String> getParams() {
                User user = mPreferenceManager.getUser();
                String knuId = user.getUserId();
                String password = user.getPassword();
                Map<String, String> params = new HashMap<>();

                params.put("usr_id", knuId);
                params.put("usr_pwd", password);
                return params;
            }
        });
    }

    public void clearUser() {
        mPreferenceManager.clear();
    }

    public static final class State {
        public boolean isSuccess;

        public boolean isPreferenceClear;

        public String message;

        public State(boolean isSuccess, boolean isPreferenceClear, String message) {
            this.isSuccess = isSuccess;
            this.isPreferenceClear = isPreferenceClear;
            this.message = message;
        }
    }
}
