package com.hhp227.knu_minigroup.data;

import android.util.Log;
import androidx.annotation.NonNull;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.dto.MemberItem;
import com.hhp227.knu_minigroup.helper.Callback;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private String mGroupKey;

    private String mLastKey = null; // 마지막으로 가져온 데이터의 키

    private boolean mStopRequestMore = false;


    public UserRepository() {
    }

    public UserRepository(String mGroupKey) {
        this.mGroupKey = mGroupKey;
    }

    public void getManagedMemberList(String cookie, String groupId, Callback callback) {
        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_MEMBER_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Source source = new Source(response);
                    Element listZone = source.getElementById("listZone");
                    List<MemberItem> memberItemList = new ArrayList<>();

                    if (listZone != null) {
                        for (Element element : listZone.getChildElements()) {
                            List<Element> tdList = element.getAllElements(HTMLElementName.TD);

                            if (tdList.size() >= 7) {
                                String studentNumber = tdList.get(0).getContent().getFirstElement().getAttributeValue("value");
                                String imageUrl = tdList.get(1).getContent().getFirstElement().getAttributeValue("src");
                                String uid = imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&ext"));
                                String name = tdList.get(2).getContent().toString();
                                String deptName = tdList.get(3).getTextExtractor().toString();
                                String division = tdList.get(5).getContent().toString();
                                String date = tdList.get(6).getContent().toString();

                                memberItemList.add(new MemberItem(uid, name, null, studentNumber, deptName, division, date));
                            }
                        }
                    }
                    callback.onSuccess(memberItemList);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", groupId);
                return params;
            }
        });
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
                                //User value = snapshot.getValue(User.class);
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
