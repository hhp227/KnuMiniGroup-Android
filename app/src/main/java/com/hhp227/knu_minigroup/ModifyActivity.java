package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;

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
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.WriteItem;
import com.hhp227.knu_minigroup.helper.BitmapUtil;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;
import com.hhp227.knu_minigroup.volley.util.MultipartRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.hhp227.knu_minigroup.WriteActivity.CAMERA_PICK_IMAGE_REQUEST_CODE;
import static com.hhp227.knu_minigroup.WriteActivity.REQUEST_IMAGE_CAPTURE;

public class ModifyActivity extends Activity {
    private static final String TAG = ModifyActivity.class.getSimpleName();
    private int mContextMenuRequest;
    private String mGrpId, mArtlNum, mCurrentPhotoPath, mCookie, mTitle, mContent, mGrpKey, mArtlKey;
    private EditText mInputTitle, mInputContent;
    private List<WriteItem> mContents;
    private ProgressDialog mProgressDialog;
    private StringBuilder mMakeHtmlImages;
    private Uri mPhotoUri;
    private List<String> mImageList;
    private WriteListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        ActionBar actionBar = getActionBar();
        LinearLayout buttonImage = findViewById(R.id.ll_image);
        ListView listView = findViewById(R.id.lv_write);
        View headerView = getLayoutInflater().inflate(R.layout.write_text, null, false);
        Intent intent = getIntent();
        mInputTitle = headerView.findViewById(R.id.et_title);
        mInputContent = headerView.findViewById(R.id.et_content);
        mContents = new ArrayList<>();
        mCookie = app.AppController.getInstance().getPreferenceManager().getCookie();
        mAdapter = new WriteListAdapter(getApplicationContext(), R.layout.write_content, mContents);
        mProgressDialog = new ProgressDialog(this);
        mGrpId = intent.getStringExtra("grp_id");
        mArtlNum = intent.getStringExtra("artl_num");
        mTitle = intent.getStringExtra("sbjt");
        mContent = intent.getStringExtra("txt");
        mImageList = intent.getStringArrayListExtra("img");
        mGrpKey = intent.getStringExtra("grp_key");
        mArtlKey = intent.getStringExtra("artl_key");

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
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContextMenuRequest = 2;
                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        mInputTitle.setText(mTitle);
        mInputContent.setText(mContent);
        listView.addHeaderView(headerView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mContextMenuRequest = 1;
                view.showContextMenu();
            }
        });
        mProgressDialog.setCancelable(false);
        if (mImageList.size() > 0) {
            for (String imageUrl : mImageList) {
                WriteItem writeItem = new WriteItem(null, null, imageUrl);
                mContents.add(writeItem);
            }
            mAdapter.notifyDataSetChanged();
        }
        registerForContextMenu(listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;
            case R.id.action_send :
                String title = mInputTitle.getEditableText().toString();
                String content = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ? Html.toHtml(mInputContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) : Html.toHtml(mInputContent.getText());
                if (!mInputTitle.getText().toString().isEmpty() && !(TextUtils.isEmpty(mInputContent.getText()) && mContents.size() == 0)) {
                    mMakeHtmlImages = new StringBuilder();
                    mImageList.clear();
                    mProgressDialog.setMessage("전송중...");
                    mProgressDialog.setProgressStyle(mContents.size() > 0 ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
                    showProgressDialog();

                    if (mContents.size() > 0) {
                        int position = 0;
                        uploadImage(position, mContents.get(position));
                    } else
                        actionSend(title, content);
                } else
                    Toast.makeText(getApplicationContext(), (TextUtils.isEmpty(mInputTitle.getText()) ? "제목" : "내용") + "을 입력하세요.", Toast.LENGTH_LONG).show();
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
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case 1 :
                int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position - 1;
                mContents.remove(position);
                mAdapter.notifyDataSetChanged();
                return true;
            case 2 :
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CAMERA_PICK_IMAGE_REQUEST_CODE);
                return true;
            case 3 :
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
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if (requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            bitmap = new BitmapUtil(this).bitmapResize(fileUri, 200);

            WriteItem writeItem = new WriteItem();
            writeItem.setBitmap(bitmap);
            writeItem.setFileUri(fileUri);

            mContents.add(writeItem);
            mAdapter.notifyDataSetChanged();
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
                    WriteItem writeItem = new WriteItem(mPhotoUri, rotatedBitmap, null);

                    mContents.add(writeItem);
                    mAdapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(final int position, final WriteItem writeItem) {
        if (writeItem.getImage() != null) {
            imageUploadProcess(position, writeItem.getImage(), false);
        } else {
            MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, EndPoint.IMAGE_UPLOAD, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String imageSrc = new String(response.data);
                    imageSrc = EndPoint.BASE_URL + imageSrc.substring(imageSrc.lastIndexOf("/ilosfiles2/"), imageSrc.lastIndexOf("\""));
                    imageUploadProcess(position, imageSrc, true);
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
                    params.put("file", new DataPart(System.currentTimeMillis() + position + ".jpg", getFileDataFromDrawable(writeItem.getBitmap())));
                    return params;
                }

                private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                    return byteArrayOutputStream.toByteArray();
                }
            };
            app.AppController.getInstance().addToRequestQueue(multipartRequest);
        }
    }

    private void imageUploadProcess(int count, String imageUrl, boolean isFlag) {
        mImageList.add(imageUrl);
        mProgressDialog.setProgress((int) ((double) (count) / mContents.size() * 100));
        try {
            mMakeHtmlImages.append("<p><img src=\"" + imageUrl + "\" width=\"488\"><p>" + (count < mContents.size() - 1 ? "<br>": ""));
            if (count < mContents.size() - 1) {
                count++;
                Thread.sleep(isFlag ? 700 : 0);
                uploadImage(count, mContents.get(count));
            } else {
                String title = mInputTitle.getEditableText().toString();
                String content = (!TextUtils.isEmpty(mInputContent.getText()) ? Html.toHtml(mInputContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) + "<p><br data-mce-bogus=\"1\"></p>" : "") + mMakeHtmlImages.toString();
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
                try {
                    hideProgressDialog();

                    Toast.makeText(getApplicationContext(), "수정완료", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ModifyActivity.this, ArticleActivity.class);
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
        app.AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
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
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);
                if (articleItem != null) {
                    articleItem.setTitle(mInputTitle.getText().toString());
                    articleItem.setContent(TextUtils.isEmpty(mInputContent.getText()) ? null : mInputContent.getText().toString());
                    articleItem.setImages(mImageList.isEmpty() ? null : mImageList);
                    query.getRef().setValue(articleItem);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
