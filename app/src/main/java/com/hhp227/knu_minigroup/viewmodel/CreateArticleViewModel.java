package com.hhp227.knu_minigroup.viewmodel;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import net.htmlparser.jericho.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateArticleViewModel extends ViewModel {
    public final List<Object> mContents = new ArrayList<>();

    private static final String TAG = CreateArticleViewModel.class.getSimpleName(), STATE = "state", BITMAP = "bitmap";

    public List<String> mImageList; // TEMP

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private StringBuilder mMakeHtmlContents;

    private final String mGrpId, mGrpKey, mArtlNum, mArtlKey;

    private final SavedStateHandle mSavedStateHandle;

    public CreateArticleViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGrpId = savedStateHandle.get("grp_id");
        mGrpKey = savedStateHandle.get("grp_key");
        mArtlNum = savedStateHandle.get("artl_num");
        mArtlKey = savedStateHandle.get("artl_key");
        mImageList = savedStateHandle.get("img");

        if (mImageList != null && !mImageList.isEmpty()) {
            mContents.addAll(mImageList);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        mSavedStateHandle.set(BITMAP, bitmap);
    }

    public LiveData<Bitmap> getBitmapState() {
        return mSavedStateHandle.getLiveData(BITMAP);
    }

    public void setYoutube(YouTubeItem youtubeItem) {
        mSavedStateHandle.set("vid", youtubeItem);
    }

    public LiveData<YouTubeItem> getYoutubeState() {
        return mSavedStateHandle.getLiveData("vid");
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public LiveData<ArticleFormState> getArticleFormState() {
        return mSavedStateHandle.getLiveData("articleFormState");
    }

    public <T> void addItem(T content) {
        mContents.add(content);
    }

    public <T> void addItem(int position, T content) {
        mContents.add(position, content);
    }

    public void removeItem(int position) {
        mContents.remove(position);
    }

    public void actionSend(String title, String content) {
        Log.e("TEST", "actionSend: " + title + ", " + content + ", " + mContents);
        if (!title.isEmpty() && !(TextUtils.isEmpty(content) && mContents.size() == 0)) {
            mMakeHtmlContents = new StringBuilder();
            mImageList = new ArrayList<>();

            mSavedStateHandle.set(STATE, new State(true, null, Collections.emptyList(), null));
            if (mContents.size() > 0) {
                int position = 0;

                if (mContents.get(position) instanceof String) {
                    String image = (String) mContents.get(position);

                    uploadProcess(position, image, false);
                } else if (mContents.get(position) instanceof Bitmap) {////////////// 리팩토링 요망
                    Bitmap bitmap = (Bitmap) mContents.get(position);// 수정

                    uploadImage(position, bitmap); // 수정
                } else if (mContents.get(position) instanceof YouTubeItem) {
                    YouTubeItem youTubeItem = (YouTubeItem) mContents.get(position);

                    uploadProcess(position, youTubeItem.videoId, true);
                }
            } else {
                typeCheck(title, content);
            }
        } else {
            mSavedStateHandle.set("articleFormState", new ArticleFormState((title.isEmpty() ? "제목" : "내용") + "을 입력하세요."));
        }
    }

    private void typeCheck(String title, String content) {
        if (((int) mSavedStateHandle.get("type")) == 0) {
            actionCreate(title, content);
        } else {
            actionUpdate(title, content);
        }
    }

    private void actionCreate(final String title, final String content) {
        Log.e("TEST", "actionCreate: " + title + ", " + content + ", escapeHtml: " + Html.fromHtml(content).toString());
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.WRITE_ARTICLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("isError");

                    if (!error) {
                        getArticleId(title, Html.fromHtml(content).toString().trim());
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "에러 : " + e.getMessage());
                    mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), e.getMessage()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("SBJT", title);
                params.put("CLUB_GRP_ID", mGrpId);
                params.put("TXT", content);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private void actionUpdate(final String title, final String content) {
        Log.e("TEST", "actionUpdate: " + title + ", " + content);
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_ARTICLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("TEST", "onResponse: " + response);
                try {
                    /*Intent intent = new Intent(CreateArticleActivity.this, ArticleActivity.class);

                    Toast.makeText(getApplicationContext(), "수정완료", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK, intent);
                    finish();*/
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    initFirebaseData(title, content);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGrpId);
                params.put("ARTL_NUM", mArtlNum);
                params.put("SBJT", title);
                params.put("TXT", content);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private void getArticleId(String title, String content) {
        String params = "?CLUB_GRP_ID=" + mGrpId + "&displayL=1";

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                String artlNum = source.getFirstElementByClass("comment_wrap").getAttributeValue("num");

                insertArticleToFirebase(artlNum, title, content);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), error.getMessage()));
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

    private void uploadImage(final int position, final Bitmap bitmap) {

    }

    private void uploadProcess(int position, String imageUrl, boolean isYoutube) {

    }

    private void insertArticleToFirebase(String artlNum, String title, String content) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        Map<String, Object> map = new HashMap<>();
        User user = mPreferenceManager.getUser();

        map.put("id", artlNum);
        map.put("uid", user.getUid());
        map.put("name", user.getName());
        map.put("title", title);
        map.put("timestamp", System.currentTimeMillis());
        map.put("content", TextUtils.isEmpty(content) ? null : content);
        map.put("images", mImageList);
        map.put("youtube", getYoutubeState().getValue());
        databaseReference.child(mGrpKey).push().setValue(map);
        mSavedStateHandle.set(STATE, new State(false, artlNum, Collections.emptyList(), null));
    }

    private void initFirebaseData(String title, String content) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        updateArticleDataToFirebase(databaseReference.child(mGrpKey).child(mArtlKey), title, content);
    }

    private void updateArticleDataToFirebase(final Query query, final String title, final String content) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);

                if (articleItem != null) {
                    articleItem.setTitle(title);
                    articleItem.setContent(TextUtils.isEmpty(content) ? null : content);
                    articleItem.setImages(mImageList.isEmpty() ? null : mImageList);
                    articleItem.setYoutube(getYoutubeState().getValue());
                    query.getRef().setValue(articleItem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), databaseError.getMessage()));
            }
        });
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public String articleId;

        public List<Object> contents;

        public String message;

        public State(boolean isLoading, String articleId, List<Object> contents, String message) {
            this.isLoading = isLoading;
            this.articleId = articleId;
            this.contents = contents;
            this.message = message;
        }

        protected State(Parcel in) {
            isLoading = in.readByte() != 0;
            articleId = in.readString();
            message = in.readString();
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeByte((byte) (isLoading ? 1 : 0));
            parcel.writeString(articleId);
            parcel.writeString(message);
        }
    }

    public static final class ArticleFormState implements Parcelable {
        public String message;

        public ArticleFormState(String message) {
            this.message = message;
        }

        protected ArticleFormState(Parcel in) {
            message = in.readString();
        }

        public static final Creator<ArticleFormState> CREATOR = new Creator<ArticleFormState>() {
            @Override
            public ArticleFormState createFromParcel(Parcel in) {
                return new ArticleFormState(in);
            }

            @Override
            public ArticleFormState[] newArray(int size) {
                return new ArticleFormState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(message);
        }
    }
}
