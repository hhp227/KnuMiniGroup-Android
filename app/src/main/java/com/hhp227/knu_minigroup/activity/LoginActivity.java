package com.hhp227.knu_minigroup.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityLoginBinding;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity {
    private static final String TAG = "로그인화면";

    private CookieManager mCookieManager;

    private ProgressDialog mProgressDialog;

    private PreferenceManager mPreferenceManager;

    private ActivityLoginBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 액션바 없음
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mProgressDialog = new ProgressDialog(this);
        mCookieManager = AppController.getInstance().getCookieManager();

        setContentView(mBinding.getRoot());
        mProgressDialog.setCancelable(false);

        // 사용자가 이미 로그인되어있는지 아닌지 확인
        if (mPreferenceManager.getUser() != null) {
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }

        // 로그인 버튼 클릭 이벤트
        mBinding.bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = mBinding.etId.getText().toString();
                final String password = mBinding.etPassword.getText().toString();

                if (!id.isEmpty() && !password.isEmpty()) {
                    if (id.equals("TestUser") && password.equals("TestUser")) {
                        firebaseLogin(id, password);
                    } else {
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
                                        mCookieManager.setCookie(EndPoint.LOGIN, header.getValue());
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
                                params.size();
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
                        };

                        mProgressDialog.setMessage("로그인중...");
                        showProgressDialog();
                        AppController.getInstance().addToRequestQueue(stringRequest);
                    }
                } else {
                    mBinding.etId.setError(id.isEmpty() ? "아이디를 입력하세요." : null);
                    mBinding.etPassword.setError(password.isEmpty() ? "패스워드를 입력하세요." : null);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    private void createLog(final User user) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.CREATE_LOG, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("error")) {

                        // 로그기록 실패
                        updateLog(user);
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
                params.put("user_id", user.getUserId());
                params.put("password", user.getPassword());
                params.put("student_number", user.getNumber());
                params.put("type", "경북대 소모임");
                return params;
            }
        });
    }

    private void getUserInfo(final String id, final String password) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.MY_INFO, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Source source = new Source(response);
                    List<String> extractedList = new ArrayList<>();
                    User user = new User();

                    for (Element element : source.getElementById("content_text").getAllElements(HTMLElementName.TR)) {
                        if (element.getAllElements(HTMLElementName.TD).size() > 1)
                            extractedList.add(String.valueOf(element.getAllElements(HTMLElementName.TD).get(1).getTextExtractor()).split(" ")[0]);
                    }
                    String nameAndNumber = extractedList.get(0);

                    user.setUserId(id);
                    user.setPassword(password);
                    user.setName(nameAndNumber.substring(0, nameAndNumber.lastIndexOf("(")));
                    user.setNumber(nameAndNumber.substring(nameAndNumber.indexOf("(") + 1, nameAndNumber.lastIndexOf(")")));
                    user.setPhoneNumber(extractedList.get(1));
                    user.setEmail(extractedList.get(2));
                    createLog(user);
                    getUserUniqueId(user);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "LMS에 문제가 생겼습니다.", Toast.LENGTH_LONG).show();
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

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }
        });
    }

    private void getUserUniqueId(final User user) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GET_USER_IMAGE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                String imageUrl = source.getElementById("photo").getAttributeValue("src");
                String uid = imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&size"));

                // 화면이동
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                user.setUid(uid);
                mPreferenceManager.storeUser(user);
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

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }
        });
    }

    private void updateLog(User user) {

    }

    private void firebaseLogin(String id, String password) {
        String email = id + "@knu.ac.kr";
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    User user = new User();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    user.setUid(firebaseUser.getUid());
                    user.setUserId(id);
                    user.setPassword(password);
                    user.setName(id);
                    user.setNumber("2022000000");
                    user.setPhoneNumber("010-0000-0000");
                    user.setEmail(email);
                    mCookieManager.setCookie(EndPoint.LOGIN, firebaseUser.getUid());
                    mPreferenceManager.storeUser(user);
                    startActivity(intent);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Firebase error" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void firebaseRegister(String id, String password) {
        String email = id + "@knu.ac.kr";
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    User user = new User();
                    databaseReference.child(firebaseUser.getUid()).setValue(firebaseUser);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    user.setUid(firebaseUser.getUid());
                    user.setUserId(id);
                    user.setPassword(password);
                    user.setName(id);
                    user.setNumber("2022000000");
                    user.setPhoneNumber("010-0000-0000");
                    user.setEmail(email);
                    mPreferenceManager.storeUser(user);
                    startActivity(intent);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Firebase error" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
