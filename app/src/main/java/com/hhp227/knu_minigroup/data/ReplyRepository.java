package com.hhp227.knu_minigroup.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.dto.ReplyItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReplyRepository {
    private final String mArticleKey;

    public ReplyRepository(String articleKey) {
        this.mArticleKey = articleKey;
    }

    public void getReplyList(Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        List<Map.Entry<String, ReplyItem>> replyItemList = new ArrayList<>();

        databaseReference.child(mArticleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ReplyItem value = snapshot.getValue(ReplyItem.class);

                    replyItemList.add(new AbstractMap.SimpleEntry<>(key, value));
                }
                callback.onSuccess(replyItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public void addReply(User user, String text, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        ReplyItem replyItem = new ReplyItem();

        callback.onLoading();
        replyItem.setUid(user.getUid());
        replyItem.setName(user.getName());
        replyItem.setTimestamp(System.currentTimeMillis());
        replyItem.setReply(text);
        databaseReference.child(mArticleKey).push().setValue(replyItem);
        callback.onSuccess(true);
    }

    public void setReply(String replyKey, String text, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        Query query = databaseReference.child(mArticleKey).child(replyKey);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ReplyItem replyItem = dataSnapshot.getValue(ReplyItem.class);

                    if (replyItem != null) {
                        replyItem.setReply(text + "\n");
                    }
                    query.getRef().setValue(replyItem);
                }
                callback.onSuccess(text);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });

        callback.onLoading();
    }

    public void removeReply(String replyKey, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");

        callback.onLoading();
        databaseReference.child(mArticleKey).child(replyKey).removeValue();
        callback.onSuccess(true);
    }
}