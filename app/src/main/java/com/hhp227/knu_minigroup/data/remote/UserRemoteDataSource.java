package com.hhp227.knu_minigroup.data.remote;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.MemberItem;
import com.hhp227.knu_minigroup.helper.Callback;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UserRemoteDataSource {
    private String mGroupKey;

    private String mLastKey = null; // 마지막으로 가져온 데이터의 키

    private boolean mStopRequestMore = false;


    public UserRemoteDataSource() {
    }

    public UserRemoteDataSource(String mGroupKey) {
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

    public boolean isStopRequestMore() {
        return mStopRequestMore;
    }

    public void setLastKey(String lastKey) {
        this.mLastKey = lastKey;
        if (lastKey == null) {
            mStopRequestMore = false; // 새로고침 시 페이징 재개
        }
    }

    /**
     * LMS 서버가 닫혀 멤버 목록을 긁어올 수 없으므로 Groups/{key}/members에서 가입한 uid를 읽어 채운다.
     * members의 값이 false면 가입 신청중이므로 멤버가 아니다.
     */
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
                List<String> uidList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    Boolean value = snapshot.getValue(Boolean.class);

                    if (newLastKey == null) {
                        newLastKey = key; // 이 페이지에서 가장 앞선 키 - 다음 페이지는 이 키 앞을 읽는다
                    }
                    if (key != null && Boolean.TRUE.equals(value)) {
                        uidList.add(0, key);
                    }
                }
                if (newLastKey == null) {
                    mStopRequestMore = true;
                }
                mLastKey = newLastKey; // 다음 페이지 요청을 위해 키 업데이트
                fetchMemberNames(uidList, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
    }

    /**
     * uid만으로는 화면에 이름을 못 쓰므로 Users/{uid}를 하나씩 읽어 채운다.
     * 모든 조회가 끝나야 목록 순서가 보장되므로 남은 개수를 세어 마지막에 한 번만 콜백한다.
     */
    private void fetchMemberNames(List<String> uidList, Callback callback) {
        if (!uidList.isEmpty()) {
            DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("Users");
            MemberItem[] memberItems = new MemberItem[uidList.size()];
            AtomicInteger remaining = new AtomicInteger(uidList.size());

            for (int i = 0; i < uidList.size(); i++) {
                final int index = i;
                final String uid = uidList.get(i);

                usersReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        complete(resolveName(dataSnapshot, uid));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        complete(uid); // 이름을 못 읽어도 멤버 자체는 보여준다
                    }

                    private void complete(String name) {
                        memberItems[index] = new MemberItem(uid, name, null);
                        if (remaining.decrementAndGet() == 0) {
                            callback.onSuccess(new ArrayList<>(Arrays.asList(memberItems)));
                        }
                    }
                });
            }
        } else {
            callback.onSuccess(new ArrayList<MemberItem>());
        }
    }

    /**
     * Users/{uid}에 저장된 이름. 클라이언트마다 저장 포맷이 달라(iOS는 name을 넣고, 구버전 안드로이드는
     * FirebaseUser를 통째로 넣어 name이 없다) 이메일 아이디 → uid 순으로 물러난다.
     */
    private static String resolveName(DataSnapshot dataSnapshot, String uid) {
        String name = dataSnapshot.child("name").getValue(String.class);

        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        String email = dataSnapshot.child("email").getValue(String.class);

        if (!TextUtils.isEmpty(email) && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return uid;
    }
}
