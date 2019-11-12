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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.hhp227.knu_minigroup.adapter.WriteListAdapter;
import com.hhp227.knu_minigroup.dto.WriteItem;
import com.hhp227.knu_minigroup.helper.BitmapUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hhp227.knu_minigroup.WriteActivity.CAMERA_PICK_IMAGE_REQUEST_CODE;
import static com.hhp227.knu_minigroup.WriteActivity.REQUEST_IMAGE_CAPTURE;

public class ModifyActivity extends Activity {
    private static final String TAG = ModifyActivity.class.getSimpleName();
    private EditText inputTitle, inputContent;
    private LinearLayout buttonImage;
    private List<WriteItem> contents;
    private ProgressDialog progressDialog;
    private Uri photoUri;
    private List<String> imageList;
    private ListView listView;
    private View headerView;
    private WriteListAdapter listAdapter;

    private int contextMenuRequest;
    private String currentPhotoPath, cookie, title, content;

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
        cookie = app.AppController.getInstance().getPreferenceManager().getCookie();
        listAdapter = new WriteListAdapter(getApplicationContext(), R.layout.write_content, contents);
        progressDialog = new ProgressDialog(this);

        Intent intent = getIntent();
        //Toast.makeText(getApplicationContext(), "grpId : " + intent.getIntExtra("grp_id", 0), Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), "artlNum : " + intent.getIntExtra("artl_num", 0), Toast.LENGTH_LONG).show();
        title = intent.getStringExtra("sbjt");
        content = intent.getStringExtra("txt");
        imageList = intent.getStringArrayListExtra("img");

        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contextMenuRequest = 2;
                registerForContextMenu(v);
                openContextMenu(v);
                unregisterForContextMenu(v);
            }
        });
        inputTitle.setText(title);
        inputContent.setText(content);
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
        if (imageList.size() > 0) {
            for (String imageUrl : imageList) {
                WriteItem writeItem = new WriteItem(null, null, imageUrl);
                contents.add(writeItem);
            }
            //listAdapter.notifyDataSetChanged();
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
