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
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.activity.GroupActivity;
import com.hhp227.knu_minigroup.activity.MainActivity;
import com.hhp227.knu_minigroup.activity.NoticeActivity;
import com.hhp227.knu_minigroup.activity.ProfileActivity;
import com.hhp227.knu_minigroup.activity.SettingsActivity;
import com.hhp227.knu_minigroup.activity.VerInfoActivity;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ContentTab4Binding;
import com.hhp227.knu_minigroup.databinding.FragmentTab4Binding;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.dto.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

// TODO
public class Tab4Fragment extends Fragment implements View.OnClickListener {
    public static final int UPDATE_GROUP = 30;
    private static final String TAG = "설정";
    private static boolean mIsAdmin;
    private static int mPosition;
    private static String mGroupId, mGroupImage, mKey;

    private CookieManager mCookieManager;

    private User mUser;

    private FragmentTab4Binding mBinding;

    private ActivityResultLauncher<Intent> mProfileActivityResultLauncher;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab4Binding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCookieManager = AppController.getInstance().getCookieManager();
        mProfileActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                ((GroupActivity) requireActivity()).onProfileActivityResult(result);
            }
        });

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.recyclerView.setAdapter(new RecyclerView.Adapter<Tab4Holder>() {
            @NonNull
            @Override
            public Tab4Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new Tab4Holder(ContentTab4Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mProfileActivityResultLauncher = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_GROUP && resultCode == Activity.RESULT_OK) {
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            String groupName = data.getStringExtra("grp_nm");
            String groupDescription = data.getStringExtra("grp_desc");
            String joinType = data.getStringExtra("join_div");
            Intent intent = new Intent(getContext(), GroupMainFragment.class);

            intent.putExtra("grp_nm", groupName);
            intent.putExtra("grp_desc", groupDescription);
            intent.putExtra("join_div", joinType);
            intent.putExtra("pos", mPosition);
            requireActivity().setResult(Activity.RESULT_OK, intent);
            if (actionBar != null) {
                actionBar.setTitle(groupName);
            }
        }
    }

    public void onProfileActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mBinding.recyclerView.getAdapter().notifyDataSetChanged();
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
                mProfileActivityResultLauncher.launch(new Intent(getContext(), ProfileActivity.class));
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
                                        requireActivity().setResult(Activity.RESULT_OK, new Intent(getContext(), MainActivity.class));
                                        requireActivity().finish();
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
                startActivityForResult(intent, UPDATE_GROUP);
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
        private final ContentTab4Binding mBinding;

        public Tab4Holder(ContentTab4Binding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.llProfile.setOnClickListener(Tab4Fragment.this);
            mBinding.llWithdrawal.setOnClickListener(Tab4Fragment.this);
            mBinding.llNotice.setOnClickListener(Tab4Fragment.this);
            mBinding.llFeedback.setOnClickListener(Tab4Fragment.this);
            mBinding.llAppstore.setOnClickListener(Tab4Fragment.this);
            mBinding.llShare.setOnClickListener(Tab4Fragment.this);
            mBinding.llVerinfo.setOnClickListener(Tab4Fragment.this);
            mBinding.llSettings.setOnClickListener(Tab4Fragment.this);
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
                    .into(mBinding.ivProfileImage);
            mBinding.tvName.setText(user.getName());
            mBinding.tvKnuId.setText(user.getUserId());
            if (mIsAdmin) {
                mBinding.tvWithdrawal.setText("소모임 폐쇄");
                mBinding.llSettings.setVisibility(View.VISIBLE);
            } else {
                mBinding.tvWithdrawal.setText("소모임 탈퇴");
                mBinding.llSettings.setVisibility(View.GONE);
            }
            mBinding.adView.loadAd(new AdRequest.Builder().build());
        }
    }
}