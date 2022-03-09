package com.hhp227.knu_minigroup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
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
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityProfileBinding;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.helper.BitmapUtil;
import com.hhp227.knu_minigroup.volley.util.MultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "프로필";

    private boolean mIsVisible;

    private Bitmap mBitmap;

    private CookieManager mCookieManager;

    private ProgressDialog mProgressDialog;

    private User mUser;

    private ActivityProfileBinding mBinding;

    private final ActivityResultLauncher<Intent> mCameraPickImageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                mIsVisible = true;
                mBitmap = new BitmapUtil(getBaseContext()).bitmapResize(result.getData().getData(), 200);

                Glide.with(getApplicationContext())
                        .load(mBitmap)
                        .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle).circleCrop())
                        .into(mBinding.ivProfileImage);
                invalidateOptionsMenu();
            }
        }
    });

    private final ActivityResultLauncher<Intent> mCameraCaptureImageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                mIsVisible = true;
                mBitmap = (Bitmap) result.getData().getExtras().get("data");

                Glide.with(getApplicationContext())
                        .load(mBitmap)
                        .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle).circleCrop())
                        .into(mBinding.ivProfileImage);
                invalidateOptionsMenu();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityProfileBinding.inflate(getLayoutInflater());

        setContentView(mBinding.getRoot());
        mCookieManager = AppController.getInstance().getCookieManager();
        mUser = AppController.getInstance().getPreferenceManager().getUser();
        mProgressDialog = new ProgressDialog(this);

        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("요청중 ...");
        Glide.with(getApplicationContext())
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mUser.getUid()), new LazyHeaders.Builder().addHeader("Cookie", mCookieManager.getCookie(EndPoint.LOGIN)).build()))
                .apply(RequestOptions
                        .errorOf(R.drawable.user_image_view_circle)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(mBinding.ivProfileImage);
        mBinding.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        mBinding.bSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, EndPoint.SYNC_PROFILE, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hideProgressDialog();
                        try {
                            if (!response.getBoolean("isError")) {
                                Glide.with(getApplicationContext())
                                        .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", mUser.getUid()), new LazyHeaders.Builder().addHeader("Cookie", mCookieManager.getCookie(EndPoint.LOGIN)).build()))
                                        .apply(RequestOptions
                                                .errorOf(R.drawable.user_image_view_circle)
                                                .circleCrop()
                                                .skipMemoryCache(true)
                                                .diskCacheStrategy(DiskCacheStrategy.NONE))
                                        .into(mBinding.ivProfileImage);
                                setResult(RESULT_OK);
                                Toast.makeText(getApplicationContext(), response.getString("message"), Toast.LENGTH_LONG).show();
                            } else
                                Toast.makeText(getApplicationContext(), "동기화 실패", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();

                        headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                        return headers;
                    }
                };

                showProgressDialog();
                AppController.getInstance().addToRequestQueue(jsonObjectRequest);
            }
        });
        mBinding.tvName.setText(mUser.getName());
        mBinding.tvKnuId.setText(mUser.getUserId());
        mBinding.tvDept.setText(mUser.getDepartment());
        mBinding.tvStuNum.setText(mUser.getNumber());
        mBinding.tvGrade.setText(mUser.getGrade());
        mBinding.tvEmail.setText(mUser.getEmail());
        mBinding.tvIp.setText(mUser.getUserIp());
        mBinding.tvPhoneNum.setText(mUser.getPhoneNumber());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("프로필 이미지 변경");
        getMenuInflater().inflate(R.menu.myinfo, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.album:
                intent = new Intent(Intent.ACTION_PICK);

                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                mCameraPickImageActivityResultLauncher.launch(intent);
                return true;
            case R.id.camera:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                mCameraCaptureImageActivityResultLauncher.launch(intent);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.hhp227.knu_minigroup.R.menu.modify, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(com.hhp227.knu_minigroup.R.id.action_send);

        menuItem.setVisible(mIsVisible);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                uploadImage(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadImage(final boolean isUpdate) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, isUpdate ? EndPoint.PROFILE_IMAGE_UPDATE : EndPoint.PROFILE_IMAGE_PREVIEW, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                if (isUpdate) {
                    hideProgressDialog();
                    setResult(RESULT_OK);
                    Toast.makeText(getApplicationContext(), new String(response.data).contains("성공") ? "수정되었습니다." : "실패했습니다.", Toast.LENGTH_LONG).show();
                } else
                    uploadImage(true);
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

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("FLAG", "FILE");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                params.put("img_file", new DataPart(UUID.randomUUID().toString().replace("-", "").concat(".jpg"), getFileDataFromDrawable(mBitmap)));
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        };

        AppController.getInstance().addToRequestQueue(multipartRequest);
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
