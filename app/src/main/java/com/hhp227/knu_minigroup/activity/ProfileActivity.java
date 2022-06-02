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
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityProfileBinding;
import com.hhp227.knu_minigroup.helper.BitmapUtil;
import com.hhp227.knu_minigroup.viewmodel.ProfileViewModel;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding mBinding;

    private ActivityResultLauncher<Intent> mCameraPickActivityResultLauncher, mCameraCaptureActivityResultLauncher;

    private ProfileViewModel mViewModel;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityProfileBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        ActivityResultCallback<ActivityResult> activityResultCallback = new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getExtras().get("data") != null) {
                        mViewModel.setBitmap((Bitmap) result.getData().getExtras().get("data"));
                    } else if (result.getData().getData() != null) {
                        mViewModel.setBitmap(new BitmapUtil(getBaseContext()).bitmapResize(result.getData().getData(), 200));
                    }
                }
            }
        };
        mCameraPickActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResultCallback);
        mCameraCaptureActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResultCallback);
        mProgressDialog = new ProgressDialog(this);

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("요청중 ...");
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
                mViewModel.sync();
            }
        });
        mViewModel.mBitmap.observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                Glide.with(getApplicationContext())
                        .load(bitmap)
                        .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle).circleCrop())
                        .into(mBinding.ivProfileImage);
                invalidateOptionsMenu();
            }
        });
        mViewModel.mState.observe(this, new Observer<ProfileViewModel.State>() {
            @Override
            public void onChanged(ProfileViewModel.State state) {
                if (state.isLoading) {
                    showProgressDialog();
                } else if (state.user != null) {
                    mBinding.tvName.setText(state.user.getName());
                    mBinding.tvKnuId.setText(state.user.getUserId());
                    mBinding.tvDept.setText(state.user.getDepartment());
                    mBinding.tvStuNum.setText(state.user.getNumber());
                    mBinding.tvGrade.setText(state.user.getGrade());
                    mBinding.tvEmail.setText(state.user.getEmail());
                    mBinding.tvIp.setText(state.user.getUserIp());
                    mBinding.tvPhoneNum.setText(state.user.getPhoneNumber());
                    Glide.with(getApplicationContext())
                            .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", state.user.getUid()), new LazyHeaders.Builder().addHeader("Cookie", mViewModel.getCookie()).build()))
                            .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle)
                                    .circleCrop()
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE))
                            .into(mBinding.ivProfileImage);
                    if (state.isSuccess) {
                        setResult(RESULT_OK);
                        Toast.makeText(getApplicationContext(), state.message, Toast.LENGTH_LONG).show();
                    }
                } else if (state.message != null && !state.message.isEmpty()) {
                    hideProgressDialog();
                    Toast.makeText(getApplicationContext(), state.message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
        mCameraPickActivityResultLauncher = null;
        mCameraCaptureActivityResultLauncher = null;
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
                mCameraPickActivityResultLauncher.launch(intent);
                return true;
            case R.id.camera:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                mCameraCaptureActivityResultLauncher.launch(intent);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modify, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_send);

        menuItem.setVisible(mViewModel.mBitmap.getValue() != null);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                mViewModel.uploadImage(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
