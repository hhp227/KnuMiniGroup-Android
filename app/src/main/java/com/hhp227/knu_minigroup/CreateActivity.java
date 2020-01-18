package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.helper.BitmapUtil;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;
import com.hhp227.knu_minigroup.volley.util.MultipartRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateActivity extends Activity {
    private static final String TAG = CreateActivity.class.getSimpleName();

    // 인텐트값
    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int CAMERA_PICK_IMAGE_REQUEST_CODE = 200;
    private boolean mJoinTypeCheck;
    private String mCookie, mPushId;
    private Bitmap mBitmap;
    private EditText mGroupTitle, mGroupDescription;
    private ImageView mGroupImage, mResetTitle;
    private PreferenceManager mPreferenceManager;
    private ProgressDialog mProgressDialog;
    private RadioGroup mJoinType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        ActionBar actionBar = getActionBar();
        mGroupTitle = findViewById(R.id.et_title);
        mGroupDescription = findViewById(R.id.et_description);
        mResetTitle = findViewById(R.id.iv_reset);
        mGroupImage = findViewById(R.id.iv_group_image);
        mJoinType = findViewById(R.id.rg_jointype);
        mPreferenceManager = app.AppController.getInstance().getPreferenceManager();
        mCookie = mPreferenceManager.getCookie();
        mProgressDialog = new ProgressDialog(this);
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
                @Override
                public boolean isLayoutRtl() {
                    return false;
                }
            });
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("전송중...");
        mGroupTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mResetTitle.setImageResource(s.length() > 0 ? R.drawable.ic_clear_black_24dp : R.drawable.ic_clear_gray_24dp);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mResetTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGroupTitle.setText("");
            }
        });
        mGroupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        mJoinType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mJoinTypeCheck = checkedId != R.id.rb_auto;
            }
        });
        mJoinType.check(R.id.rb_auto);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;
            case R.id.action_send :
                final String title = mGroupTitle.getText().toString().trim();
                final String description = mGroupDescription.getText().toString().trim();
                final String join = !mJoinTypeCheck ? "0" : "1";
                if (!title.isEmpty() && !description.isEmpty()) {
                    showProgressDialog();
                    app.AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.CREATE_GROUP, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (!response.getBoolean("isError")) {
                                    String groupId = response.getString("CLUB_GRP_ID").trim();
                                    String groupName = response.getString("GRP_NM");
                                    if (mBitmap != null)
                                        groupImageUpdate(groupId, groupName, description, join);
                                    else {
                                        insertGroupToFirebase(groupId, groupName, description, join);
                                        createGroupSuccess(groupId, groupName);
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                                hideProgressDialog();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.e(error.getMessage());
                            hideProgressDialog();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Cookie", mCookie);
                            return headers;
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
                        }

                        @Override
                        public byte[] getBody() {
                            Map<String, String> params = new HashMap<>();
                            params.put("GRP_NM", title);
                            params.put("TXT", description);
                            params.put("JOIN_DIV", join);
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
                } else {
                    Toast.makeText(getApplicationContext(), "그룹명 또는 그룹설명을 입력해주세요.", Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                mGroupImage.setImageResource(R.drawable.add_photo);
                mBitmap = null;
                Toast.makeText(getBaseContext(), "이미지 없음 선택", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            mBitmap = (Bitmap) data.getExtras().get("data");
            mGroupImage.setImageBitmap(mBitmap);
        } else if (requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            mBitmap = new BitmapUtil(this).bitmapResize(fileUri, 200);
            mGroupImage.setImageBitmap(mBitmap);
        }
    }

    private void createGroupSuccess(String groupId, String groupName) {
        Intent intent = new Intent(CreateActivity.this, GroupActivity.class);
        intent.putExtra(getString(R.string.extra_admin), true);
        intent.putExtra(getString(R.string.extra_group_id), groupId);
        intent.putExtra(getString(R.string.extra_group_name), groupName);
        intent.putExtra(getString(R.string.extra_key), mPushId);
        setResult(RESULT_OK, intent);
        startActivity(intent);
        finish();
        hideProgressDialog();
        Toast.makeText(getApplicationContext(), "그룹이 생성되었습니다.", Toast.LENGTH_LONG).show();
    }

    private void groupImageUpdate(final String clubGrpId, final String grpNm, final String txt, final String joinDiv) {
        app.AppController.getInstance().addToRequestQueue(new MultipartRequest(Request.Method.POST, EndPoint.GROUP_IMAGE_UPDATE, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                insertGroupToFirebase(clubGrpId, grpNm, txt, joinDiv);
                createGroupSuccess(clubGrpId, grpNm);
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
                headers.put("Cookie", mCookie);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("CLUB_GRP_ID", clubGrpId);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart(UUID.randomUUID().toString().replace("-", "").concat(".jpg"), getFileDataFromDrawable(mBitmap)));
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        });
    }

    private void insertGroupToFirebase(String groupId, String groupName, String description, String joinType) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Map<String, Boolean> members = new HashMap<>();
        members.put(mPreferenceManager.getUser().getUid(), true);
        GroupItem groupItem = new GroupItem();
        groupItem.setId(groupId);
        groupItem.setTimestamp(System.currentTimeMillis());
        groupItem.setAuthor(mPreferenceManager.getUser().getName());
        groupItem.setAuthorUid(mPreferenceManager.getUser().getUid());
        groupItem.setImage(EndPoint.BASE_URL + (mBitmap != null ? "/ilosfiles2/club/photo/" + groupId.concat(".jpg") : "/ilos/images/community/share_nophoto.gif"));
        groupItem.setName(groupName);
        groupItem.setDescription(description);
        groupItem.setJoinType(joinType);
        groupItem.setMembers(members);
        groupItem.setMemberCount(members.size());

        mPushId = databaseReference.push().getKey();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("Groups/" + mPushId, groupItem);
        childUpdates.put("UserGroupList/" + mPreferenceManager.getUser().getUid() + "/" + mPushId, true);
        databaseReference.updateChildren(childUpdates);
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
