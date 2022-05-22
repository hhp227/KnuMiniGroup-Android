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
import android.os.Message;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.WriteListAdapter;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityCreateArticleBinding;
import com.hhp227.knu_minigroup.databinding.WriteTextBinding;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.helper.BitmapUtil;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import com.hhp227.knu_minigroup.viewmodel.CreateArticleViewModel;
import com.hhp227.knu_minigroup.volley.util.MultipartRequest;
import net.htmlparser.jericho.Source;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

// TODO
public class CreateArticleActivity extends AppCompatActivity {
    private static final String TAG = CreateArticleActivity.class.getSimpleName();

    private int mContextMenuRequest;

    private String mGrpId, mGrpNm, mGrpImg, mCurrentPhotoPath, mCookie;

    private String mArtlNum, mTitle, mContent, mGrpKey, mArtlKey;

    private PreferenceManager mPreferenceManager;

    private ProgressDialog mProgressDialog;

    private StringBuilder mMakeHtmlContents;

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
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mCookie = AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN);
        mProgressDialog = new ProgressDialog(this);
        mGrpId = getIntent().getStringExtra("grp_id");
        mGrpNm = getIntent().getStringExtra("grp_nm");
        mGrpImg = getIntent().getStringExtra("grp_img");
        mArtlNum = getIntent().getStringExtra("artl_num");
        mTitle = getIntent().getStringExtra("sbjt");
        mContent = getIntent().getStringExtra("txt");
        mGrpKey = getIntent().getStringExtra("grp_key");
        mArtlKey = getIntent().getStringExtra("artl_key");
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
                mContextMenuRequest = 2;

                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        mActivityCreateArticleBinding.llVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextMenuRequest = 3;

                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        mWriteTextBinding.etTitle.setText(mTitle);
        mWriteTextBinding.etContent.setText(mContent);
        mActivityCreateArticleBinding.lvWrite.addHeaderView(mWriteTextBinding.getRoot());
        mActivityCreateArticleBinding.lvWrite.setAdapter(mAdapter);
        mActivityCreateArticleBinding.lvWrite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mContextMenuRequest = 1;

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
        getMenuInflater().inflate(com.hhp227.knu_minigroup.R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                String title = mWriteTextBinding.etTitle.getEditableText().toString();
                String content = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ? Html.toHtml(mWriteTextBinding.etContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) : Html.toHtml(mWriteTextBinding.etContent.getText());

                if (!title.isEmpty() && !(TextUtils.isEmpty(content) && mViewModel.mContents.size() == 0)) {
                    mMakeHtmlContents = new StringBuilder();
                    mViewModel.mImageList = new ArrayList<>();

                    mProgressDialog.setProgressStyle(mViewModel.mContents.size() > 0 ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
                    showProgressDialog();
                    if (mViewModel.mContents.size() > 0) {
                        int position = 0;

                        if (mViewModel.mContents.get(position) instanceof String) {
                            String image = (String) mViewModel.mContents.get(position);

                            uploadProcess(position, image, false);
                        } else if (mViewModel.mContents.get(position) instanceof Bitmap) {////////////// 리팩토링 요망
                            Bitmap bitmap = (Bitmap) mViewModel.mContents.get(position);// 수정

                            uploadImage(position, bitmap); // 수정
                        } else if (mViewModel.mContents.get(position) instanceof YouTubeItem) {
                            YouTubeItem youTubeItem = (YouTubeItem) mViewModel.mContents.get(position);

                            uploadProcess(position, youTubeItem.videoId, true);
                        }
                    } else
                        actionSend(title, content);
                } else
                    Toast.makeText(getApplicationContext(), (title.isEmpty() ? "제목" : "내용") + "을 입력하세요.", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch (mContextMenuRequest) {
            case 1:
                menu.setHeaderTitle("작업 선택");
                menu.add(Menu.NONE, 1, Menu.NONE, "삭제");
                break;
            case 2:
                menu.setHeaderTitle("이미지 선택");
                menu.add(Menu.NONE, 2, Menu.NONE, "갤러리");
                menu.add(Menu.NONE, 3, Menu.NONE, "카메라");
                break;
            case 3:
                menu.setHeaderTitle("동영상 선택");
                menu.add(Menu.NONE, 4, Menu.NONE, "유튜브");
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
                mViewModel.mContents.remove(position);
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

    private void uploadImage(final int position, final Bitmap bitmap) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, EndPoint.IMAGE_UPLOAD, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String imageSrc = new String(response.data);
                imageSrc = EndPoint.BASE_URL + imageSrc.substring(imageSrc.lastIndexOf("/ilosfiles2/"), imageSrc.lastIndexOf("\""));

                uploadProcess(position, imageSrc, false);
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
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                params.put("file", new DataPart(System.currentTimeMillis() + position + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        };
        Volley.newRequestQueue(this).add(multipartRequest);
    }

    private void uploadProcess(int position, String imageUrl, boolean isYoutube) {
        if (!isYoutube)
            mViewModel.mImageList.add(imageUrl);
        mProgressDialog.setProgress((int) ((double) (position) / (mViewModel.mContents.size() - 1) * 100));
        try {
            String test = (isYoutube ? "<p><embed title=\"YouTube video player\" class=\"youtube-player\" autostart=\"true\" src=\"//www.youtube.com/embed/" + imageUrl + "?autoplay=1\"  width=\"488\" height=\"274\"></embed><p>" // 유튜브 태그
                    : ("<p><img src=\"" + imageUrl + "\" width=\"488\"><p>")) + (position < mViewModel.mContents.size() - 1 ? "<br>": "");

            mMakeHtmlContents.append(test);
            if (position < mViewModel.mContents.size() - 1) {
                position++;
                Thread.sleep(isYoutube ? 0 : 700);

                // 분기
                if (mViewModel.mContents.get(position) instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) mViewModel.mContents.get(position);

                    uploadImage(position, bitmap);
                } else if (mViewModel.mContents.get(position) instanceof String) {
                    String imageSrc = (String) mViewModel.mContents.get(position);

                    uploadProcess(position, imageSrc, false);
                } else if (mViewModel.mContents.get(position) instanceof YouTubeItem) {
                    YouTubeItem youTubeItem = (YouTubeItem) mViewModel.mContents.get(position);

                    uploadProcess(position, youTubeItem.videoId, true);
                }
            } else {
                String title = mWriteTextBinding.etTitle.getEditableText().toString();
                String content = (!TextUtils.isEmpty(mWriteTextBinding.etContent.getText().toString()) ? Html.toHtml(mWriteTextBinding.etContent.getText()) + "<p><br data-mce-bogus=\"1\"></p>" : "") + mMakeHtmlContents.toString();

                actionSend(title, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "이미지 업로드 실패", Toast.LENGTH_LONG).show();
            hideProgressDialog();
        }
    }

    private void actionSend(final String title, final String content) {
        if (getIntent().getIntExtra("type", -1) == 0) {
            actionCreate(title, content);
        } else {
            actionUpdate(title, content);
        }
    }

    private void actionCreate(final String title, final String content) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.WRITE_ARTICLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("isError");

                    if (!error) {
                        setResult(RESULT_OK);
                        finish();
                        Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "에러 : " + e.getMessage());
                } finally {
                    getArticleId();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
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

                params.put("SBJT", title);
                params.put("CLUB_GRP_ID", mGrpId);
                params.put("TXT", content);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private void actionUpdate(final String title, final String content) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_ARTICLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                try {
                    Intent intent = new Intent(CreateArticleActivity.this, ArticleActivity.class);

                    Toast.makeText(getApplicationContext(), "수정완료", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    initFirebaseData();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
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

                params.put("CLUB_GRP_ID", mGrpId);
                params.put("ARTL_NUM", mArtlNum);
                params.put("SBJT", title);
                params.put("TXT", content);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private void getArticleId() {
        String params = "?CLUB_GRP_ID=" + mGrpId + "&displayL=1";

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                String artlNum = source.getFirstElementByClass("comment_wrap").getAttributeValue("num");

                insertArticleToFirebase(artlNum);
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

                headers.put("Cookie", mCookie);
                return headers;
            }
        });
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

    private void insertArticleToFirebase(String artlNum) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        Map<String, Object> map = new HashMap<>();

        map.put("id", artlNum);
        map.put("uid", mPreferenceManager.getUser().getUid());
        map.put("name", mPreferenceManager.getUser().getName());
        map.put("title", mWriteTextBinding.etTitle.getText().toString());
        map.put("timestamp", System.currentTimeMillis());
        map.put("content", TextUtils.isEmpty(mWriteTextBinding.etContent.getText().toString()) ? null : mWriteTextBinding.etContent.getText().toString());
        map.put("images", mViewModel.mImageList);
        map.put("youtube", mViewModel.getYoutubeState().getValue());
        databaseReference.child(mGrpKey).push().setValue(map);
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        updateArticleDataToFirebase(databaseReference.child(mGrpKey).child(mArtlKey));
    }

    private void updateArticleDataToFirebase(final Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);
                if (articleItem != null) {
                    articleItem.setTitle(mWriteTextBinding.etTitle.getText().toString());
                    articleItem.setContent(TextUtils.isEmpty(mWriteTextBinding.etContent.getText()) ? null : mWriteTextBinding.etContent.getText().toString());
                    articleItem.setImages(mViewModel.mImageList.isEmpty() ? null : mViewModel.mImageList);
                    articleItem.setYoutube(mViewModel.getYoutubeState().getValue());
                    query.getRef().setValue(articleItem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
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
