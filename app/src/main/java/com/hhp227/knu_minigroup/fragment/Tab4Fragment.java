package com.hhp227.knu_minigroup.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.*;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Tab4Fragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "설정";
    private static boolean mIsAdmin;
    private static int mPosition;
    private static String mGroupId, mGroupImage, mKey;
    private long mLastClickTime;
    private ProgressDialog mProgressDialog;
    private User mUser;

    public Tab4Fragment() {
    }

    public static Tab4Fragment newInstance(boolean isAdmin, String grpId, String grpImg, int position, String key) {
        Bundle args = new Bundle();
        args.putBoolean("admin", isAdmin);
        args.putString("grp_id", grpId);
        args.putString("grp_img", grpImg);
        args.putInt("pos", position);
        args.putString("key", key);
        Tab4Fragment fragment = new Tab4Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsAdmin = getArguments().getBoolean("admin");
            mGroupId = getArguments().getString("grp_id");
            mGroupImage = getArguments().getString("grp_img");
            mPosition = getArguments().getInt("pos");
            mKey = getArguments().getString("key");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab4, container, false);
        TextView name = rootView.findViewById(R.id.tv_name);
        TextView knuId = rootView.findViewById(R.id.tv_knu_id);
        TextView withdrawalText = rootView.findViewById(R.id.tv_withdrawal);
        AdView adView = rootView.findViewById(R.id.adView);
        ImageView profileImage = rootView.findViewById(R.id.iv_profile_image);
        LinearLayout profile = rootView.findViewById(R.id.ll_profile);
        LinearLayout withdrawal = rootView.findViewById(R.id.ll_withdrawal);
        LinearLayout settings = rootView.findViewById(R.id.ll_settings);
        LinearLayout notice = rootView.findViewById(R.id.ll_notice);
        LinearLayout feedback = rootView.findViewById(R.id.ll_feedback);
        LinearLayout appStore = rootView.findViewById(R.id.ll_appstore);
        LinearLayout share = rootView.findViewById(R.id.ll_share);
        LinearLayout version = rootView.findViewById(R.id.ll_verinfo);
        AdRequest adRequest = new AdRequest.Builder().build();
        mProgressDialog = new ProgressDialog(getContext());
        mUser = app.AppController.getInstance().getPreferenceManager().getUser();
        String stuKnuId = mUser.getUserId();
        String userName = mUser.getName();

        mProgressDialog.setCancelable(false);
        Glide.with(getContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mUser.getUid()), new LazyHeaders.Builder().addHeader("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie()).build()))
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage);
        adView.loadAd(adRequest);
        name.setText(userName);
        knuId.setText(stuKnuId);
        profile.setOnClickListener(this);
        withdrawal.setOnClickListener(this);
        if (mIsAdmin) {
            withdrawalText.setText("소모임 폐쇄");
            settings.setOnClickListener(this);
            settings.setVisibility(View.VISIBLE);
        } else {
            withdrawalText.setText("소모임 탈퇴");
            settings.setVisibility(View.GONE);
        }
        notice.setOnClickListener(this);
        feedback.setOnClickListener(this);
        appStore.setOnClickListener(this);
        share.setOnClickListener(this);
        version.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
            return;
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()) {
            case R.id.ll_profile :
                startActivity(new Intent(getContext(), ProfileActivity.class));
                break;
            case R.id.ll_withdrawal :
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage((mIsAdmin ? "폐쇄" : "탈퇴") + "하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.setMessage("요청중...");
                        showProgressDialog();
                        app.AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, mIsAdmin ? EndPoint.DELETE_GROUP : EndPoint.WITHDRAWAL_GROUP, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (!response.getBoolean("isError")) {
                                        Toast.makeText(getContext(), "소모임 " + (mIsAdmin ? "폐쇄" : "탈퇴") + " 완료", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } finally {
                                    hideProgressDialog();
                                    deleteGroupFromFirebase();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                VolleyLog.e(TAG, error.getMessage());
                                hideProgressDialog();
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie());
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
                        });
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
            case R.id.ll_settings :
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("grp_img", mGroupImage);
                intent.putExtra("key", mKey);
                startActivityForResult(intent, GroupFragment.UPDATE_GROUP);
                break;
            case R.id.ll_notice :
                startActivity(new Intent(getContext(), NoticeActivity.class));
                break;
            case R.id.ll_feedback:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/Text");
                email.putExtra(Intent.EXTRA_EMAIL, new String[] {"hong227@naver.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "경북대소모임 건의사항");
                email.putExtra(Intent.EXTRA_TEXT, "작성자 (Writer) : " + mUser.getName() + "\n기기 모델 (Model) : " + Build.MODEL + "\n앱 버전 (AppVer) : " + Build.VERSION.RELEASE + "\n내용 (Content) : " + "");
                email.setType("message/rfc822");
                startActivity(email);
                break;
            case R.id.ll_appstore :
                String appUrl = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)));
                break;
            case R.id.ll_share :
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share.putExtra(Intent.EXTRA_TEXT, "확인하세요" + "\n" +
                        "GitHub Page :  https://localhost/" +
                        "Sample App : https://play.google.com/store/apps/details?id=" + getContext().getPackageName());
                startActivity(Intent.createChooser(share, getString(R.string.app_name)));
                break;
            case R.id.ll_verinfo :
                startActivity(new Intent(getActivity(), VerInfoActivity.class));
                break;
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GroupFragment.UPDATE_GROUP && resultCode == Activity.RESULT_OK) {
            String groupName = data.getStringExtra("grp_nm");
            String groupDescription = data.getStringExtra("grp_desc");
            String joinType = data.getStringExtra("join_div");
            getActivity().getActionBar().setTitle(groupName);
            Intent intent = new Intent(getContext(), GroupFragment.class);
            intent.putExtra("grp_nm", groupName);
            intent.putExtra("grp_desc", groupDescription);
            intent.putExtra("join_div", joinType);
            intent.putExtra("pos", mPosition);
            getActivity().setResult(Activity.RESULT_OK, intent);
        }
    }

    private void deleteGroupFromFirebase() {
        final DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");
        if (mIsAdmin) {
            groupsReference.child(mKey).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        userGroupListReference.child(snapshot.getKey()).child(mKey).removeValue();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                }
            });
            articlesReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        replysReference.child(snapshot.getKey()).removeValue();
                    articlesReference.child(mKey).removeValue();
                    groupsReference.child(mKey).removeValue();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, databaseError.getMessage());
                }
            });
        } else {
            groupsReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null)
                        return;
                    GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);
                    if (groupItem.getMembers() != null && groupItem.getMembers().containsKey(mUser.getUid())) {
                        Map<String, Boolean> members = groupItem.getMembers();
                        members.remove(mUser.getUid());
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
            userGroupListReference.child(app.AppController.getInstance().getPreferenceManager().getUser().getUid()).child(mKey).removeValue();
        }
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
