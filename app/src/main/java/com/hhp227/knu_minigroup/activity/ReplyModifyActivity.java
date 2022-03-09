package com.hhp227.knu_minigroup.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityReplyModifyBinding;
import com.hhp227.knu_minigroup.databinding.ModifyTextBinding;
import com.hhp227.knu_minigroup.dto.ReplyItem;

import java.util.HashMap;
import java.util.Map;

public class ReplyModifyActivity extends AppCompatActivity {
    private static final String TAG = "댓글수정";

    private Holder mHolder;

    private ProgressDialog mProgressDialog;

    private String mGroupId, mArticleId, mReplyId, mReply, mArticleKey, mReplyKey;

    private ActivityReplyModifyBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityReplyModifyBinding.inflate(getLayoutInflater());

        setContentView(mBinding.getRoot());
        mProgressDialog = new ProgressDialog(this);
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra("grp_id");
        mArticleId = intent.getStringExtra("artl_num");
        mReplyId = intent.getStringExtra("cmmt_num");
        mArticleKey = intent.getStringExtra("artl_key");
        mReplyKey = intent.getStringExtra("cmmt_key");
        mReply = intent.getStringExtra("cmt");
        mReply = mReply.contains("※") ? mReply.substring(0, mReply.lastIndexOf("※")).trim() : mReply;

        mProgressDialog.setCancelable(false);
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBinding.rvWrite.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvWrite.setAdapter(new RecyclerView.Adapter<Holder>() {
            @NonNull
            @Override
            public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                mHolder = new Holder(ModifyTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                return mHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull Holder holder, int position) {
                holder.bind(mReply);
            }

            @Override
            public int getItemCount() {
                return 1;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
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
                final String text = mHolder.mBinding.etReply.getText().toString().trim();

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

                            headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
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
                    AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ReplyItem replyItem = dataSnapshot.getValue(ReplyItem.class);

                    replyItem.setReply(mHolder.mBinding.etReply.getText().toString() + "\n");
                    query.getRef().setValue(replyItem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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

    public static class Holder extends RecyclerView.ViewHolder {
        private final ModifyTextBinding mBinding;

        Holder(ModifyTextBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(String reply) {
            mBinding.etReply.setText(reply);
        }
    }
}
