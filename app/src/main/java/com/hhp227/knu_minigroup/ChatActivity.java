package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.adapter.MessageListAdapter;
import com.hhp227.knu_minigroup.dto.MessageItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends Activity {
    private ActionBar actionBar;
    private DatabaseReference databaseReference;
    private EditText inputMessage;
    private List<MessageItem> messageItemList;
    private ListView listView;
    private MessageListAdapter messageListAdapter;
    private String sender, receiver;
    private TextView buttonSend;
    private User user;

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
        sender = user.getImageId();
        receiver = getIntent().getStringExtra("image");
        messageListAdapter = new MessageListAdapter(this, messageItemList, sender);
        actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
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
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageItemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageItem message = snapshot.getValue(MessageItem.class);
                    if (message.getFrom().equals(sender) && message.getTo().equals(receiver) || message.getFrom().equals(receiver) && message.getTo().equals(sender))
                        messageItemList.add(message);
                }
                messageListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
