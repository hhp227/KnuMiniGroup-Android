package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hhp227.knu_minigroup.adapter.WriteListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.WriteItem;
import com.hhp227.knu_minigroup.helper.BitmapUtil;
import com.hhp227.knu_minigroup.volley.util.MultipartRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WriteActivity extends Activity {
    public static final int CAMERA_PICK_IMAGE_REQUEST_CODE = 100;
    public static final int REQUEST_IMAGE_CAPTURE = 200;
    private static final String TAG = WriteActivity.class.getSimpleName();
    private int contextMenuRequest;
    private EditText inputTitle, inputContent;
    private LinearLayout buttonImage;
    private List<WriteItem> contents;
    private ListView listView;
    private ProgressDialog progressDialog;
    private String currentPhotoPath;
    private StringBuilder makeHtmlImages;
    private Uri photoUri;
    private View headerView;
    private WriteListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        buttonImage = findViewById(R.id.ll_image);
        listView = findViewById(R.id.lv_write);
        headerView = getLayoutInflater().inflate(R.layout.write_text, null, false);
        inputTitle = headerView.findViewById(R.id.et_title);
        inputContent = headerView.findViewById(R.id.et_content);
        contents = new ArrayList<>();
        listAdapter = new WriteListAdapter(getApplicationContext(), R.layout.write_content, contents);
        progressDialog = new ProgressDialog(this);

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contextMenuRequest = 2;
                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        listView.addHeaderView(headerView);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                contextMenuRequest = 1;
                view.showContextMenu();
            }
        });
        progressDialog.setCancelable(false);
        registerForContextMenu(listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home :
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return true;
            case R.id.action_send :
                makeHtmlImages = new StringBuilder();

                int grpId = getIntent().getIntExtra("grp_id", 0);
                String title = inputTitle.getEditableText().toString();
                String content = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ? Html.toHtml(inputContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) : Html.toHtml(inputContent.getText());
                if (!title.isEmpty()) {
                    progressDialog.setMessage("전송중...");
                    progressDialog.setProgressStyle(contents.size() > 0 ? ProgressDialog.STYLE_HORIZONTAL : ProgressDialog.STYLE_SPINNER);
                    showProgressDialog();

                    if (contents.size() > 0) {
                        int position = 0;
                        uploadImage(position, contents.get(position).getBitmap());
                    } else
                        sendSuccess(grpId, title, content);
                } else
                    Toast.makeText(getApplicationContext(), "제목을 입력하세요", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        switch (contextMenuRequest) {
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
                contents.remove(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position - 1);
                listAdapter.notifyDataSetChanged();
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
                        photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
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

            contents.add(writeItem);
            listAdapter.notifyDataSetChanged();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                bitmap = new BitmapUtil(this).bitmapResize(photoUri, 200);
                if (bitmap != null) {
                    ExifInterface ei = new ExifInterface(currentPhotoPath);
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    int angle = orientation == ExifInterface.ORIENTATION_ROTATE_90 ? 90
                              : orientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180
                              : orientation == ExifInterface.ORIENTATION_ROTATE_270 ? 270
                              : 0;
                    Bitmap rotatedBitmap = new BitmapUtil(this).rotateImage(bitmap, angle);
                    WriteItem writeItem = new WriteItem(photoUri, rotatedBitmap, null);

                    contents.add(writeItem);
                    listAdapter.notifyDataSetChanged();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(final int position, final Bitmap bitmap) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, EndPoint.IMAGE_UPLOAD, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                int count = position;
                progressDialog.setProgress((int) ((double) (count) / contents.size() * 100));
                try {
                    String imageSrc = new String(response.data);
                    imageSrc = EndPoint.BASE_URL + imageSrc.substring(imageSrc.lastIndexOf("/ilosfiles2/"), imageSrc.lastIndexOf("\""));
                    makeHtmlImages.append("<p><img src=\"" + imageSrc + "\" width=\"488\"><p>");
                    if (count < contents.size() - 1) {
                        count++;
                        Thread.sleep(700);
                        uploadImage(count, contents.get(count).getBitmap());
                    } else {
                        int grpId = getIntent().getIntExtra("grp_id", 0);
                        String title = inputTitle.getEditableText().toString();
                        String content = (!TextUtils.isEmpty(inputContent.getText()) ? Html.toHtml(inputContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) + "<p><br data-mce-bogus=\"1\"></p>" : "") + makeHtmlImages.toString();
                        sendSuccess(grpId, title, content);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "이미지 업로드 실패", Toast.LENGTH_LONG).show();
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
                headers.put("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie());
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

    private void sendSuccess(final int grpId, final String title, final String content) {
        String tagStringReq = "req_send";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.WRITE_FEED, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("isError");
                    if (!error) {
                        Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(WriteActivity.this, GroupActivity.class);
                        intent.putExtra("grp_id", grpId);
                        // 이전 Activity 초기화
                        intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "에러 : " + e.getMessage());
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
                headers.put("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("SBJT", title);
                params.put("CLUB_GRP_ID", String.valueOf(grpId));
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

        currentPhotoPath = image.getAbsolutePath();
        return image;
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
