package com.hhp227.knu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileViewModel extends ViewModel {
    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> mSuccess = new MutableLiveData<>(false);

    private final MutableLiveData<User> mUser = new MutableLiveData<>(mPreferenceManager.getUser());

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    private final MutableLiveData<Bitmap> mBitmap = new MutableLiveData<>();

    public LiveData<Boolean> isLoading() {
        return mLoading;
    }

    public LiveData<Boolean> isSuccess() {
        return mSuccess;
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap.postValue(bitmap);
    }

    public LiveData<Bitmap> getBitmap() {
        return mBitmap;
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN);
    }

    public void sync() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, EndPoint.SYNC_PROFILE, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!response.getBoolean("isError")) {
                        mLoading.postValue(false);
                        mSuccess.postValue(true);
                        mUser.postValue(mPreferenceManager.getUser());
                        mMessage.postValue(response.getString("message"));
                    } else {
                        mLoading.postValue(false);
                        mMessage.postValue("동기화 실패");
                    }
                } catch (JSONException e) {
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
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();

                headers.put("Cookie", getCookie());
                return headers;
            }
        };

        mLoading.postValue(true);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    /**
     * LMS 서버가 닫혀 프로필 이미지 업로드 엔드포인트를 쓸 수 없으므로 Firebase Storage에 올린다.
     * 표시 쪽은 uid로 URL을 조립하므로(EndPoint.USER_IMAGE) 경로를 uid로 고정하고 덮어쓴다.
     *
     * @param isUpdate LMS 시절 미리보기/확정 2단계 호출의 잔재. Storage는 한 번에 끝나므로 사용하지 않는다.
     */
    public void uploadImage(final boolean isUpdate) {
        Bitmap bitmap = mBitmap.getValue();

        if (bitmap != null) {
            User user = mPreferenceManager.getUser();

            if (user == null || TextUtils.isEmpty(user.getUid())) {
                mMessage.postValue("로그인 정보를 찾을 수 없습니다.");
                return;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference(EndPoint.STORAGE_PROFILE_IMAGE_PATH)
                    .child(user.getUid().concat(".jpg"));

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            mLoading.postValue(true);
            storageReference.putBytes(byteArrayOutputStream.toByteArray())
                    .addOnSuccessListener(taskSnapshot -> {
                        mLoading.postValue(false);
                        mSuccess.postValue(true);
                        mUser.postValue(user);
                        mMessage.postValue("수정되었습니다.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileViewModel", "프로필 이미지 업로드 실패", e);
                        mLoading.postValue(false);
                        mMessage.postValue("실패했습니다.");
                    });
        } else {
            mMessage.postValue("이미지를 선택하세요.");
        }
    }
}
