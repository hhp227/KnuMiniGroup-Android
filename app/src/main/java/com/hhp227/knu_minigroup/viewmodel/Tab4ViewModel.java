package com.hhp227.knu_minigroup.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.data.GroupRepository;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

public class Tab4ViewModel extends ViewModel {
    public Boolean mIsAdmin;

    public String mGroupId, mGroupImage, mKey;

    private static final String STATE = "state";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    private final GroupRepository mGroupRepository = new GroupRepository();

    public Tab4ViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mIsAdmin = savedStateHandle.get("admin");
        mGroupId = savedStateHandle.get("grp_id");
        mGroupImage = savedStateHandle.get("grp_img");
        mKey = savedStateHandle.get("key");
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN);
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public void deleteGroup() {
        mGroupRepository.removeGroup(getUser(), mIsAdmin, mKey, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                mSavedStateHandle.set(STATE, new State(false, (Boolean) data, "소모임 " + (mIsAdmin ? "폐쇄" : "탈퇴") + " 완료"));
            }

            @Override
            public void onFailure(Throwable throwable) {
                mSavedStateHandle.set(STATE, new State(false, false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                mSavedStateHandle.set(STATE, new State(true, false, null));
            }
        });
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public boolean isSuccess;

        public String message;

        public State(boolean isLoading, boolean isSuccess, String message) {
            this.isLoading = isLoading;
            this.isSuccess = isSuccess;
            this.message = message;
        }

        private State(Parcel in) {
            isLoading = in.readByte() != 0;
            isSuccess = in.readByte() != 0;
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
            parcel.writeByte((byte) (isSuccess ? 1 : 0));
            parcel.writeString(message);
        }
    }
}
