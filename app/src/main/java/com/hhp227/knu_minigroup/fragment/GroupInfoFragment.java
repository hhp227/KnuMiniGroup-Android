package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.MainActivity;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.RequestActivity;
import com.hhp227.knu_minigroup.app.EndPoint;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class GroupInfoFragment extends DialogFragment {
    private static final String TAG = "정보창";
    private static int joinType, groupId;
    private static String groupName, groupImage, groupInfo, groupDesc;
    private Button button, close;
    private ImageView image;
    private ProgressDialog progressDialog;
    private TextView name, info, desc;

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
            joinType = getArguments().getInt("type");
            groupId = getArguments().getInt("grp_id");
            groupName = getArguments().getString("grp_nm");
            groupImage = getArguments().getString("img");
            groupInfo = getArguments().getString("info");
            groupDesc = getArguments().getString("desc");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        View rootView = inflater.inflate(R.layout.fragment_group_info, container, false);
        button = rootView.findViewById(R.id.b_request);
        close = rootView.findViewById(R.id.b_close);
        image = rootView.findViewById(R.id.iv_group_image);
        name = rootView.findViewById(R.id.tv_name);
        info = rootView.findViewById(R.id.tv_info);
        desc = rootView.findViewById(R.id.tv_desciption);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("요청중...");
                showProgressDialog();
                String tag_json_req = "req_register";
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, joinType == 0 ? EndPoint.REGISTER_GROUP : EndPoint.WITHDRAWAL_GROUP, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (joinType == 0 && !response.getBoolean("isError")) {
                                Toast.makeText(getContext(), "신청완료", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                getActivity().setResult(RESULT_OK, intent);
                                getActivity().finish();
                            } else if (joinType == 1 && !response.getBoolean("isError")) {
                                Toast.makeText(getContext(), "신청취소", Toast.LENGTH_LONG).show();
                                ((RequestActivity) getActivity()).refresh();
                                GroupInfoFragment.this.dismiss();
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
                        params.put("CLUB_GRP_ID", String.valueOf(groupId));
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
        name.setText(groupName);
        info.setText(groupInfo);
        desc.setText(groupDesc);
        desc.setMaxLines(6);
        button.setText(joinType == 0 ? "가입신청" : "신청취소");
        Glide.with(this)
                .load(groupImage)
                .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(image);
        return rootView;
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
