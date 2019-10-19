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

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MOD);
        editor = sharedPreferences.edit();
    }

    public void storeUser(User user) {
        Log.e(TAG, "사용자 Session 저장" + user.getName() + ", " + user.getKnuId());
    }

    public User getUser() {
        User user = new User();
        return user;
    }
}
