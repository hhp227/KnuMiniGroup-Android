package com.hhp227.knu_minigroup.data;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private final String mGroupKey;

    private String mLastKey = null; // 마지막으로 가져온 데이터의 키

    private boolean mStopRequestMore = false;


    public UserRepository(String mGroupKey) {
        this.mGroupKey = mGroupKey;
    }

    public void getUserList(int limit, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        Query query = databaseReference.child(mGroupKey).child("members").orderByKey().limitToLast(limit);

        if (mLastKey != null) {
            query = query.endBefore(mLastKey);
        }
        callback.onLoading();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newLastKey = null;
                List<Map.Entry<String, GroupItem>> groupItemList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    boolean value = snapshot.getValue(Boolean.class);

                    if (groupItemList.isEmpty()) {
                        newLastKey = key; // 마지막 키 저장
                    }
                    if (key != null && value) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                        Query query = databaseReference.child(key);

                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User value = snapshot.getValue(User.class);
                                Log.e("TEST", "dataSnapshot: " + dataSnapshot);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        Log.e("TEST", "key: " + key + ", value: " + value);
                        //groupItemList.add(0, new AbstractMap.SimpleEntry<>(key, value));
                    }
                }
                if (newLastKey == null) {
                    mStopRequestMore = true;
                }
                mLastKey = newLastKey; // 다음 페이지 요청을 위해 키 업데이트
                callback.onSuccess(groupItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
    }
}