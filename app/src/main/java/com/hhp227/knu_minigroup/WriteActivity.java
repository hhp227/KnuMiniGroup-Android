package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.adapter.WriteListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WriteActivity extends Activity {
    private static final String TAG = WriteActivity.class.getSimpleName();
    private EditText inputTitle, inputContent;
    private LinearLayout buttonImage;
    private List<String> contents;
    private ListView listView;
    private ProgressDialog progressDialog;
    private View headerView;
    private WriteListAdapter listAdapter;
    private int contextMenuRequest;

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
        progressDialog.setCancelable(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home :
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return true;
            case R.id.action_send :
                final int grpId = getIntent().getIntExtra("grp_id", 0);
                final String title = inputTitle.getEditableText().toString();
                final String content = inputContent.getText().toString().trim();
                if (!title.isEmpty()) {
                    String tagStringReq = "req_send";

                    progressDialog.setMessage("전송중...");
                    showProgressDialog();

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
        return super.onContextItemSelected(item);
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
