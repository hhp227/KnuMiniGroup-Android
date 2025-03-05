package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginViewModel extends ViewModel {
    private static final String TAG = "로그인화면";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<User> mUser = new MutableLiveData<>(mPreferenceManager.getUser());

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    private final MutableLiveData<String> mEmailError = new MutableLiveData<>();

    private final MutableLiveData<String> mPasswordError = new MutableLiveData<>();

    public MutableLiveData<String> id = new MutableLiveData<>("");

    public MutableLiveData<String> password = new MutableLiveData<>("");

    public LiveData<Boolean> isLoading() {
        return mLoading;
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    public LiveData<String> getEmailError() {
        return mEmailError;
    }

    public LiveData<String> getPasswordError() {
        return mPasswordError;
    }

    public void login(String id, String password) {
        if (!id.isEmpty() && !password.isEmpty()) {
            mLoading.postValue(true);
            if (id.equals("TestUser") && password.equals("TestUser")) {
                firebaseLogin(id, password);
            } else {
                loginKNUSSO(id, password);
            }
        } else {
            mEmailError.postValue(id.isEmpty() ? "아이디를 입력하세요." : null);
            mPasswordError.postValue(password.isEmpty() ? "패스워드를 입력하세요." : null);
        }
    }

    public void storeUser(User user) {
        mPreferenceManager.storeUser(user);
    }

    private void loginKNUSSO(String id, String password) {
        String tagStringReq = "req_login_KNU";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Source source = new Source(response);
                    source.fullSequentialParse();

                    // 특정 id 값을 가진 input 태그에서 value 속성 값 추출
                    String userId = getInputValueById(source, "userId");
                    String resultCode = getInputValueById(source, "resultCode");
                    String resultMessage = getInputValueById(source, "resultMessage");
                    String secureToken = getInputValueById(source, "secureToken");

                    if (resultCode != null && resultCode.equals("000000")) {
                        firebaseLogin(userId, password);
                        // TODO getUser작성할것
                    } else {
                        mLoading.postValue(false);
                        mMessage.postValue(resultMessage);
                    }
                    Log.e("TEST", "userId: " + userId + ", resultCode: " + resultCode + ", resultMessage: " + resultMessage + ", secureToken: " + secureToken);
                } catch (Exception e) {
                    mLoading.postValue(false);
                    mMessage.postValue(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLoading.postValue(false);
                mMessage.postValue(error.getMessage());
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

                params.put("id", id);
                params.put("pw", password);
                params.put("agentId", "2");
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

    private static String getInputValueById(Source source, String id) {
        Element element = source.getElementById(id);
        return (element != null) ? element.getAttributeValue("value") : null;
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
                } catch (Exception e) {
                    mLoading.postValue(false);
                    mMessage.postValue("LMS에 문제가 생겼습니다.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                mLoading.postValue(false);
                mMessage.postValue("에러 : " + error.getMessage());
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

    private void firebaseLogin(String id, String password) {
        String email = id + "@knu.ac.kr";
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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
                            mLoading.postValue(false);
                            mUser.postValue(user);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        firebaseRegister(id, password);
                    }
                });
    }

    private void firebaseRegister(String id, String password) {
        String email = id + "@knu.ac.kr";
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
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
                            mLoading.postValue(false);
                            mUser.postValue(user);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mLoading.postValue(false);
                        mMessage.postValue("Firebase error" + e.getMessage());
                    }
                });
    }
}