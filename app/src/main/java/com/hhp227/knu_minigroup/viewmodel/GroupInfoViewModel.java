package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class GroupInfoViewModel extends ViewModel {
    public static final int TYPE_REQUEST = 0;

    public static final int TYPE_CANCEL = 1;

    public final Integer mButtonType;

    public final String mGroupId, mGroupName, mGroupImage, mGroupInfo, mGroupDesc, mJoinType, mKey;

    private static final String TAG = GroupInfoViewModel.class.getSimpleName(), LOADING = "loading", JOIN_TYPE = "joinType", MESSAGE = "message";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    public GroupInfoViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGroupId = savedStateHandle.get("grp_id");
        mGroupName = savedStateHandle.get("grp_nm");
        mGroupImage = savedStateHandle.get("img");
        mGroupInfo = savedStateHandle.get("info");
        mGroupDesc = savedStateHandle.get("desc");
        mJoinType = savedStateHandle.get("type");
        mButtonType = savedStateHandle.get("btn_type");
        mKey = savedStateHandle.get("key");
    }

    public void setLoading(boolean bool) {
        mSavedStateHandle.set(LOADING, bool);
    }

    public LiveData<Boolean> isLoading() {
        return mSavedStateHandle.getLiveData(LOADING);
    }

    public void setType(int type) {
        mSavedStateHandle.set(JOIN_TYPE, type);
    }

    public LiveData<Integer> getType() {
        return mSavedStateHandle.getLiveData(JOIN_TYPE);
    }

    public void setMessage(String message) {
        mSavedStateHandle.set(MESSAGE, message);
    }

    public LiveData<String> getMessage() {
        return mSavedStateHandle.getLiveData(MESSAGE);
    }

    public void sendRequest() {
        String tag_json_req = "req_register";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, mButtonType == TYPE_REQUEST ? EndPoint.REGISTER_GROUP : EndPoint.WITHDRAWAL_GROUP, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (mButtonType == TYPE_REQUEST && !response.getBoolean("isError")) {
                        insertGroupToFirebase();
                    } else if (mButtonType == TYPE_CANCEL && !response.getBoolean("isError")) {
                        deleteUserInGroupFromFirebase();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    setLoading(false);
                    setMessage(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                setLoading(false);
                setMessage(error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGroupId);
                if (params.size() > 0) {
                    StringBuilder encodedParams = new StringBuilder();

                    try {
                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            encodedParams.append(URLEncoder.encode(entry.getKey(), getParamsEncoding()));
                            encodedParams.append('=');
                            encodedParams.append(URLEncoder.encode(entry.getValue(), getParamsEncoding()));
                            encodedParams.append('&');
                        }
                        return encodedParams.toString().getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                    }
                }
                return null;
            }
        };

        setLoading(true);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_req);
    }

    private void insertGroupToFirebase() {
        DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");
        Map<String, Object> childUpdates = new HashMap<>();

        groupsReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);

                if (groupItem != null) {
                    Map<String, Boolean> members = groupItem.getMembers() != null && !groupItem.getMembers().containsKey(mPreferenceManager.getUser().getUid()) ? groupItem.getMembers() : new HashMap<>();

                    members.put(mPreferenceManager.getUser().getUid(), mJoinType.equals("0"));
                    groupItem.setMembers(members);
                    groupItem.setMemberCount(members.size());
                    groupsReference.child(mKey).setValue(groupItem);
                }
                setLoading(false);
                setType(TYPE_REQUEST);
                setMessage("신청완료");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                setLoading(false);
                setMessage(databaseError.getMessage());
            }
        });
        childUpdates.put("/" + mPreferenceManager.getUser().getUid() + "/" + mKey, mJoinType.equals("0"));
        userGroupListReference.updateChildren(childUpdates);
    }

    private void deleteUserInGroupFromFirebase() {
        DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");

        groupsReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);

                if (groupItem != null) {
                    if (groupItem.getMembers() != null && groupItem.getMembers().containsKey(mPreferenceManager.getUser().getUid())) {
                        Map<String, Boolean> members = groupItem.getMembers();

                        members.remove(mPreferenceManager.getUser().getUid());
                        groupItem.setMembers(members);
                        groupItem.setMemberCount(members.size());
                    }
                }
                groupsReference.child(mKey).setValue(groupItem);
                setLoading(false);
                setType(TYPE_CANCEL);
                setMessage("신청취소");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                setLoading(false);
                setMessage(databaseError.getMessage());
            }
        });
        userGroupListReference.child(mPreferenceManager.getUser().getUid()).child(mKey).removeValue();
    }
}
