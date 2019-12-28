package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import app.AppController;
import com.android.volley.*;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.adapter.MessageListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.MessageItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;
import com.hhp227.knu_minigroup.volley.util.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends Activity {
    private static final int LIMIT = 20;
    private DatabaseReference databaseReference;
    private EditText inputMessage;
    private List<MessageItem> messageItemList;
    private ListView listView;
    private MessageListAdapter messageListAdapter;
    private String cursor, sender, receiver, value;
    private TextView buttonSend;
    private User user;
    private boolean hasRequestedMore, hasSelection, isGroupChat;
    private int currentScrollState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        buttonSend = findViewById(R.id.tv_btn_send);
        inputMessage = findViewById(R.id.et_input_msg);
        listView = findViewById(R.id.lv_message);
        databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        messageItemList = new ArrayList<>();
        user = app.AppController.getInstance().getPreferenceManager().getUser();
        sender = user.getUid();
        Intent intent = getIntent();
        receiver = intent.getStringExtra("uid");
        value = intent.getStringExtra("value");
        isGroupChat = intent.getBooleanExtra("grp_chat", false);
        messageListAdapter = new MessageListAdapter(this, messageItemList, sender);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(intent.getStringExtra("chat_nm") + (isGroupChat ? " 그룹채팅방" : ""));
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputMessage.getText().toString().trim().length() > 0) {
                    sendMessage();
                    if (!isGroupChat)
                        sendLMSMessage();
                    inputMessage.setText("");
                } else
                    Toast.makeText(getApplicationContext(), "메시지를 입력하세요.", Toast.LENGTH_LONG).show();
            }
        });
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSend.setBackgroundResource(s.length() > 0 ? R.drawable.background_sendbtn_p : R.drawable.background_sendbtn_n);
                buttonSend.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.white : android.R.color.darker_gray));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
            }
        });
        /*query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageItemList.clear();
                cursor = dataSnapshot.getChildren().iterator().next().getKey();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageItem message = snapshot.getValue(MessageItem.class);
                    //if (!isGroupChat && message.getFrom().equals(sender) && message.getTo().equals(receiver) || !isGroupChat && message.getFrom().equals(receiver) && message.getTo().equals(sender) || isGroupChat && message.getTo().equals(receiver))
                    messageItemList.add(message);
                }
                messageListAdapter.notifyDataSetChanged();
                listView.setSelection(messageItemList.size() + 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                currentScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                hasSelection = firstVisibleItem + visibleItemCount > totalItemCount - 20;
                if (!hasRequestedMore && firstVisibleItem == 0 && currentScrollState != SCROLL_STATE_IDLE) {
                    hasRequestedMore = true;
                    fetchMessageList(isGroupChat ? databaseReference.child(receiver).orderByKey().endAt(cursor).limitToLast(LIMIT) : databaseReference.child(sender).child(receiver).orderByKey().endAt(cursor).limitToLast(LIMIT), messageItemList.size(), cursor);
                    cursor = null;
                }
            }
        });
        listView.setAdapter(messageListAdapter);
        fetchMessageList(isGroupChat ? databaseReference.child(receiver).orderByKey().limitToLast(LIMIT) : databaseReference.child(sender).child(receiver).orderByKey().limitToLast(LIMIT), 0, "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchMessageList(Query query, final int prevCnt, final String prevCursor) {
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Toast.makeText(getApplicationContext(), "여부 : " + hasSelection + " 커서 : " + cursor + " 이전 커서" + prevCursor + " 이전갯수 : " + prevCnt, Toast.LENGTH_LONG).show();
                if (cursor == null)
                    cursor = s;
                else if (prevCursor.equals(dataSnapshot.getKey())) {
                    hasRequestedMore = false;
                    return;
                }
                MessageItem messageItem = dataSnapshot.getValue(MessageItem.class);
                messageItemList.add(messageItemList.size() - prevCnt, messageItem);
                messageListAdapter.notifyDataSetChanged();
                if (hasSelection || hasRequestedMore)
                    listView.setSelection(prevCnt == 0 ? messageItemList.size() : messageItemList.size() - prevCnt + 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        /*query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!init && dataSnapshot.getChildrenCount() <= 1 || !dataSnapshot.hasChildren())
                    return;
                List<MessageItem> messageList = new ArrayList<>();
                cursor = dataSnapshot.getChildren().iterator().next().getKey();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageItem message = snapshot.getValue(MessageItem.class);
                    messageList.add(message);
                }
                if (init)
                    messageItemList.clear();
                else
                    messageList.remove(messageList.size() - 1);
                messageItemList.addAll(0, messageList);
                messageListAdapter.notifyDataSetChanged();
                listView.setSelection(messageList.size() + 1);
                hasRequestedMore = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/
    }

    private void sendMessage() {
        Map<String, Object> map = new HashMap<>();
        map.put("from", sender);
        map.put("name", user.getName());
        map.put("message", inputMessage.getText().toString());
        map.put("type", "text");
        map.put("seen", false);
        map.put("timestamp", System.currentTimeMillis());
        if (isGroupChat) {
            databaseReference.child(receiver).push().setValue(map);
        } else {
            String receiverPath = receiver + "/" + sender + "/";
            String senderPath = sender + "/" + receiver + "/";
            String pushId = databaseReference.child(sender).child(receiver).push().getKey();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put(receiverPath.concat(pushId), map);
            messageMap.put(senderPath.concat(pushId), map);

            databaseReference.updateChildren(messageMap);
        }
    }

    private void sendLMSMessage() {
        app.AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.SEND_MESSAGE, null, new Response.Listener<JSONObject>() {
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
                headers.put("Cookie", AppController.getInstance().getPreferenceManager().getCookie());
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();
                params.put("TXT", inputMessage.getText().toString());
                params.put("send_msg", "Y");
                params.put("USERS", value);
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
