package com.hhp227.knu_minigroup.data.remote;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.helper.StorageCleaner;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;


import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupRemoteDataSource {
    private String mLastKey = null; // 마지막으로 가져온 데이터의 키

    private boolean mStopRequestMore = false;

    public GroupRemoteDataSource() {
    }

    public boolean isStopRequestMore() {
        return mStopRequestMore;
    }

    public void setLastKey(String lastKey) {
        this.mLastKey = lastKey;
    }

    public void getJoinedGroupList(User user, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        Query query = databaseReference.child(user.getUid()).orderByValue().equalTo(true);

        callback.onLoading();
        fetchDataTaskFromFirebase(query, false, new ArrayList<>(), callback);
    }

    public void getNotJoinedGroupList(int offset, int limit, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        Query query = databaseReference.orderByKey().limitToFirst(limit);

        if (mLastKey != null) {
            query = query.startAfter(mLastKey);
        }
        callback.onLoading();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newLastKey = null;
                List<Map.Entry<String, GroupItem>> groupItemList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    GroupItem value = snapshot.getValue(GroupItem.class);

                    if (groupItemList.size() == dataSnapshot.getChildrenCount() - 1) {
                        newLastKey = key; // 마지막 키 저장
                    }
                    if (value != null) {
                        groupItemList.add(new AbstractMap.SimpleEntry<>(key, value));
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
            }
        });
    }

    public void getJoinRequestGroupList(User user, int offset, int limit, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        Query query = databaseReference.child(user.getUid()).orderByValue().equalTo(false);

        fetchDataTaskFromFirebase(query, false, new ArrayList<>(), callback);
    }

    public void getPopularGroupList(String cookie, Callback callback) {
        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                List<GroupItem> popularItemList = new ArrayList<>();

                try {
                    Source source = new Source(response);
                    List<Element> list = source.getAllElements("id", "accordion", false);

                    for (Element element : list) {
                        try {
                            Element menuList = element.getFirstElementByClass("menu_list");

                            if (menuList != null && "accordion".equals(element.getAttributeValue("class"))) {
                                int id = groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"));
                                String imageUrl = EndPoint.BASE_URL + element.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                                String name = element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                                StringBuilder info = new StringBuilder();
                                String description = menuList.getAllElementsByClass("info").get(0).getContent().toString();
                                String joinType = menuList.getAllElementsByClass("info").get(1).getTextExtractor().toString().trim();

                                for (Element span : element.getFirstElement(HTMLElementName.A).getAllElementsByClass("info")) {
                                    String extractedText = span.getTextExtractor().toString();

                                    info.append(extractedText.contains("회원수") ?
                                            extractedText.substring(0, extractedText.lastIndexOf("생성일")).trim() + "\n" :
                                            extractedText + "\n");
                                }

                                GroupItem groupItem = new GroupItem();

                                groupItem.setId(String.valueOf(id));
                                groupItem.setImage(imageUrl);
                                groupItem.setName(name);
                                groupItem.setInfo(info.toString().trim());
                                groupItem.setDescription(description);
                                groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                                popularItemList.add(groupItem);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    callback.onSuccess(popularItemList);
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
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("panel_id", "3");
                params.put("encoding", "utf-8");
                try {
                    return encodeParams(params, getParamsEncoding());
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), e);
                }
            }
        });
    }

    public void getGroup(String cookie, String groupId, String groupImage, Callback callback) {
        String params = "?CLUB_GRP_ID=" + groupId;

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.MODIFY_GROUP + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Source source = new Source(response);
                    GroupItem groupItem = new GroupItem();
                    String joinType = "0";

                    for (Element rbElement : source.getFirstElementByClass("radiobox").getAllElementsByClass("chktype")) {
                        if (rbElement.toString().contains("checked")) {
                            joinType = rbElement.getAttributeValue("value");
                        }
                    }
                    groupItem.setId(groupId);
                    groupItem.setImage(groupImage);
                    groupItem.setName(source.getElementById("wrtGroup").getAttributeValue("value"));
                    groupItem.setDescription(source.getElementById("wrtExplain").getContent().toString());
                    groupItem.setJoinType(joinType);
                    callback.onSuccess(groupItem);
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
        });
    }

    public void addGroup(String cookie, User user, Bitmap bitmap, String title, String description, String type, Callback callback) {
        callback.onLoading();
        if (bitmap != null)
            groupImageUpdate(cookie, user, title, description, bitmap, type, callback);
        else {
            insertGroupToFirebase(user, title, description, null, type, callback);
        }
    }

    /**
     * 커버 이미지를 Firebase Storage에 올리고 다운로드 URL을 그룹 정보에 담는다.
     * LMS 서버가 닫혀 기존 이미지 업로드 엔드포인트는 쓸 수 없다.
     */
    private void groupImageUpdate(String cookie, User user, String groupName, String description, Bitmap bitmap, String type, Callback callback) {
        uploadGroupImage(bitmap, imageUrl -> insertGroupToFirebase(user, groupName, description, imageUrl, type, callback), callback);
    }

    /**
     * 그룹 생성과 그룹 설정이 함께 쓰는 커버 업로드. 성공하면 다운로드 URL을 넘긴다.
     */
    private void uploadGroupImage(Bitmap bitmap, OnImageUploaded onImageUploaded, Callback callback) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("group_images")
                .child(UUID.randomUUID().toString().replace("-", "").concat(".jpg"));

        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        storageReference.putBytes(byteArrayOutputStream.toByteArray())
                .continueWithTask(task -> {
                    if (!task.isSuccessful() && task.getException() != null) {
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> onImageUploaded.onImageUploaded(uri.toString()))
                .addOnFailureListener(e -> {
                    Log.e("GroupRemoteDataSource", "그룹 이미지 업로드 실패", e);
                    callback.onFailure(e);
                });
    }

    private interface OnImageUploaded {
        void onImageUploaded(String imageUrl);
    }

    /**
     * LMS 서버가 닫혀 그룹 수정 엔드포인트를 쓸 수 없으므로 Firebase에 바로 반영한다.
     * 커버를 새로 골랐으면 Storage에 올린 뒤 그 URL까지 함께 갱신한다.
     */
    public void setGroup(String cookie, String groupKey, String groupId, String groupName, String description, String joinType, Bitmap bitmap, Callback callback) {
        GroupItem groupItem = new GroupItem();

        groupItem.setId(groupId);
        groupItem.setName(groupName);
        groupItem.setDescription(description);
        groupItem.setJoinType(joinType);
        callback.onLoading();
        if (bitmap != null) {
            uploadGroupImage(bitmap, imageUrl -> {
                groupItem.setImage(imageUrl);
                updateGroupDataToFirebase(groupKey, groupItem, callback);
            }, callback);
        } else {
            updateGroupDataToFirebase(groupKey, groupItem, callback);
        }
    }

    public void removeGroup(User user, boolean isAdmin, String key, Callback callback) {
        final DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");

        if (isAdmin) {
            // 그룹 노드를 통째로 읽어 멤버 목록과 커버 이미지를 한 번에 확보한다.
            groupsReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot groupSnapshot) {
                    GroupItem groupItem = groupSnapshot.getValue(GroupItem.class);
                    final String groupImage = groupItem != null ? groupItem.getImage() : null;

                    for (DataSnapshot snapshot : groupSnapshot.child("members").getChildren()) {
                        if (snapshot.getKey() != null) {
                            userGroupListReference.child(snapshot.getKey()).child(key).removeValue();
                        }
                    }
                    articlesReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");
                            List<String> articleImageList = new ArrayList<>();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ArticleItem articleItem = snapshot.getValue(ArticleItem.class);

                                if (snapshot.getKey() != null) {
                                    replysReference.child(snapshot.getKey()).removeValue();
                                }
                                if (articleItem != null && articleItem.getImages() != null) {
                                    articleImageList.addAll(articleItem.getImages());
                                }
                            }
                            articlesReference.child(key).removeValue();
                            groupsReference.child(key).removeValue();

                            // 글과 그룹이 사라졌으니 참조를 잃은 이미지도 함께 정리한다.
                            StorageCleaner.delete(articleImageList);
                            StorageCleaner.delete(groupImage);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            callback.onFailure(databaseError.toException());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onFailure(databaseError.toException());
                }
            });
        } else {
            groupsReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);

                    if (groupItem != null) {
                        if (groupItem.getMembers() != null && groupItem.getMembers().containsKey(user.getUid())) {
                            Map<String, Boolean> members = groupItem.getMembers();

                            members.remove(user.getUid());
                            groupItem.setMembers(members);
                            groupItem.setMemberCount(members.size());
                        }
                    }
                    groupsReference.child(key).setValue(groupItem);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onFailure(databaseError.toException());
                }
            });
            userGroupListReference.child(user.getUid()).child(key).removeValue();
        }
        callback.onSuccess(true);
    }

    private void fetchDataTaskFromFirebase(Query query, final boolean isRecursion, List<Map.Entry<String, Object>> groupItemList, Callback callback) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isRecursion) {
                    try {
                        String key = dataSnapshot.getKey();
                        GroupItem value = dataSnapshot.getValue(GroupItem.class);

                        if (value != null) {
                            groupItemList.add(new AbstractMap.SimpleEntry<>(key, value));
                        }
                    } catch (Exception e) {
                        callback.onFailure(e);
                    } finally {
                        callback.onSuccess(groupItemList);
                    }
                } else {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                            String key = snapshot.getKey();

                            if (key != null) {
                                fetchDataTaskFromFirebase(databaseReference.child(key), true, groupItemList, callback);
                            }
                        }
                    } else {
                        callback.onSuccess(groupItemList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private void insertGroupToFirebase(User user, String groupName, String description, String imageUrl, String type, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Map<String, Boolean> members = new HashMap<>();
        GroupItem groupItem = new GroupItem();
        Map<String, Object> childUpdates = new HashMap<>();
        String key = databaseReference.push().getKey();

        members.put(user.getUid(), true);
        groupItem.setId(key);
        groupItem.setTimestamp(System.currentTimeMillis());
        groupItem.setAuthor(user.getName());
        groupItem.setAuthorUid(user.getUid());
        // 커버가 없으면 null로 두고 표시 단계의 placeholder에 맡긴다 (LMS의 기본 이미지 URL은 서버가 닫혀 로드되지 않는다)
        groupItem.setImage(imageUrl);
        groupItem.setName(groupName);
        groupItem.setDescription(description);
        groupItem.setJoinType(type);
        groupItem.setMembers(members);
        groupItem.setMemberCount(members.size());
        childUpdates.put("Groups/" + key, groupItem);
        childUpdates.put("UserGroupList/" + user.getUid() + "/" + key, true);
        databaseReference.updateChildren(childUpdates);
        callback.onSuccess(new AbstractMap.SimpleEntry<>(key, groupItem));
    }

    private void updateGroupDataToFirebase(String groupKey, GroupItem newGroupItem, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        Query query = databaseReference.child(groupKey);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);

                if (groupItem != null) {
                    // 커버는 매번 새 UUID 파일로 올라가므로, 교체된 경우 이전 파일을 지워야 고아가 남지 않는다.
                    final String oldImage = groupItem.getImage();
                    final String newImage = newGroupItem.getImage();

                    groupItem.setName(newGroupItem.getName());
                    groupItem.setDescription(newGroupItem.getDescription());
                    groupItem.setJoinType(newGroupItem.getJoinType());
                    // 커버를 새로 고른 경우에만 교체하고, 아니면 기존 이미지를 유지한다
                    if (newImage != null) {
                        groupItem.setImage(newImage);
                    } else {
                        newGroupItem.setImage(oldImage);
                    }
                    query.getRef().setValue(groupItem, (error, reference) -> {
                        // 갱신이 반영된 뒤에만 이전 커버를 정리한다.
                        if (error == null && newImage != null && !newImage.equals(oldImage)) {
                            StorageCleaner.delete(oldImage);
                        }
                    });
                }
                callback.onSuccess(newGroupItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private byte[] encodeParams(Map<String, String> params, String paramsEncoding) throws UnsupportedEncodingException {
        StringBuilder encodedParams = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
            encodedParams.append('=');
            encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
            encodedParams.append('&');
        }
        return encodedParams.toString().getBytes(paramsEncoding);
    }

    private static int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("[(]|[)]|[,]")[1].trim());
    }
}
