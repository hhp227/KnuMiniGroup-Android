package com.hhp227.knu_minigroup.viewmodel;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.knu_minigroup.dto.YouTubeItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateArticleViewModel extends ViewModel {
    public final List<Object> mContents = new ArrayList<>();

    private static final String TAG = CreateArticleViewModel.class.getSimpleName(), STATE = "state", BITMAP = "bitmap";

    public List<String> mImageList; // TEMP

    private StringBuilder mMakeHtmlContents;

    private final SavedStateHandle mSavedStateHandle;

    public CreateArticleViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;

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
        Log.e("TEST", "type: " + mSavedStateHandle.get("type"));
        if (((int) mSavedStateHandle.get("type")) == 0) {
            actionCreate(title, content);
        } else {
            actionUpdate(title, content);
        }
    }

    private void actionCreate(String title, String content) {
        Log.e("TEST", "actionCreate: " + title + ", " + content);
    }

    private void actionUpdate(String title, String content) {
        Log.e("TEST", "actionUpdate: " + title + ", " + content);
    }

    private void uploadImage(final int position, final Bitmap bitmap) {

    }

    private void uploadProcess(int position, String imageUrl, boolean isYoutube) {

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
