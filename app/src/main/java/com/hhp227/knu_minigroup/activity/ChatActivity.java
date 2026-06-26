package com.hhp227.knu_minigroup.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hhp227.knu_minigroup.adapter.MessageListAdapter;
import com.hhp227.knu_minigroup.databinding.ActivityChatBinding;
import com.hhp227.knu_minigroup.dto.MessageItem;
import com.hhp227.knu_minigroup.viewmodel.ChatViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private int mCurrentScrollState;

    private boolean mHasSelection;

    private ActivityChatBinding mBinding;

    private MessageListAdapter mAdapter;

    private ChatViewModel mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityChatBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        mAdapter = new MessageListAdapter(new ArrayList<MessageItem>(), mViewModel.getUser().getUid());

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            boolean isGroupChat = getIntent().getBooleanExtra("grp_chat", false);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra("chat_nm") + (isGroupChat ? " 그룹채팅방" : ""));
        }
        subscribeUi();
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

    private void subscribeUi() {
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.lvMessage.setAdapter(mAdapter);
        mBinding.etInputMsg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.lvMessage.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
                }
            }
        });
        mBinding.lvMessage.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mCurrentScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mHasSelection = firstVisibleItem + visibleItemCount > totalItemCount - 20;
                if (firstVisibleItem == 0 && mCurrentScrollState != SCROLL_STATE_IDLE) {
                    mViewModel.fetchPreviousPage();
                }
            }
        });
        mViewModel.getMessageItemList().observe(this, new Observer<List<MessageItem>>() {
            @Override
            public void onChanged(List<MessageItem> messageItems) {
                mAdapter.submitList(messageItems);
            }
        });
        mViewModel.getScrollEvent().observe(this, new Observer<ChatViewModel.ScrollEvent>() {
            @Override
            public void onChanged(ChatViewModel.ScrollEvent scrollEvent) {
                if (scrollEvent == null || (!scrollEvent.initialLoad && !mHasSelection && !scrollEvent.requestedMore)) {
                    return;
                }
                try {
                    mBinding.lvMessage.setSelection(scrollEvent.initialLoad ? scrollEvent.itemCount : scrollEvent.addedCount);
                } catch (Exception ignored) {
                }
            }
        });
        mViewModel.getMessageFormState().observe(this, new Observer<ChatViewModel.InputMessageFormState>() {
            @Override
            public void onChanged(ChatViewModel.InputMessageFormState inputMessageFormState) {
                if (inputMessageFormState != null && inputMessageFormState.messageError != null) {
                    Toast.makeText(ChatActivity.this, inputMessageFormState.messageError, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
