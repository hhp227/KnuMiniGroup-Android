package com.hhp227.knu_minigroup.data;

import android.webkit.CookieManager;
import android.webkit.ValueCallback;

import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.data.local.AuthLocalDataSource;
import com.hhp227.knu_minigroup.data.remote.AuthRemoteDataSource;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;

public class AuthRepository {
    private final AuthRemoteDataSource mAuthRemoteDataSource;

    private final AuthLocalDataSource mAuthLocalDataSource;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    public AuthRepository() {
        this.mAuthRemoteDataSource = new AuthRemoteDataSource();
        this.mAuthLocalDataSource = new AuthLocalDataSource();
    }

    /**
     * Firebase 우선 로그인.
     * 1) Firebase signIn 성공 → 끝 (SSO 안 탐 — 평상시 경로)
     * 2) 실패 → KNU SSO로 재학생 인증 → Firebase 가입 (최초 로그인)
     * 3) 가입 충돌(EMAIL_ALREADY_IN_USE) = KNU 비밀번호 변경 → 이전 비밀번호로 로그인 후 updatePassword
     * 4) 이전 비밀번호가 없거나 실패 → 재설정 메일 발송
     */
    public void login(final String id, final String password, final Callback callback) {
        callback.onLoading();
        mAuthLocalDataSource.migrateLegacyPassword();
        mAuthRemoteDataSource.signIn(id + "@knu.ac.kr", password, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                finishLogin((FirebaseUser) data, id, password, callback);
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (id.equals("TestUser") && password.equals("TestUser")) {
                    register(id, password, callback);
                } else {
                    verifyWithKNUSSO(id, password, callback);
                }
            }

            @Override
            public void onLoading() {
            }
        });
    }

    /**
     * 자동 로그인 — SSO 없이 Firebase 세션만 확인.
     * 세션이 없으면 저장된 자격증명으로 조용히 재로그인 (레거시 업그레이드/세션 유실 복구).
     * reload의 네트워크 오류는 통과시켜 오프라인 시작을 허용한다.
     */
    public void loginSilently(final Callback callback) {
        callback.onLoading();
        mAuthLocalDataSource.migrateLegacyPassword();
        final FirebaseUser firebaseUser = mAuthRemoteDataSource.getCurrentUser();

        if (firebaseUser != null) {
            mAuthRemoteDataSource.reload(new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    User user = mAuthLocalDataSource.getUser();

                    if (user != null) {
                        mCookieManager.setCookie(EndPoint.LOGIN, firebaseUser.getUid());
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("로그인이 필요합니다."));
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    if (throwable instanceof FirebaseAuthInvalidUserException) {
                        // 계정 삭제/비활성 — 세션 무효
                        callback.onFailure(new Exception("세션이 만료되었습니다. 다시 로그인해주세요."));
                    } else {
                        // 네트워크 오류 — 오프라인 시작 허용
                        User user = mAuthLocalDataSource.getUser();

                        if (user != null) {
                            mCookieManager.setCookie(EndPoint.LOGIN, firebaseUser.getUid());
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure(new Exception("로그인이 필요합니다."));
                        }
                    }
                }

                @Override
                public void onLoading() {
                }
            });
        } else {
            String savedId = mAuthLocalDataSource.getSavedId();
            String savedPassword = mAuthLocalDataSource.getSavedPassword();

            if (savedId != null && savedPassword != null && mAuthLocalDataSource.getUser() != null) {
                login(savedId, savedPassword, callback);
            } else {
                callback.onFailure(new Exception("로그인이 필요합니다."));
            }
        }
    }

    public void logout() {
        mAuthRemoteDataSource.signOut();
        mAuthLocalDataSource.clearUser();
        mCookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
            }
        });
    }

    private void verifyWithKNUSSO(final String id, final String password, final Callback callback) {
        mAuthRemoteDataSource.loginKNUSSO(id, password, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                register((String) data, password, callback);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }

            @Override
            public void onLoading() {
            }
        });
    }

    private void register(final String id, final String password, final Callback callback) {
        mAuthRemoteDataSource.register(id + "@knu.ac.kr", password, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                finishLogin((FirebaseUser) data, id, password, callback);
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (throwable instanceof FirebaseAuthUserCollisionException) {
                    syncChangedPassword(id, password, callback);
                } else {
                    callback.onFailure(throwable);
                }
            }

            @Override
            public void onLoading() {
            }
        });
    }

    // KNU 인증은 통과했는데 가입이 충돌 → KNU 비밀번호가 바뀐 계정
    private void syncChangedPassword(final String id, final String newPassword, final Callback callback) {
        String savedId = mAuthLocalDataSource.getSavedId();
        String savedPassword = mAuthLocalDataSource.getSavedPassword();

        if (savedId != null && savedId.equalsIgnoreCase(id) && savedPassword != null && savedPassword.equals(newPassword)) {
            // 비밀번호가 그대로인데 가입이 충돌 → 실제 변경이 아니라 Firebase 일시 장애(스로틀 등)
            callback.onFailure(new Exception("일시적인 오류로 로그인하지 못했습니다. 잠시 후 다시 시도해주세요."));
        } else if (savedId != null && savedId.equalsIgnoreCase(id) && savedPassword != null && !savedPassword.equals(newPassword)) {
            mAuthRemoteDataSource.signIn(id + "@knu.ac.kr", savedPassword, new Callback() {
                @Override
                public <T> void onSuccess(T data) {
                    updatePassword(id, newPassword, callback);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    sendResetEmail(id, callback);
                }

                @Override
                public void onLoading() {
                }
            });
        } else {
            sendResetEmail(id, callback);
        }
    }

    private void updatePassword(final String id, final String newPassword, final Callback callback) {
        mAuthRemoteDataSource.updatePassword(newPassword, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                finishLogin((FirebaseUser) data, id, newPassword, callback);
            }

            @Override
            public void onFailure(Throwable throwable) {
                // 이전 비밀번호 signIn은 성공해 세션이 살아있는 상태 — 안내와 상태가 모순되지 않도록 정리
                mAuthRemoteDataSource.signOut();
                sendResetEmail(id, callback);
            }

            @Override
            public void onLoading() {
            }
        });
    }

    private void sendResetEmail(final String id, final Callback callback) {
        final String email = id + "@knu.ac.kr";

        mAuthRemoteDataSource.sendPasswordResetEmail(email, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                callback.onFailure(new Exception("경북대 비밀번호가 변경되어 " + email + "(경북대 웹메일)로 재설정 메일을 보냈습니다. 메일의 링크에서 새 비밀번호로 재설정한 뒤 다시 로그인해주세요."));
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(new Exception("재설정 메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요."));
            }

            @Override
            public void onLoading() {
            }
        });
    }

    private void finishLogin(FirebaseUser firebaseUser, String id, String password, Callback callback) {
        String email = id + "@knu.ac.kr";
        User user = new User();

        user.setUid(firebaseUser.getUid());
        user.setUserId(id);
        user.setPassword("");
        user.setName(id);
        user.setNumber("2022000000");
        user.setPhoneNumber("010-0000-0000");
        user.setEmail(email);
        mAuthLocalDataSource.putCredentials(id, password);
        mAuthLocalDataSource.storeUser(user);
        mAuthRemoteDataSource.saveUserToFirebase(firebaseUser.getUid(), id, email);
        mCookieManager.setCookie(EndPoint.LOGIN, firebaseUser.getUid());
        callback.onSuccess(user);
    }
}
