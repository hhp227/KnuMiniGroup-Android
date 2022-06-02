package com.hhp227.knu_minigroup.viewmodel;

import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Header;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
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

public class LoginViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public final MutableLiveData<LoginFormState> mLoginFormState = new MutableLiveData<>();

    private static final String TAG = "로그인화면";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    public void login(String id, String password) {
        if (!id.isEmpty() && !password.isEmpty()) {
            mState.postValue(new State(true, null, null));
            if (id.equals("TestUser") && password.equals("TestUser")) {
                firebaseLogin(id, password);
            } else {
                loginLMS(id, password);
            }
        } else {
            mLoginFormState.postValue(new LoginFormState(id.isEmpty() ? "아이디를 입력하세요." : null, password.isEmpty() ? "패스워드를 입력하세요." : null));
        }
    }

    public void storeUser(User user) {
        mPreferenceManager.storeUser(user);
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    private void loginLMS(String id, String password) {
        String tagStringReq = "req_login_LMS";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("isError");

                    if (!error)
                        getUserInfo(id, password);
                    else
                        mState.postValue(new State(false, null, "로그인 실패"));
                } catch (JSONException e) {
                    mState.postValue(new State(false, null, "로그인 실패" + e.getMessage()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, null, error.getMessage()));
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

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
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
                    mState.postValue(new State(false, null, "LMS에 문제가 생겼습니다."));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                mState.postValue(new State(false, null, "에러 : " + error.getMessage()));
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

                user.setUid(uid);
                mState.postValue(new State(false, user, null));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                mState.postValue(new State(false, null, error.getMessage()));
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

                    user.setUid(firebaseUser.getUid());
                    user.setUserId(id);
                    user.setPassword(password);
                    user.setName(id);
                    user.setNumber("2022000000");
                    user.setPhoneNumber("010-0000-0000");
                    user.setEmail(email);
                    mCookieManager.setCookie(EndPoint.LOGIN, firebaseUser.getUid());
                    mState.postValue(new State(false, user, null));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mState.postValue(new State(false, null, "Firebase error" + e.getMessage()));
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

                    user.setUid(firebaseUser.getUid());
                    user.setUserId(id);
                    user.setPassword(password);
                    user.setName(id);
                    user.setNumber("2022000000");
                    user.setPhoneNumber("010-0000-0000");
                    user.setEmail(email);
                    mState.postValue(new State(false, user, null));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mState.postValue(new State(false, null, "Firebase error" + e.getMessage()));
            }
        });
    }

    public static final class State {
        public boolean isLoading;

        public User user;

        public String message;

        public State(boolean isLoading, User user, String message) {
            this.isLoading = isLoading;
            this.user = user;
            this.message = message;
        }
    }

    public static final class LoginFormState {
        public String emailError;

        public String passwordError;

        public LoginFormState(String emailError, String passwordError) {
            this.emailError = emailError;
            this.passwordError = passwordError;
        }
    }
}
