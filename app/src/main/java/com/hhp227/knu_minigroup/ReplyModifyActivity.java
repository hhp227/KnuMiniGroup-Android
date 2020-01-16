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
    private EditText mInputReply;
    private ProgressDialog mProgressDialog;
    private String mGroupId, mArticleId, mReplyId, mReply, mArticleKey, mReplyKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_modify);
        View headerView = getLayoutInflater().inflate(R.layout.modify_text, null, false);
        ActionBar actionBar = getActionBar();
        ListView listView = findViewById(R.id.lv_write);
        Intent intent = getIntent();
        mInputReply = headerView.findViewById(R.id.et_reply);
        mProgressDialog = new ProgressDialog(this);

        mGroupId = intent.getStringExtra("grp_id");
        mArticleId = intent.getStringExtra("artl_num");
        mReplyId = intent.getStringExtra("cmmt_num");
        mArticleKey = intent.getStringExtra("artl_key");
        mReplyKey = intent.getStringExtra("cmmt_key");
        mReply = intent.getStringExtra("cmt");
        mReply = mReply.contains("※") ? mReply.substring(0, mReply.lastIndexOf("※")).trim() : mReply;
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
        mInputReply.setText(mReply);
        mProgressDialog.setCancelable(false);
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
                final String text = mInputReply.getText().toString().trim();
                if (!TextUtils.isEmpty(text)) {
                    String tag_string_req = "req_send";
                    mProgressDialog.setMessage("전송중...");
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
                            params.put("CLUB_GRP_ID", mGroupId);
                            params.put("ARTL_NUM", mArticleId);
                            params.put("CMMT_NUM", mReplyId);
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
        updateReplyDataToFirebase(databaseReference.child(mArticleKey).child(mReplyKey));
    }

    private void updateReplyDataToFirebase(final Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ReplyItem replyItem = dataSnapshot.getValue(ReplyItem.class);
                    replyItem.setReply(mInputReply.getText().toString() + "\n");
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
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
