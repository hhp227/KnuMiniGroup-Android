package com.hhp227.knu_minigroup.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;

import android.widget.*;
import androidx.fragment.app.Fragment;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.hhp227.knu_minigroup.CreateActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE;
import static com.hhp227.knu_minigroup.CreateActivity.CAMERA_PICK_IMAGE_REQUEST_CODE;

public class DefaultSettingFragment extends Fragment {
    private static int groupId;
    private Bitmap bitmap;
    private EditText inputTitle, inputDescription;
    private ImageView groupImage, resetTitle;
    private RadioGroup joinType;

    private boolean joinTypeCheck;

    public DefaultSettingFragment() {
        // Required empty public constructor
    }

    public static DefaultSettingFragment newInstance(int grpId) {
        DefaultSettingFragment fragment = new DefaultSettingFragment();
        Bundle args = new Bundle();
        args.putInt("grp_id", grpId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            groupId = getArguments().getInt("grp_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_default_setting, container, false);
        groupImage = rootView.findViewById(R.id.iv_group_image);
        inputTitle = rootView.findViewById(R.id.et_title);
        inputDescription = rootView.findViewById(R.id.et_description);
        joinType = rootView.findViewById(R.id.rg_jointype);
        resetTitle = rootView.findViewById(R.id.iv_reset);

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(v);
                getActivity().openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        inputTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetTitle.setImageResource(s.length() > 0 ? R.drawable.ic_clear_black_24dp : R.drawable.ic_clear_gray_24dp);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        joinType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                joinTypeCheck = checkedId != R.id.rb_auto;
            }
        });
        resetTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputTitle.setText("");
            }
        });
        String params = "?CLUB_GRP_ID=" + groupId;
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.MODIFY_GROUP + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                String title = source.getElementById("wrtGroup").getAttributeValue("value");
                String desc = source.getElementById("wrtExplain").getContent().toString();
                for (Element rbElement : source.getFirstElementByClass("radiobox").getAllElementsByClass("chktype"))
                    if (rbElement.toString().contains("checked"))
                        joinTypeCheck = !rbElement.getAttributeValue("value").equals("0");

                inputTitle.setText(title);
                inputDescription.setText(desc);
                joinType.check(!joinTypeCheck ? R.id.rb_auto : R.id.rb_check);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie());
                return headers;
            }
        });
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.modify, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send :
                String tagJsonReq = "req_send";
                app.AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.UPDATE_GROUP, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!response.getBoolean("isError")) {
                                Toast.makeText(getContext(), response.toString(), Toast.LENGTH_LONG).show();
                                getActivity().finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(error.getMessage());
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
                    }

                    @Override
                    public byte[] getBody() {
                        Map<String, String> params = new HashMap<>();
                        params.put("CLUB_GRP_ID", String.valueOf(groupId));
                        params.put("GRP_NM", inputTitle.getText().toString());
                        params.put("TXT", inputDescription.getText().toString());
                        params.put("JOIN_DIV", !joinTypeCheck ? "0" : "1");
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

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie());
                        return headers;
                    }
                }, tagJsonReq);
                return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("이미지 선택");
        menu.add("카메라");
        menu.add("갤러리");
        menu.add("이미지 없음");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getTitle().toString()) {
            case "카메라" :
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                break;
            case "갤러리" :
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                galleryIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, CAMERA_PICK_IMAGE_REQUEST_CODE);
                break;
            case "이미지 없음" :
                groupImage.setImageResource(R.drawable.add_photo);
                bitmap = null;
                Toast.makeText(getContext(), "이미지 없음 선택", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }
}
