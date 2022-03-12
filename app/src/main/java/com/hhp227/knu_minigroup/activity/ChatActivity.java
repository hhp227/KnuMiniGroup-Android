package com.hhp227.knu_minigroup.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.MessageListAdapter;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityChatBinding;
import com.hhp227.knu_minigroup.dto.MessageItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.volley.util.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final int LIMIT = 20;

    private boolean mHasRequestedMore, mHasSelection, mIsGroupChat;

    private int mCurrentScrollState;

    private DatabaseReference mDatabaseReference;

    private List<MessageItem> mMessageItemList;

    private MessageListAdapter mAdapter;

    private String mCursor, mSender, mReceiver, mValue, mFirstMessageKey;

    private User mUser;

    private ActivityChatBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        mMessageItemList = new ArrayList<>();
        mUser = AppController.getInstance().getPreferenceManager().getUser();
        mSender = mUser.getUid();
        mReceiver = getIntent().getStringExtra("uid");
        mValue = getIntent().getStringExtra("value");
        mIsGroupChat = getIntent().getBooleanExtra("grp_chat", false);
        mAdapter = new MessageListAdapter(mMessageItemList, mSender);

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra("chat_nm") + (mIsGroupChat ? " 그룹채팅방" : ""));
        }
        mBinding.tvBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.etInputMsg.getText().toString().trim().length() > 0) {
                    sendMessage();
                    if (!mIsGroupChat)
                        sendLMSMessage();
                    mBinding.etInputMsg.setText("");
                } else
                    Toast.makeText(getApplicationContext(), "메시지를 입력하세요.", Toast.LENGTH_LONG).show();
            }
        });
        mBinding.etInputMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.tvBtnSend.setBackgroundResource(s.length() > 0 ? R.drawable.background_sendbtn_p : R.drawable.background_sendbtn_n);
                mBinding.tvBtnSend.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.white : android.R.color.darker_gray));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mBinding.etInputMsg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    mBinding.lvMessage.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            }
        });
        /*mQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMessageItemList.clear();
                mCursor = dataSnapshot.getChildren().iterator().next().getKey();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageItem message = snapshot.getValue(MessageItem.class);
                    //if (!isGroupChat && message.getFrom().equals(sender) && message.getTo().equals(receiver) || !isGroupChat && message.getFrom().equals(receiver) && message.getTo().equals(sender) || isGroupChat && message.getTo().equals(receiver))
                    mMessageItemList.add(message);
                }
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mMessageItemList.size() + 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/
        mBinding.lvMessage.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mCurrentScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mHasSelection = firstVisibleItem + visibleItemCount > totalItemCount - 20;
                if (!mHasRequestedMore && firstVisibleItem == 0 && mCurrentScrollState != SCROLL_STATE_IDLE) {
                    mHasRequestedMore = true;

                    fetchMessageList(mIsGroupChat ? mDatabaseReference.child(mReceiver).orderByKey().endAt(mCursor).limitToLast(LIMIT) : mDatabaseReference.child(mSender).child(mReceiver).orderByKey().endAt(mCursor).limitToLast(LIMIT), mMessageItemList.size(), mCursor);
                    mCursor = null;
                }
            }
        });
        mBinding.lvMessage.setAdapter(mAdapter);
        fetchMessageList(mIsGroupChat ? mDatabaseReference.child(mReceiver).orderByKey().limitToLast(LIMIT) : mDatabaseReference.child(mSender).child(mReceiver).orderByKey().limitToLast(LIMIT), 0, "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchMessageList(Query query, final int prevCnt, final String prevCursor) {
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                if (mFirstMessageKey != null && mFirstMessageKey.equals(dataSnapshot.getKey()))
                    return;
                else if (s == null)
                    mFirstMessageKey = dataSnapshot.getKey();
                if (mCursor == null)
                    mCursor = s;
                else if (prevCursor.equals(dataSnapshot.getKey())) {
                    mHasRequestedMore = false;
                    return;
                }
                MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);

                mMessageItemList.add(mMessageItemList.size() - prevCnt, messageItem);
                mAdapter.notifyDataSetChanged();
                if (mHasSelection || mHasRequestedMore)
                    mBinding.lvMessage.setSelection(prevCnt == 0 ? mMessageItemList.size() : mMessageItemList.size() - prevCnt + 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!init && dataSnapshot.getChildrenCount() <= 1 || !dataSnapshot.hasChildren())
                    return;
                List<MessageItem> messageList = new ArrayList<>();
                mCursor = dataSnapshot.getChildren().iterator().next().getKey();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageItem message = snapshot.getValue(MessageItem.class);
                    messageList.add(message);
                }
                if (init)
                    mMessageItemList.clear();
                else
                    messageList.remove(messageList.size() - 1);
                mMessageItemList.addAll(0, messageList);
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(messageList.size() + 1);
                mHasRequestedMore = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/
    }

    private void sendMessage() {
        Map<String, Object> map = new HashMap<>();

        map.put("from", mSender);
        map.put("name", mUser.getName());
        map.put("message", mBinding.etInputMsg.getText().toString());
        map.put("type", "text");
        map.put("seen", false);
        map.put("timestamp", System.currentTimeMillis());
        if (mIsGroupChat) {
            mDatabaseReference.child(mReceiver).push().setValue(map);
        } else {
            String receiverPath = mReceiver + "/" + mSender + "/";
            String senderPath = mSender + "/" + mReceiver + "/";
            String pushId = mDatabaseReference.child(mSender).child(mReceiver).push().getKey();
            Map<String, Object> messageMap = new HashMap<>();

            messageMap.put(receiverPath.concat(pushId), map);
            messageMap.put(senderPath.concat(pushId), map);
            mDatabaseReference.updateChildren(messageMap);
        }
    }

    private void sendLMSMessage() {
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.SEND_MESSAGE, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!response.getBoolean("isError"))
                        Log.d("채팅", response.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("TXT", mBinding.etInputMsg.getText().toString());
                params.put("send_msg", "Y");
                params.put("USERS", mValue);
                if (params.size() > 0) {
                    StringBuilder encodedParams = new StringBuilder();

                    try {
                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            encodedParams.append(URLEncoder.encode(entry.getKey(), getParamsEncoding()));
                            encodedParams.append('=');
                            encodedParams.append(URLEncoder.encode(entry.getValue(), getParamsEncoding()));
                            encodedParams.append('&');
                        }
                        return encodedParams.toString().getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                    }
                }
                return null;
            }
        }, "req_send_msg");
    }
}
