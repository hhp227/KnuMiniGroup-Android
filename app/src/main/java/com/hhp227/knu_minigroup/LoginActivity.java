package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import net.htmlparser.jericho.Source;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity {
    private static final String TAG = "로그인화면";
    private Button login;
    private EditText inputId, inputPassword;
    private ProgressDialog progressDialog;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 액션바 없음
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.b_login);
        inputId = findViewById(R.id.et_id);
        inputPassword = findViewById(R.id.et_password);
        preferenceManager = app.AppController.getInstance().getPreferenceManager();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        // 사용자가 이미 로그인되어있는지 아닌지 확인
        if (preferenceManager.getUser() != null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }

        // 로그인 버튼 클릭 이벤트
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = inputId.getText().toString();
                final String password = inputPassword.getText().toString();

                if (!id.isEmpty() && !password.isEmpty()) {
                    progressDialog.setMessage("로그인중...");
                    showProgressDialog();

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "로그인 응답: " + response);

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                boolean error = jsonObject.getBoolean("isError");
                                if (!error)
                                    getUserInfo(id, password);
                                else
                                    Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                Log.e(TAG, "JSON에러 : " + e);
                                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                            }
                            hideProgressDialog();
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
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            List<Header> headers = response.allHeaders;
                            for (Header header : headers)
                                if (header.getName().equals("Set-Cookie") && header.getValue().contains("SESSION_NEWLMS"))
                                    preferenceManager.storeCookie(header.getValue());
                            return super.parseNetworkResponse(response);
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
                        }

                        @Override
                        public byte[] getBody() {
                            Map<String, String> params = new HashMap<>();
                            params.put("usr_id", id);
                            params.put("usr_pwd", password);
                            if (params.size() > 0) {
                                StringBuilder encodedParams = new StringBuilder();
                                try {
                                    for (Map.Entry<String, String> entry : params.entrySet()) {
                                        encodedParams.append(URLEncoder.encode(entry.getKey(), getParamsEncoding()));
                                        encodedParams.append('=');
                                        encodedParams.append(URLEncoder.encode(entry.getValue(), getParamsEncoding()));
                                        encodedParams.append('&');
                                    }
                                    return encodedParams.toString().getBytes(getParamsEncoding());
                                } catch (UnsupportedEncodingException uee) {
                                    throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                                }
                            }
                            return null;
                        }
                    };
                    app.AppController.getInstance().addToRequestQueue(stringRequest);
                } else {
                    Toast.makeText(getApplicationContext(), "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createLog(final User user) {
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.CREATE_LOG, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean("error")) {
                        // 로그기록 성공
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", user.getName());
                params.put("knu_id", user.getUserId());
                params.put("password", user.getPassword());
                params.put("student_number", user.getNumber());
                params.put("real_name", "경북대 소모임");
                return params;
            }
        });
    }

    private void getUserInfo(final String id, final String password) {
        app.AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.NEW_MESSAGE, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!response.getBoolean("isError")) {
                        JSONObject param = response.getJSONObject("param");
                        String name = param.getString("session.origin_nm");
                        String department = param.getString("session.dept_nm");
                        String number = param.getString("session.stu_id");
                        String grade = param.getString("session.grade");
                        String email = param.getString("session.email");

                        User user = new User();
                        user.setUserId(id);
                        user.setPassword(password);
                        user.setName(name);
                        user.setDepartment(department);
                        user.setNumber(number);
                        user.setGrade(grade);
                        user.setEmail(email);

                        createLog(user);
                        getUserUniqueId(user);
                    } else
                        Toast.makeText(getApplicationContext(), "에러 발생", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                Toast.makeText(getApplicationContext(), "에러 : " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", preferenceManager.getCookie());
                return headers;
            }
        });
    }

    private void getUserUniqueId(final User user) {
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GET_USER_IMAGE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                String imageUrl = source.getElementById("photo").getAttributeValue("src");
                String uid = imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&size"));

                user.setUid(uid);

                preferenceManager.storeUser(user);
                // 화면이동
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                hideProgressDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", preferenceManager.getCookie());
                return headers;
            }
        });
    }

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
