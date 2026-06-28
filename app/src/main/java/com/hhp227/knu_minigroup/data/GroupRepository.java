package com.hhp227.knu_minigroup.data;

import static com.hhp227.knu_minigroup.app.EndPoint.GROUP_IMAGE;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.volley.util.MultipartRequest;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroupRepository {
    private String mLastKey = null; // 마지막으로 가져온 데이터의 키

    private boolean mStopRequestMore = false;

    public GroupRepository() {
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

    public void setGroup(String cookie, String groupKey, String groupId, String groupName, String description, String joinType, Callback callback) {
        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.UPDATE_GROUP, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!response.getBoolean("isError")) {
                        GroupItem groupItem = new GroupItem();

                        groupItem.setId(groupId);
                        groupItem.setName(response.getString("GRP_NM"));
                        groupItem.setDescription(description);
                        groupItem.setJoinType(joinType);
                        updateGroupDataToFirebase(groupKey, groupItem, callback);
                    } else {
                        callback.onFailure(new IllegalStateException(response.toString()));
                    }
                } catch (JSONException e) {
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
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", groupId);
                params.put("GRP_NM", groupName);
                params.put("TXT", description);
                params.put("JOIN_DIV", joinType);
                try {
                    return encodeParams(params, getParamsEncoding());
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), e);
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }
        });
    }

    public void removeGroup(User user, boolean isAdmin, String key, Callback callback) {
        final DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");

        if (isAdmin) {
            groupsReference.child(key).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey() != null) {
                            userGroupListReference.child(snapshot.getKey()).child(key).removeValue();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onFailure(databaseError.toException());
                }
            });
            articlesReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey() != null) {
                            replysReference.child(snapshot.getKey()).removeValue();
                        }
                    }
                    articlesReference.child(key).removeValue();
                    groupsReference.child(key).removeValue();
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

    private void groupImageUpdate(String cookie, User user, String groupName, String description, Bitmap bitmap, String type, Callback callback) {
        AppController.getInstance().addToRequestQueue(new MultipartRequest(Request.Method.POST, EndPoint.GROUP_IMAGE_UPDATE, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                insertGroupToFirebase(user, groupName, description, bitmap, type, callback);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 302) {
                    // 임시로 넣은코드, 서버에서 왜 이런 응답을 보내는지 이해가 안된다.
                    insertGroupToFirebase(user, groupName, description, bitmap, type, callback);
                } else {
                    callback.onFailure(error);
                }
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

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                if (bitmap != null) {
                    params.put("file", new DataPart(UUID.randomUUID().toString().replace("-", "").concat(".jpg"), getFileDataFromDrawable(bitmap)));
                }
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        });
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

    private void insertGroupToFirebase(User user, String groupName, String description, Bitmap bitmap, String type, Callback callback) {
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
        groupItem.setImage(bitmap != null ? GROUP_IMAGE.replace("{FILE}", key.concat(".jpg")) : EndPoint.BASE_URL + "/ilos/images/community/share_nophoto.gif");
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
                    groupItem.setName(newGroupItem.getName());
                    groupItem.setDescription(newGroupItem.getDescription());
                    groupItem.setJoinType(newGroupItem.getJoinType());
                    query.getRef().setValue(groupItem);
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
