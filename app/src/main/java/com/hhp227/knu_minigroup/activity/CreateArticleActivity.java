package com.hhp227.knu_minigroup.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Spannable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.WriteListAdapter;
import com.hhp227.knu_minigroup.databinding.ActivityCreateArticleBinding;
import com.hhp227.knu_minigroup.databinding.WriteTextBinding;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.helper.BitmapUtil;
import com.hhp227.knu_minigroup.viewmodel.CreateArticleViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateArticleActivity extends AppCompatActivity {
    private String mCurrentPhotoPath;

    private ProgressDialog mProgressDialog;

    private Uri mPhotoUri;

    private WriteListAdapter mAdapter;

    private ActivityCreateArticleBinding mActivityCreateArticleBinding;

    private WriteTextBinding mWriteTextBinding;

    private ActivityResultLauncher<Intent> mCameraPickActivityResultLauncher, mCameraCaptureActivityResultLauncher, mYouTubeSearchActivityResultLauncher;

    private CreateArticleViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityCreateArticleBinding = ActivityCreateArticleBinding.inflate(getLayoutInflater());
        mWriteTextBinding = WriteTextBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(CreateArticleViewModel.class);
        mAdapter = new WriteListAdapter(getApplicationContext(), R.layout.write_content, mViewModel.mContents);
        mProgressDialog = new ProgressDialog(this);
        mCameraPickActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ClipData clipData = result.getData().getClipData();

                            if (clipData != null) {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    Uri fileUri = clipData.getItemAt(i).getUri();
                                    Bitmap bitmap = new BitmapUtil(getBaseContext()).bitmapResize(fileUri, 200);

                                    mViewModel.setBitmap(bitmap);
                                }
                            }
                        }
                    });
                }
            }
        });
        mCameraCaptureActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Bitmap bitmap = new BitmapUtil(getBaseContext()).bitmapResize(mPhotoUri, 200);

                                if (bitmap != null) {
                                    ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                                    int angle = orientation == ExifInterface.ORIENTATION_ROTATE_90 ? 90
                                            : orientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180
                                            : orientation == ExifInterface.ORIENTATION_ROTATE_270 ? 270
                                            : 0;
                                    Bitmap rotatedBitmap = new BitmapUtil(getBaseContext()).rotateImage(bitmap, angle);

                                    mViewModel.setBitmap(rotatedBitmap);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        mYouTubeSearchActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    YouTubeItem youTubeItem = result.getData().getParcelableExtra("youtube");

                    mViewModel.setYoutube(youTubeItem);
                }
            }
        });

        setContentView(mActivityCreateArticleBinding.getRoot());
        setSupportActionBar(mActivityCreateArticleBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mActivityCreateArticleBinding.llImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        mActivityCreateArticleBinding.llVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        mWriteTextBinding.etTitle.setText(getIntent().getStringExtra("sbjt"));
        mWriteTextBinding.etContent.setText(getIntent().getStringExtra("txt"));
        mActivityCreateArticleBinding.lvWrite.addHeaderView(mWriteTextBinding.getRoot());
        mActivityCreateArticleBinding.lvWrite.setAdapter(mAdapter);
        mActivityCreateArticleBinding.lvWrite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });
        mProgressDialog.setMessage("전송중...");
        mProgressDialog.setCancelable(false);
        registerForContextMenu(mActivityCreateArticleBinding.lvWrite);
        mViewModel.getBitmapState().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                mViewModel.addItem(bitmap);
                mAdapter.notifyDataSetChanged();
            }
        });
        mViewModel.getYoutubeState().observe(this, new Observer<YouTubeItem>() {
            @Override
            public void onChanged(YouTubeItem youTubeItem) {
                if (youTubeItem != null) {
                    if (youTubeItem.position > -1) {
                        mViewModel.addItem(youTubeItem.position, youTubeItem);
                    } else {
                        mViewModel.addItem(youTubeItem);
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        mViewModel.getState().observe(this, new Observer<CreateArticleViewModel.State>() {
            @Override
            public void onChanged(CreateArticleViewModel.State state) {
                if (state.progress >= 0) {
                    mProgressDialog.setProgressStyle(mViewModel.mContents.size() > 0 ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setProgress(state.progress);
                    showProgressDialog();
                } else if (state.articleId != null && !state.articleId.isEmpty()) {
                    setResult(RESULT_OK);
                    finish();
                    Toast.makeText(getApplicationContext(), state.message, Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                } else if (state.message != null && !state.message.isEmpty()) {
                    Toast.makeText(getApplicationContext(), state.message, Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }
        });
        mViewModel.getArticleFormState().observe(this, new Observer<CreateArticleViewModel.ArticleFormState>() {
            @Override
            public void onChanged(CreateArticleViewModel.ArticleFormState articleFormState) {
                Toast.makeText(getApplicationContext(), articleFormState.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWriteTextBinding = null;
        mActivityCreateArticleBinding = null;
        mCameraPickActivityResultLauncher = null;
        mCameraCaptureActivityResultLauncher = null;
        mYouTubeSearchActivityResultLauncher = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                Spannable title = mWriteTextBinding.etTitle.getEditableText();
                Spannable content = mWriteTextBinding.etContent.getText();

                mViewModel.actionSend(title, content);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch (v.getId()) {
            case R.id.ll_image:
                menu.setHeaderTitle("이미지 선택");
                menu.add(Menu.NONE, 2, Menu.NONE, "갤러리");
                menu.add(Menu.NONE, 3, Menu.NONE, "카메라");
                break;
            case R.id.ll_video:
                menu.setHeaderTitle("동영상 선택");
                menu.add(Menu.NONE, 4, Menu.NONE, "유튜브");
                break;
            default:
                menu.setHeaderTitle("작업 선택");
                menu.add(Menu.NONE, 1, Menu.NONE, "삭제");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case 1:
                int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position - 1;

                if (mViewModel.mContents.get(position) instanceof YouTubeItem) {
                    mViewModel.setYoutube(null);
                }
                mViewModel.removeItem(position);
                mAdapter.notifyDataSetChanged();
                return true;
            case 2:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*")
                        .setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                mCameraPickActivityResultLauncher.launch(intent);
                return true;
            case 3:
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;

                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        mPhotoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                        mCameraCaptureActivityResultLauncher.launch(intent);
                    }
                }
                return true;
            case 4:
                if (mViewModel.getYoutubeState().getValue() != null)
                    Toast.makeText(getApplicationContext(), "동영상은 하나만 첨부 할수 있습니다.", Toast.LENGTH_LONG).show();
                else {
                    Intent ysIntent = new Intent(getApplicationContext(), YouTubeSearchActivity.class);

                    mYouTubeSearchActivityResultLauncher.launch(ysIntent);
                }
                return true;
        }
        return false;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
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
