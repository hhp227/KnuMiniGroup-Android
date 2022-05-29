package com.hhp227.knu_minigroup.viewmodel;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.dto.MessageItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public final List<MessageItem> mMessageItemList = new ArrayList<>();

    public final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages");

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    public Boolean mIsGroupChat;

    public String mReceiver, mFirstMessageKey;

    private String mCursor = null;

    public ChatViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mIsGroupChat = savedStateHandle.get("grp_chat");
        mReceiver = savedStateHandle.get("uid");

        fetchNextPage();
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public LiveData<InputMessageFormState> getMessageFormState() {
        return mSavedStateHandle.getLiveData("messageFormState");
    }

    @SuppressLint("todo")
    public void fetchMessageList(Query query, final int prevCnt, final String prevCursor) {
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @SuppressLint("todo")
    public void fetchNextPage() {
        mCursor = null;

        mState.postValue(new State(false, Collections.emptyList(), true, null));
    }

    public void actionSend(String text) {
        if (!text.isEmpty()) {
            Map<String, Object> map = new HashMap<>();

            map.put("from", getUser().getUid());
            map.put("name", getUser().getName());
            map.put("message", text);
            map.put("type", "text");
            map.put("seen", false);
            map.put("timestamp", System.currentTimeMillis());
            if (mIsGroupChat) {
                mDatabaseReference.child(mReceiver).push().setValue(map);
            } else {
                String receiverPath = mReceiver + "/" + getUser().getUid() + "/";
                String senderPath = getUser().getUid() + "/" + mReceiver + "/";
                String pushId = mDatabaseReference.child(getUser().getUid()).child(mReceiver).push().getKey();
                Map<String, Object> messageMap = new HashMap<>();

                messageMap.put(receiverPath.concat(pushId), map);
                messageMap.put(senderPath.concat(pushId), map);
                mDatabaseReference.updateChildren(messageMap);
            }
        } else {
            mSavedStateHandle.set("messageFormState", new InputMessageFormState("메시지를 입력하세요."));
        }
    }

    public void addAll(List<MessageItem> messageItemList) {
        mMessageItemList.addAll(messageItemList);
    }

    public static final class State {
        public boolean isLoading;

        public List<MessageItem> messageItemList;

        public boolean hasRequestedMore;

        public String message;

        public State(boolean isLoading, List<MessageItem> messageItemList, boolean hasRequestedMore, String message) {
            this.isLoading = isLoading;
            this.messageItemList = messageItemList;
            this.hasRequestedMore = hasRequestedMore;
            this.message = message;
        }
    }

    public static final class InputMessageFormState implements Parcelable {
        public String messageError;

        public InputMessageFormState(String messageError) {
            this.messageError = messageError;
        }

        protected InputMessageFormState(Parcel in) {
            messageError = in.readString();
        }

        public static final Creator<InputMessageFormState> CREATOR = new Creator<InputMessageFormState>() {
            @Override
            public InputMessageFormState createFromParcel(Parcel in) {
                return new InputMessageFormState(in);
            }

            @Override
            public InputMessageFormState[] newArray(int size) {
                return new InputMessageFormState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(messageError);
        }
    }
}
