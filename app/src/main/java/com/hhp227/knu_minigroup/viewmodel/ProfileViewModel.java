package com.hhp227.knu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.webkit.CookieManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import com.hhp227.knu_minigroup.volley.util.MultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public final MutableLiveData<Bitmap> mBitmap = new MutableLiveData<>();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    public ProfileViewModel() {
        mState.postValue(new State(false, false, mPreferenceManager.getUser(), null));
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap.postValue(bitmap);
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
                        mState.postValue(new State(false, true, mPreferenceManager.getUser(), response.getString("message")));
                    } else {
                        mState.postValue(new State(false, false, null, "동기화 실패"));
                    }
                } catch (JSONException e) {
                    mState.postValue(new State(false, false, null, e.getMessage()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, false, null, error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();

                headers.put("Cookie", getCookie());
                return headers;
            }
        };

        mState.postValue(new State(true, false, null, null));
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void uploadImage(final boolean isUpdate) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, isUpdate ? EndPoint.PROFILE_IMAGE_UPDATE : EndPoint.PROFILE_IMAGE_PREVIEW, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                if (isUpdate) {
                    mState.postValue(new State(false, true, mPreferenceManager.getUser(), new String(response.data).contains("성공") ? "수정되었습니다." : "실패했습니다."));
                } else {
                    uploadImage(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, false, null, error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", getCookie());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("FLAG", "FILE");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                Bitmap bitmap = mBitmap.getValue();

                if (bitmap != null) {
                    params.put("img_file", new DataPart(UUID.randomUUID().toString().replace("-", "").concat(".jpg"), getFileDataFromDrawable(bitmap)));
                }
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        };

        AppController.getInstance().addToRequestQueue(multipartRequest);
    }

    public static final class State {
        public boolean isLoading;

        public boolean isSuccess;

        public User user;

        public String message;

        public State(boolean isLoading, boolean isSuccess, User user, String message) {
            this.isLoading = isLoading;
            this.isSuccess = isSuccess;
            this.user = user;
            this.message = message;
        }
    }
}
