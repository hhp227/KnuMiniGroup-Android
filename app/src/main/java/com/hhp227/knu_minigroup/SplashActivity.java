package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends Activity {
    private static int SPLASH_TIME_OUT = 1250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 액션바 안보이기
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.LOGIN, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
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
                                app.AppController.getInstance().getPreferenceManager().storeCookie(header.getValue());
                        return super.parseNetworkResponse(response);
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        User user = app.AppController.getInstance().getPreferenceManager().getUser();

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
