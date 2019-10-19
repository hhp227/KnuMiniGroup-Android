package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    private static final String TAG = "로그인화면";
    private Button login;
    private EditText inputId, inputPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 액션바 없음
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.bLogin);
        inputId = findViewById(R.id.etId);
        inputPassword = findViewById(R.id.etPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // 사용자가 이미 로그인되어있는지 아닌지 확인
        if(AppController.getInstance().getPreferenceManager().getUser() != null) {

        }

        // 로그인 버튼 클릭 이벤트
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = inputId.getText().toString();
                final String password = inputPassword.getText().toString();

                if(!id.isEmpty() && !password.isEmpty()) {
                    progressDialog.setMessage("로그인중...");
                    showProgressDialog();

                    String URL_KNU_LOGIN = "";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_KNU_LOGIN, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "로그인 에러: " + error.getMessage());
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                            hideProgressDialog();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("user.usr_id", id);
                            params.put("user.passwd", password);

                            return params;
                        }
                    };
                    AppController.getInstance().addToRequestQueue(stringRequest, "request_knu_login");
                } else {
                    Toast.makeText(getApplicationContext(), "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    private void showProgressDialog() {
        if(!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
