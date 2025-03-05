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

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.util.HashMap;
import java.util.Map;

public class SplashViewModel extends ViewModel {
    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final MutableLiveData<Boolean> mSuccess = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> mPreferenceClear = new MutableLiveData<>(false);

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    public MutableLiveData<Boolean> isSuccess() {
        return mSuccess;
    }

    public MutableLiveData<Boolean> isPreferenceClear() {
        return mPreferenceClear;
    }

    public MutableLiveData<String> getMessage() {
        return mMessage;
    }

    public void connection() {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Source source = new Source(response);
                    source.fullSequentialParse();

                    String resultCode = getInputValueById(source, "resultCode");

                    if (resultCode != null && resultCode.equals("000000")) {
                        mSuccess.postValue(true);
                        mPreferenceClear.postValue(false);
                    } else {
                        mSuccess.postValue(false);
                        mPreferenceClear.postValue(true);
                        mMessage.postValue("패스워드가 변경되었습니다. 다시 로그인해주세요.");
                    }
                } catch (Exception e) {
                    mSuccess.postValue(false);
                    mPreferenceClear.postValue(false);
                    mMessage.postValue("학습관리시스템(LMS) 서버에 접속할수 없습니다.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSuccess.postValue(false);
                mPreferenceClear.postValue(false);
                mMessage.postValue("네트워크 연결을 확인해주세요.");
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

                params.put("id", knuId);
                params.put("pw", password);
                params.put("agentId", "2");
                return params;
            }
        });
    }

    private static String getInputValueById(Source source, String id) {
        Element element = source.getElementById(id);
        return (element != null) ? element.getAttributeValue("value") : null;
    }

    public void clearUser() {
        mPreferenceManager.clear();
    }
}