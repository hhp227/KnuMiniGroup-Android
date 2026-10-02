package com.hhp227.knu_minigroup.data.remote;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.helper.Callback;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class AuthRemoteDataSource {
    private final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    public FirebaseUser getCurrentUser() {
        return mFirebaseAuth.getCurrentUser();
    }

    // KNU SSO는 재학생 검증 용도로만 쓴다 — 세션 쿠키(SESSION_NEWLMS)는 저장하지 않는다 (LMS 폐쇄)
    public void loginKNUSSO(final String id, final String password, final Callback callback) {
        String tagStringReq = "req_login_KNU";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Source source = new Source(response);

                    source.fullSequentialParse();
                    String userId = getInputValueById(source, "userId");
                    String resultCode = getInputValueById(source, "resultCode");
                    String resultMessage = getInputValueById(source, "resultMessage");

                    if (resultCode != null && resultCode.equals("000000")) {
                        callback.onSuccess(userId != null ? userId : id);
                    } else {
                        callback.onFailure(new Exception(resultMessage != null && !resultMessage.isEmpty() ? resultMessage : "아이디 또는 비밀번호가 올바르지 않습니다."));
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        }) {
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

    public void signIn(String email, String password, final Callback callback) {
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        callback.onSuccess(authResult.getUser());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void register(String email, String password, final Callback callback) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        callback.onSuccess(authResult.getUser());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void updatePassword(String newPassword, final Callback callback) {
        final FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            callback.onFailure(new Exception("로그인 상태가 아닙니다."));
            return;
        }
        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(firebaseUser);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void sendPasswordResetEmail(String email, final Callback callback) {
        mFirebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void reload(final Callback callback) {
        final FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            callback.onFailure(new Exception("로그인 상태가 아닙니다."));
            return;
        }
        firebaseUser.reload()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(firebaseUser);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void signOut() {
        mFirebaseAuth.signOut();
    }

    /**
     * 멤버 목록이 uid로 이름을 찾을 수 있도록 Users/{uid}를 채운다.
     * 예전에는 FirebaseUser 객체를 통째로 넣어 name이 없었으므로, iOS와 같은 포맷(uid/email/name)으로 맞춘다.
     * 로그인할 때마다 호출해 기존 계정도 다음 로그인 때 보정되게 한다 (setValue가 아닌 병합이라 다른 필드는 보존).
     */
    public void saveUserToFirebase(String uid, String id, String email) {
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("uid", uid);
        childUpdates.put("email", email);
        childUpdates.put("name", id);
        FirebaseDatabase.getInstance().getReference("Users").child(uid).updateChildren(childUpdates);
    }

    private static String getInputValueById(Source source, String id) {
        Element element = source.getElementById(id);
        return (element != null) ? element.getAttributeValue("value") : null;
    }
}
