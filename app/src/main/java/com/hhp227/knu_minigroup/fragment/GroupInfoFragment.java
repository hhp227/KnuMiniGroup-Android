package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.MainActivity;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.RequestActivity;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class GroupInfoFragment extends DialogFragment {
    private static final String TAG = "정보창";
    private static int mButtonType;
    private static String mGroupId, mGroupName, mGroupImage, mGroupInfo, mGroupDesc, mJoinType, mKey;
    private PreferenceManager mPreferenceManager;
    private ProgressDialog mProgressDialog;

    public static GroupInfoFragment newInstance() {
        Bundle args = new Bundle();

        GroupInfoFragment fragment = new GroupInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroupId = getArguments().getString("grp_id");
            mGroupName = getArguments().getString("grp_nm");
            mGroupImage = getArguments().getString("img");
            mGroupInfo = getArguments().getString("info");
            mGroupDesc = getArguments().getString("desc");
            mJoinType = getArguments().getString("type");
            mButtonType = getArguments().getInt("btn_type");
            mKey = getArguments().getString("key");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        View rootView = inflater.inflate(R.layout.fragment_group_info, container, false);
        Button button = rootView.findViewById(R.id.b_request);
        Button close = rootView.findViewById(R.id.b_close);
        ImageView image = rootView.findViewById(R.id.iv_group_image);
        TextView name = rootView.findViewById(R.id.tv_name);
        TextView info = rootView.findViewById(R.id.tv_info);
        TextView desc = rootView.findViewById(R.id.tv_desciption);
        mPreferenceManager = app.AppController.getInstance().getPreferenceManager();
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setMessage("요청중...");
                showProgressDialog();
                String tag_json_req = "req_register";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, mButtonType == 0 ? EndPoint.REGISTER_GROUP : EndPoint.WITHDRAWAL_GROUP, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (mButtonType == 0 && !response.getBoolean("isError")) {
                                Toast.makeText(getContext(), "신청완료", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                getActivity().setResult(RESULT_OK, intent);
                                getActivity().finish();
                                insertGroupToFirebase();
                            } else if (mButtonType == 1 && !response.getBoolean("isError")) {
                                Toast.makeText(getContext(), "신청취소", Toast.LENGTH_LONG).show();
                                ((RequestActivity) getActivity()).refresh();
                                GroupInfoFragment.this.dismiss();
                                deleteUserInGroupFromFirebase();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        hideProgressDialog();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage());
                        hideProgressDialog();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", mPreferenceManager.getCookie());
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
                app.AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag_json_req);
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupInfoFragment.this.dismiss();
            }
        });
        name.setText(mGroupName);
        info.setText(mGroupInfo);
        desc.setText(mGroupDesc);
        desc.setMaxLines(6);
        button.setText(mButtonType == 0 ? "가입신청" : "신청취소");
        Glide.with(this)
                .load(mGroupImage)
                .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(image);

        return rootView;
    }

    private void insertGroupToFirebase() {
        DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");
        groupsReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;
                GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);
                Map<String, Boolean> members = groupItem.getMembers() != null && !groupItem.getMembers().containsKey(mPreferenceManager.getUser().getUid()) ? groupItem.getMembers() : new HashMap<String, Boolean>();
                members.put(mPreferenceManager.getUser().getUid(), mJoinType.equals("0"));
                groupItem.setMembers(members);
                groupItem.setMemberCount(members.size());
                groupsReference.child(mKey).setValue(groupItem);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + mPreferenceManager.getUser().getUid() + "/" + mKey, mJoinType.equals("0"));
        userGroupListReference.updateChildren(childUpdates);
    }

    private void deleteUserInGroupFromFirebase() {
        DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");
        groupsReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;
                GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);
                if (groupItem.getMembers() != null && groupItem.getMembers().containsKey(mPreferenceManager.getUser().getUid())) {
                    Map<String, Boolean> members = groupItem.getMembers();
                    members.remove(mPreferenceManager.getUser().getUid());
                    groupItem.setMembers(members);
                    groupItem.setMemberCount(members.size());
                }
                groupsReference.child(mKey).setValue(groupItem);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
        userGroupListReference.child(mPreferenceManager.getUser().getUid()).child(mKey).removeValue();
    }

    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
