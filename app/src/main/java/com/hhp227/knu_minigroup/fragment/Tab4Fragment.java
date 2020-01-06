package com.hhp227.knu_minigroup.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.hhp227.knu_minigroup.*;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class Tab4Fragment extends BaseFragment implements View.OnClickListener {
    private static String groupId;
    private static boolean isAdmin;
    private static final String TAG = Tab4Fragment.class.getSimpleName();
    private long mLastClickTime;
    AdView adView;
    ImageView profileImage;
    LinearLayout profile, withdrawal, settings, appStore, share, version;
    ProgressDialog progressDialog;
    TextView name, knuId, withdrawalText;

    public Tab4Fragment() {
    }

    public static Tab4Fragment newInstance(boolean isAdmin, String grpId) {
        Bundle args = new Bundle();
        args.putBoolean("admin", isAdmin);
        args.putString("grp_id", grpId);
        Tab4Fragment fragment = new Tab4Fragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAdmin = getArguments().getBoolean("admin");
            groupId = getArguments().getString("grp_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab4, container, false);
        adView = rootView.findViewById(R.id.adView);
        profileImage = rootView.findViewById(R.id.iv_profile_image);
        name = rootView.findViewById(R.id.tv_name);
        knuId = rootView.findViewById(R.id.tv_knu_id);
        profile = rootView.findViewById(R.id.ll_profile);
        withdrawal = rootView.findViewById(R.id.ll_withdrawal);
        withdrawalText = rootView.findViewById(R.id.tv_withdrawal);
        settings = rootView.findViewById(R.id.ll_settings);
        appStore = rootView.findViewById(R.id.ll_appstore);
        share = rootView.findViewById(R.id.ll_share);
        version = rootView.findViewById(R.id.ll_verinfo);
        progressDialog = new ProgressDialog(getContext());

        AdRequest adRequest = new AdRequest.Builder().build();
        User user = app.AppController.getInstance().getPreferenceManager().getUser();
        String stuKnuId = user.getUserId();
        String userName = user.getName();
        progressDialog.setCancelable(false);
        Glide.with(getContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", user.getUid()), new LazyHeaders.Builder().addHeader("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie()).build()))
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage);
        adView.loadAd(adRequest);
        name.setText(userName);
        knuId.setText(stuKnuId);
        profile.setOnClickListener(this);
        withdrawal.setOnClickListener(this);
        if (isAdmin) {
            withdrawalText.setText("소모임 폐쇄");
            settings.setOnClickListener(this);
            settings.setVisibility(View.VISIBLE);
        } else {
            withdrawalText.setText("소모임 탈퇴");
            settings.setVisibility(View.GONE);
        }
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
                builder.setMessage((isAdmin ? "폐쇄" : "탈퇴") + "하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.setMessage("요청중...");
                        showProgressDialog();
                        app.AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, isAdmin ? EndPoint.DELETE_GROUP : EndPoint.WITHDRAWAL_GROUP, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (!response.getBoolean("isError")) {
                                        Toast.makeText(getContext(), "소모임 " + (isAdmin ? "폐쇄" : "탈퇴") + " 완료", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } finally {
                                    hideProgressDialog();
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
                                params.put("CLUB_GRP_ID", groupId);
                                if (params != null && params.size() > 0) {
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
                intent.putExtra("grp_id", groupId);
                startActivity(intent);
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

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
