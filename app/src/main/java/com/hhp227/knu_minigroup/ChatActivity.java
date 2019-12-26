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
import androidx.annotation.NonNull;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.adapter.MessageListAdapter;
import com.hhp227.knu_minigroup.dto.MessageItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends Activity {
    private static final int LIMIT = 20;
    private DatabaseReference databaseReference;
    private EditText inputMessage;
    private List<MessageItem> messageItemList;
    private ListView listView;
    private MessageListAdapter messageListAdapter;
    private Query query;
    private String sender, receiver;
    private TextView buttonSend;
    private User user;
    private boolean hasRequestedMore, isGroupChat;
    private int currentScrollState;
    private String cursor;

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
        isGroupChat = intent.getBooleanExtra("grp_chat", false);
        messageListAdapter = new MessageListAdapter(this, messageItemList, sender);
        query = databaseReference.orderByKey().limitToLast(LIMIT);
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
        query.addValueEventListener(new ValueEventListener() {
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
                setFocusMessage(messageItemList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                currentScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean loadMore = firstVisibleItem == 0;
                if (!hasRequestedMore && loadMore && currentScrollState != SCROLL_STATE_IDLE) {
                    hasRequestedMore = true;
                    fetchMessageList();
                }
            }
        });
        listView.setAdapter(messageListAdapter);
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

    private void fetchMessageList() {
        final List<MessageItem> messageList = new ArrayList<>();
        query = databaseReference.orderByKey().endAt(cursor).limitToLast(LIMIT);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(getApplicationContext(), cursor, Toast.LENGTH_LONG).show();
                cursor = dataSnapshot.getChildren().iterator().next().getKey();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageItem message = snapshot.getValue(MessageItem.class);
                    messageList.add(message);
                }
                messageList.remove(messageList.size() - 1);
                messageItemList.addAll(0, messageList);
                messageListAdapter.notifyDataSetChanged();
                setFocusMessage(messageList.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setFocusMessage(int count) {
        if (hasRequestedMore) {
            int firstPosition = listView.getFirstVisiblePosition();
            View firstView = listView.getChildAt(0);
            int top = firstView != null ? firstView.getTop() : 0;
            listView.setSelectionFromTop(firstPosition + count, top);
        } else
            listView.setSelection(listView.getCount());
        hasRequestedMore = false;
    }

    private void sendMessage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> map = new HashMap<>();
        map.put("from", sender);
        map.put("to", receiver);
        map.put("name", user.getName());
        map.put("message", inputMessage.getText().toString());
        map.put("time", System.currentTimeMillis());

        reference.child("Messages").push().setValue(map);
    }
}
