package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ReplyItem;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

import java.util.HashMap;
import java.util.Map;

public class ReplyModifyActivity extends Activity {
    private static final String TAG = "댓글수정";
    private EditText inputReply;
    private ListView listView;
    private ProgressDialog progressDialog;
    private View headerView;
    private String groupId, articleId, replyId, reply, articleKey, replyKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_modify);
        headerView = getLayoutInflater().inflate(R.layout.modify_text, null, false);
        inputReply = headerView.findViewById(R.id.et_reply);
        listView = findViewById(R.id.lv_write);
        progressDialog = new ProgressDialog(this);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("grp_id");
        articleId = intent.getStringExtra("artl_num");
        replyId = intent.getStringExtra("cmmt_num");
        articleKey = intent.getStringExtra("artl_key");
        replyKey = intent.getStringExtra("cmmt_key");
        reply = intent.getStringExtra("cmt");
        reply = reply.contains("※") ? reply.substring(0, reply.lastIndexOf("※")).trim() : reply;
        ActionBar actionBar = getActionBar();
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
        listView.addHeaderView(headerView);
        listView.setAdapter(null);
        inputReply.setText(reply);
        progressDialog.setCancelable(false);
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
                final String text = inputReply.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    String tag_string_req = "req_send";
                    progressDialog.setMessage("전송중...");
                    showProgressDialog();

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_REPLY, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                // 입력 자판 숨기기
                                View view = ReplyModifyActivity.this.getCurrentFocus();
                                if (view != null) {
                                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                                Intent intent = new Intent(ReplyModifyActivity.this, ArticleActivity.class);
                                intent.putExtra("update_reply", response);
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
                            VolleyLog.e(TAG, error.getMessage());
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
                            params.put("CLUB_GRP_ID", groupId);
                            params.put("ARTL_NUM", articleId);
                            params.put("CMMT_NUM", replyId);
                            params.put("CMT", text);
                            return params;
                        }
                    };
                    app.AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
                } else
                    Toast.makeText(getApplicationContext(), "내용을 입력하세요.", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        updateReplyDataToFirebase(databaseReference.child(articleKey).child(replyKey));
    }

    private void updateReplyDataToFirebase(final Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ReplyItem replyItem = dataSnapshot.getValue(ReplyItem.class);
                    replyItem.setReply(inputReply.getText().toString() + "\n");
                    query.getRef().setValue(replyItem);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
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
