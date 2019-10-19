package com.hhp227.knu_minigroup.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.hhp227.knu_minigroup.user.User;

public class PreferenceManager {
    private static String TAG = "세션메니져";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;

    final int PRIVATE_MOD = 0;

    // SharedPreference 파일 이름
    private static final String PREF_NAME = "ApplicationLogin";

    private static final String KEY_USER_ID = "usr_id";
    private static final String KEY_USER_PASSWORD = "usr_pwd";
    private static final String KEY_SESSION_ID = "session_id";

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MOD);
        editor = sharedPreferences.edit();
    }

    public void storeUser(User user) {
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USER_PASSWORD, user.getPassword());
        editor.commit();

        Log.i(TAG, "사용자 Session 저장 : " + user.getUserId());
    }

    public User getUser() {
        if(sharedPreferences.getString(KEY_USER_ID, null) != null) {
            String knuId = sharedPreferences.getString(KEY_USER_ID, null);
            String password = sharedPreferences.getString(KEY_USER_PASSWORD, null);
            User user = new User(knuId, password);

            return user;
        }
        return null;
    }

    public void storeSessionId(String session) {
        editor.putString(KEY_SESSION_ID, session);
        editor.commit();
    }

    public String getSessionId() {
        return sharedPreferences.getString(KEY_SESSION_ID, null);
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}
