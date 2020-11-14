package com.hhp227.knu_minigroup.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.*;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.dto.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Tab4Fragment extends Fragment implements View.OnClickListener {
    public static final int UPDATE_PROFILE = 0;

    private static final String TAG = "설정";

    private static boolean mIsAdmin;

    private static int mPosition;

    private static String mGroupId, mGroupImage, mKey;

    private long mLastClickTime;

    private CookieManager mCookieManager;

    private User mUser;

    private RecyclerView mRecyclerView;

    public Tab4Fragment() {
    }

    public static Tab4Fragment newInstance(boolean isAdmin, String grpId, String grpImg, int position, String key) {
        Tab4Fragment fragment = new Tab4Fragment();
        Bundle args = new Bundle();

        args.putBoolean("admin", isAdmin);
        args.putString("grp_id", grpId);
        args.putString("grp_img", grpImg);
        args.putInt("pos", position);
        args.putString("key", key);
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
        return inflater.inflate(R.layout.fragment_tab4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mCookieManager = AppController.getInstance().getCookieManager();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RecyclerView.Adapter<Tab4Holder>() {
            @NonNull
            @Override
            public Tab4Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.content_tab4, parent, false);

                return new Tab4Holder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull Tab4Holder holder, int position) {
                mUser = AppController.getInstance().getPreferenceManager().getUser();

                holder.bind(mUser);
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GroupFragment.UPDATE_GROUP && resultCode == Activity.RESULT_OK) {
            String groupName = data.getStringExtra("grp_nm");
            String groupDescription = data.getStringExtra("grp_desc");
            String joinType = data.getStringExtra("join_div");
            Intent intent = new Intent(getContext(), GroupFragment.class);

            intent.putExtra("grp_nm", groupName);
            intent.putExtra("grp_desc", groupDescription);
            intent.putExtra("join_div", joinType);
            intent.putExtra("pos", mPosition);
            getActivity().setResult(Activity.RESULT_OK, intent);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(groupName);
        } else if (requestCode == UPDATE_PROFILE && resultCode == Activity.RESULT_OK) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
            getActivity().setResult(Activity.RESULT_OK);
        }
    }

    private void deleteGroupFromFirebase() {
        final DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        final DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        final DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");
        if (mIsAdmin) {
            groupsReference.child(mKey).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        userGroupListReference.child(snapshot.getKey()).child(mKey).removeValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                }
            });
            articlesReference.child(mKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                }
            });
            userGroupListReference.child(AppController.getInstance().getPreferenceManager().getUser().getUid()).child(mKey).removeValue();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_profile:
                startActivityForResult(new Intent(getContext(), ProfileActivity.class), UPDATE_PROFILE);
                break;
            case R.id.ll_withdrawal:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setMessage((mIsAdmin ? "폐쇄" : "탈퇴") + "하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, mIsAdmin ? EndPoint.DELETE_GROUP : EndPoint.WITHDRAWAL_GROUP, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (!response.getBoolean("isError")) {
                                        Intent intent = new Intent(getContext(), MainActivity.class);

                                        intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        Toast.makeText(getContext(), "소모임 " + (mIsAdmin ? "폐쇄" : "탈퇴") + " 완료", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } finally {
                                    //hideProgressDialog();
                                    deleteGroupFromFirebase();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                VolleyLog.e(TAG, error.getMessage());
                                //hideProgressDialog();
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
            case R.id.ll_settings:
                Intent intent = new Intent(getContext(), SettingsActivity.class);

                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("grp_img", mGroupImage);
                intent.putExtra("key", mKey);
                startActivityForResult(intent, GroupFragment.UPDATE_GROUP);
                break;
            case R.id.ll_notice:
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
            case R.id.ll_appstore:
                String appUrl = "https://play.google.com/store/apps/details?id=" + getContext().getPackageName();

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appUrl)));
                break;
            case R.id.ll_share:
                Intent share = new Intent(Intent.ACTION_SEND);

                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                share.putExtra(Intent.EXTRA_TEXT, "확인하세요" + "\n" +
                        "GitHub Page :  https://localhost/" +
                        "Sample App : https://play.google.com/store/apps/details?id=" + getContext().getPackageName());
                startActivity(Intent.createChooser(share, getString(R.string.app_name)));
                break;
            case R.id.ll_verinfo:
                startActivity(new Intent(getActivity(), VerInfoActivity.class));
                break;
        }
    }

    public class Tab4Holder extends RecyclerView.ViewHolder {
        private final AdView adView;

        private final LinearLayout profile, withdrawal, settings, notice, feedback, appStore, share, version;

        private final ImageView profileImage;

        private final TextView name, yuId, withdrawalText;

        public Tab4Holder(View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.ad_view);
            appStore = itemView.findViewById(R.id.ll_appstore);
            feedback = itemView.findViewById(R.id.ll_feedback);
            name = itemView.findViewById(R.id.tv_name);
            notice = itemView.findViewById(R.id.ll_notice);
            profile = itemView.findViewById(R.id.ll_profile);
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            settings = itemView.findViewById(R.id.ll_settings);
            share = itemView.findViewById(R.id.ll_share);
            version = itemView.findViewById(R.id.ll_verinfo);
            withdrawal = itemView.findViewById(R.id.ll_withdrawal);
            withdrawalText = itemView.findViewById(R.id.tv_withdrawal);
            yuId = itemView.findViewById(R.id.tv_yu_id);

            profile.setOnClickListener(Tab4Fragment.this);
            withdrawal.setOnClickListener(Tab4Fragment.this);
            notice.setOnClickListener(Tab4Fragment.this);
            feedback.setOnClickListener(Tab4Fragment.this);
            appStore.setOnClickListener(Tab4Fragment.this);
            share.setOnClickListener(Tab4Fragment.this);
            version.setOnClickListener(Tab4Fragment.this);
            settings.setOnClickListener(Tab4Fragment.this);
        }

        private void bind(User user) {
            Glide.with(itemView.getContext())
                    .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", user.getUid()), new LazyHeaders.Builder()
                            .addHeader("Cookie", mCookieManager.getCookie(EndPoint.LOGIN))
                            .build()))
                    .apply(RequestOptions
                            .circleCropTransform()
                            .error(R.drawable.user_image_view_circle)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(profileImage);
            name.setText(user.getName());
            yuId.setText(user.getUserId());
            if (mIsAdmin) {
                withdrawalText.setText("소모임 폐쇄");
                settings.setVisibility(View.VISIBLE);
            } else {
                withdrawalText.setText("소모임 탈퇴");
                settings.setVisibility(View.GONE);
            }
            adView.loadAd(new AdRequest.Builder().build());
        }
    }
}