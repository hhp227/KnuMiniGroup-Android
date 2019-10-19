package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.user.User;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends Activity {
    private static int SPLASH_TIME_OUT = 1250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
