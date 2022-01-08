package com.hhp227.knu_minigroup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.core.content.FileProvider;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.adapter.WriteListAdapter;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityWriteBinding;
import com.hhp227.knu_minigroup.databinding.WriteTextBinding;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.helper.BitmapUtil;
import com.hhp227.knu_minigroup.volley.util.MultipartRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.hhp227.knu_minigroup.WriteActivity.*;

public class ModifyActivity extends AppCompatActivity {
    private static final String TAG = ModifyActivity.class.getSimpleName();

    private int mContextMenuRequest;

    private String mGrpId, mArtlNum, mCurrentPhotoPath, mCookie, mTitle, mContent, mGrpKey, mArtlKey;

    private List<String> mImageList;

    private List<Object> mContents;

    private ProgressDialog mProgressDialog;

    private StringBuilder mMakeHtmlContents;

    private Uri mPhotoUri;

    private WriteListAdapter mAdapter;

    private YouTubeItem mYouTubeItem;

    private ActivityWriteBinding mActivityWriteBinding;

    private WriteTextBinding mWriteTextBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityWriteBinding = ActivityWriteBinding.inflate(getLayoutInflater());
        mWriteTextBinding = WriteTextBinding.inflate(getLayoutInflater());

        setContentView(mActivityWriteBinding.getRoot());
        Intent intent = getIntent();
        mContents = new ArrayList<>();
        mCookie = AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN);
        mAdapter = new WriteListAdapter(getApplicationContext(), R.layout.write_content, mContents);
        mProgressDialog = new ProgressDialog(this);
        mGrpId = intent.getStringExtra("grp_id");
        mArtlNum = intent.getStringExtra("artl_num");
        mTitle = intent.getStringExtra("sbjt");
        mContent = intent.getStringExtra("txt");
        mImageList = intent.getStringArrayListExtra("img");
        mYouTubeItem = intent.getParcelableExtra("vid");
        mGrpKey = intent.getStringExtra("grp_key");
        mArtlKey = intent.getStringExtra("artl_key");

        setSupportActionBar(mActivityWriteBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mActivityWriteBinding.llImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextMenuRequest = 2;

                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        mActivityWriteBinding.llVideo.setOnClickListener(new View.OnClickListener() {
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
        mActivityWriteBinding.lvWrite.addHeaderView(mWriteTextBinding.getRoot());
        mActivityWriteBinding.lvWrite.setAdapter(mAdapter);
        mActivityWriteBinding.lvWrite.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mContextMenuRequest = 1;

                view.showContextMenu();
            }
        });
        mProgressDialog.setCancelable(false);
        if (mImageList.size() > 0) {
            mContents.addAll(mImageList);
            mAdapter.notifyDataSetChanged();
        }
        if (mYouTubeItem != null)
            mContents.add(mYouTubeItem.position, mYouTubeItem);
        registerForContextMenu(mActivityWriteBinding.lvWrite);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityWriteBinding = null;
        mWriteTextBinding = null;
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
                String title = mWriteTextBinding.etTitle.getEditableText().toString();
                String content = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ? Html.toHtml(mWriteTextBinding.etContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) : Html.toHtml(mWriteTextBinding.etContent.getText());

                if (!mWriteTextBinding.etTitle.getText().toString().isEmpty() && !(TextUtils.isEmpty(mWriteTextBinding.etContent.getText()) && mContents.size() == 0)) {
                    mMakeHtmlContents = new StringBuilder();

                    mImageList.clear();
                    mProgressDialog.setMessage("전송중...");
                    mProgressDialog.setProgressStyle(mContents.size() > 0 ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
                    showProgressDialog();
                    if (mContents.size() > 0) {
                        int position = 0;
                        if (mContents.get(position) instanceof String) {
                            String image = (String) mContents.get(position);

                            uploadProcess(position, image, false);
                        } else if (mContents.get(position) instanceof Bitmap) {
                            Bitmap bitmap = (Bitmap) mContents.get(position);

                            uploadImage(position, bitmap);//
                        } else if (mContents.get(position) instanceof YouTubeItem) {
                            YouTubeItem youTubeItem = (YouTubeItem) mContents.get(position);

                            uploadProcess(position, youTubeItem.videoId, true);
                        }
                    } else
                        actionSend(title, content);
                } else
                    Toast.makeText(getApplicationContext(), (TextUtils.isEmpty(mWriteTextBinding.etTitle.getText()) ? "제목" : "내용") + "을 입력하세요.", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch (mContextMenuRequest) {
            case 1 :
                menu.setHeaderTitle("작업 선택");
                menu.add(Menu.NONE, 1, Menu.NONE, "삭제");
                break;
            case 2 :
                menu.setHeaderTitle("이미지 선택");
                menu.add(Menu.NONE, 2, Menu.NONE, "갤러리");
                menu.add(Menu.NONE, 3, Menu.NONE, "카메라");
                break;
            case 3 :
                menu.setHeaderTitle("동영상 선택");
                menu.add(Menu.NONE, 4, Menu.NONE, "유튜브");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case 1:
                int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position - 1;

                if (mContents.get(position) instanceof YouTubeItem)
                    mYouTubeItem = null;

                mContents.remove(position);
                mAdapter.notifyDataSetChanged();
                return true;
            case 2:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*")
                        .setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                startActivityForResult(intent, CAMERA_PICK_IMAGE_REQUEST_CODE);
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
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
                return true;
            case 4:
                if (mYouTubeItem != null)
                    Toast.makeText(getApplicationContext(), "동영상은 하나만 첨부 할수 있습니다.", Toast.LENGTH_LONG).show();
                else {
                    Intent ysIntent = new Intent(getApplicationContext(), YouTubeSearchActivity.class);

                    ysIntent.putExtra("type", 1);
                    startActivityForResult(ysIntent, REQUEST_YOUTUBE_PICK);
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;

        if (requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri fileUri = data.getClipData().getItemAt(i).getUri();
                    bitmap = new BitmapUtil(this).bitmapResize(fileUri, 200);

                    mContents.add(bitmap);
                }
                mAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                bitmap = new BitmapUtil(this).bitmapResize(mPhotoUri, 200);

                if (bitmap != null) {
                    ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    int angle = orientation == ExifInterface.ORIENTATION_ROTATE_90 ? 90
                            : orientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180
                            : orientation == ExifInterface.ORIENTATION_ROTATE_270 ? 270
                            : 0;
                    Bitmap rotatedBitmap = new BitmapUtil(this).rotateImage(bitmap, angle);

                    mContents.add(rotatedBitmap);
                    mAdapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_YOUTUBE_PICK && resultCode == RESULT_OK) {//
            mYouTubeItem = data.getParcelableExtra("youtube");

            mContents.add(mYouTubeItem);
            mAdapter.notifyDataSetChanged();
        }
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
        AppController.getInstance().addToRequestQueue(multipartRequest);
    }

    private void uploadProcess(int position, String imageUrl, boolean isYoutube) { // 수정
        if (!isYoutube)
            mImageList.add(imageUrl);
        mProgressDialog.setProgress((int) ((double) (position) / (mContents.size() - 1) * 100));
        try {
            String test = (isYoutube ? "<p><embed title=\"YouTube video player\" class=\"youtube-player\" autostart=\"true\" src=\"//www.youtube.com/embed/" + imageUrl + "?autoplay=1\"  width=\"488\" height=\"274\"></embed><p>" // 유튜브 태그
                    : ("<p><img src=\"" + imageUrl + "\" width=\"488\"><p>")) + (position < mContents.size() - 1 ? "<br>": "");

            mMakeHtmlContents.append(test);
            if (position < mContents.size() - 1) {
                position++;
                Thread.sleep(!isYoutube ? 700 : 0);

                // 분기
                if (mContents.get(position) instanceof Bitmap) {////////////// 리팩토링 요망
                    Bitmap bitmap = (Bitmap) mContents.get(position);// 수정

                    uploadImage(position, bitmap); // 수정
                } else if (mContents.get(position) instanceof String) {
                    String imageSrc = (String) mContents.get(position);

                    uploadProcess(position, imageSrc, false);
                } else if (mContents.get(position) instanceof YouTubeItem) {
                    YouTubeItem youTubeItem = (YouTubeItem) mContents.get(position);

                    uploadProcess(position, youTubeItem.videoId, true);
                }
            } else {
                String title = mWriteTextBinding.etTitle.getEditableText().toString();
                String content = (!TextUtils.isEmpty(mWriteTextBinding.etContent.getText()) ? Html.toHtml(mWriteTextBinding.etContent.getText()) + "<p><br data-mce-bogus=\"1\"></p>" : "") + mMakeHtmlContents.toString();

                actionSend(title, content);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "이미지 업로드 실패", Toast.LENGTH_LONG).show();
            hideProgressDialog();
        }
    }

    private void actionSend(final String title, final String content) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_ARTICLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                try {
                    Intent intent = new Intent(ModifyActivity.this, ArticleActivity.class);

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
                    articleItem.setImages(mImageList.isEmpty() ? null : mImageList);
                    articleItem.setYoutube(mYouTubeItem);
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
