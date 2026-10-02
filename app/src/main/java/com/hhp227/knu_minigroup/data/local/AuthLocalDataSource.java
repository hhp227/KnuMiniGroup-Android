package com.hhp227.knu_minigroup.data.local;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AuthLocalDataSource {
    private static final String TAG = "AuthLocalDataSource";

    // 로그아웃(PreferenceManager.clear())에도 유지되어야 해서 별도 파일에 저장
    private static final String PREF_NAME = "AuthCredentials";

    private static final String KEY_SAVED_ID = "saved_id";

    private static final String KEY_SAVED_PASSWORD = "saved_pwd";

    private static final String KEYSTORE_ALIAS = "auth_credentials_key";

    private static final String PLAIN_PREFIX = "plain:"; // API 23 미만 폴백

    private static final String CIPHER_PREFIX = "aes:";

    private final SharedPreferences mSharedPreferences;

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    public AuthLocalDataSource() {
        mSharedPreferences = AppController.getInstance().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void putCredentials(String id, String password) {
        mSharedPreferences.edit()
                .putString(KEY_SAVED_ID, id)
                .putString(KEY_SAVED_PASSWORD, encrypt(password))
                .apply();
    }

    public String getSavedId() {
        return mSharedPreferences.getString(KEY_SAVED_ID, null);
    }

    public String getSavedPassword() {
        String stored = mSharedPreferences.getString(KEY_SAVED_PASSWORD, null);
        return stored != null ? decrypt(stored) : null;
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    // 비밀번호는 평문 prefs에 남기지 않는다 — 자격증명 저장소가 원본
    public void storeUser(User user) {
        user.setPassword("");
        mPreferenceManager.storeUser(user);
    }

    public void clearUser() {
        mPreferenceManager.clear();
    }

    // 구버전이 prefs에 남긴 평문 비밀번호를 암호화 저장소로 옮긴다 (멱등)
    public void migrateLegacyPassword() {
        User user = mPreferenceManager.getUser();

        if (user != null && user.getPassword() != null && !user.getPassword().isEmpty()) {
            putCredentials(user.getUserId(), user.getPassword());
            storeUser(user);
        }
    }

    private String encrypt(String plain) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return PLAIN_PREFIX + plain;
        }
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return CIPHER_PREFIX + Base64.encodeToString(iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.w(TAG, "자격증명 암호화 실패 — 평문 폴백", e);
            return PLAIN_PREFIX + plain;
        }
    }

    private String decrypt(String stored) {
        if (stored.startsWith(PLAIN_PREFIX)) {
            return stored.substring(PLAIN_PREFIX.length());
        }
        if (!stored.startsWith(CIPHER_PREFIX) || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return null;
        }
        try {
            String[] parts = stored.substring(CIPHER_PREFIX.length()).split(":");
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] encrypted = Base64.decode(parts[1], Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.w(TAG, "자격증명 복호화 실패 — 저장값 없음으로 처리", e);
            return null; // 키 유실(백업 복원 등) — 없는 것으로 취급하면 메일 폴백으로 흐른다
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");

        keyStore.load(null);
        if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEYSTORE_ALIAS, null)).getSecretKey();
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        keyGenerator.init(new KeyGenParameterSpec.Builder(KEYSTORE_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        return keyGenerator.generateKey();
    }
}
