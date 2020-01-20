package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends Activity {
    private static final int SPLASH_TIME_OUT = 1250;
    private static final String TAG = SplashActivity.class.getSimpleName();
    private PreferenceManager mPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 액션바 안보이기
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mPreferenceManager = app.AppController.getInstance().getPreferenceManager();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.LOGIN, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("isError")) {
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            } else {
                                mPreferenceManager.clear();
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                Toast.makeText(getApplicationContext(), "패스워드가 변경되었습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON에러 : " + e);
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                        } finally {
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(SplashActivity.class.getSimpleName(), error.getMessage());
                    }
                }) {
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        List<Header> headers = response.allHeaders;
                        for (Header header : headers)
                            if (header.getName().equals("Set-Cookie") && header.getValue().contains("SESSION_NEWLMS"))
                                mPreferenceManager.storeCookie(header.getValue());
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
        }, SPLASH_TIME_OUT);
    }
}
