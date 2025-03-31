package com.hhp227.knu_minigroup.activity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.ReplyListAdapter;
import com.hhp227.knu_minigroup.databinding.ActivityArticleBinding;
import com.hhp227.knu_minigroup.databinding.ArticleDetailBinding;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.ReplyItem;
import com.hhp227.knu_minigroup.fragment.Tab1Fragment;
import com.hhp227.knu_minigroup.handler.OnActivityArticleEventListener;
import com.hhp227.knu_minigroup.helper.MyYouTubeBaseActivity;
import com.hhp227.knu_minigroup.viewmodel.ArticleViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArticleActivity extends MyYouTubeBaseActivity implements OnActivityArticleEventListener {
    private static final int UPDATE_ARTICLE = 10;

    private static final int UPDATE_REPLY = 20;

    private ReplyListAdapter mAdapter;

    private ActivityArticleBinding mActivityArticleBinding;

    private ArticleDetailBinding mArticleDetailBinding;

    private ArticleViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityArticleBinding = DataBindingUtil.setContentView(this, R.layout.activity_article);
        mArticleDetailBinding = ArticleDetailBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(ArticleViewModel.class);
        mAdapter = new ReplyListAdapter();

        mActivityArticleBinding.setLifecycleOwner(this);
        mActivityArticleBinding.setViewModel(mViewModel);
        mActivityArticleBinding.setHandler(this);
        setAppBar(mActivityArticleBinding.toolbar);
        mArticleDetailBinding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.showContextMenu();
                return true;
            }
        });
        mActivityArticleBinding.lvArticle.addHeaderView(mArticleDetailBinding.getRoot());
        mActivityArticleBinding.lvArticle.setAdapter(mAdapter);
        registerForContextMenu(mActivityArticleBinding.lvArticle); // 콘텍스트메뉴
        observeViewModelData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityArticleBinding = null;
        mArticleDetailBinding = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mViewModel.mIsAuthorized) {
            menu.add(Menu.NONE, 1, Menu.NONE, "수정하기");
            menu.add(Menu.NONE, 2, Menu.NONE, "삭제하기");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case 1:
                Intent intent = new Intent(this, CreateArticleActivity.class);
                ArticleItem articleItem = mViewModel.getArticle().getValue();

                if (articleItem != null) {
                    intent.putExtra("grp_id", mViewModel.mGroupId);
                    intent.putExtra("sbjt", articleItem.getTitle());
                    intent.putExtra("txt", articleItem.getContent());
                    intent.putStringArrayListExtra("img", (ArrayList<String>) articleItem.getImages());
                    intent.putExtra("vid", articleItem.getYoutube());
                    intent.putExtra("grp_key", mViewModel.mGroupKey);
                    intent.putExtra("artl_key", mViewModel.mArticleKey);
                    intent.putExtra("type", 1);
                    startActivityForResult(intent, UPDATE_ARTICLE);
                }
                return true;
            case 2:
                mViewModel.deleteArticle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_ARTICLE && resultCode == RESULT_OK) {
            mViewModel.refresh();
        } else if (requestCode == UPDATE_REPLY && resultCode == RESULT_OK && data != null) {
            mViewModel.refreshReply();
        }
    }

    /**
     * 댓글을 길게 클릭하면 콘텍스트 메뉴가 뜸
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        boolean auth = !mAdapter.getCurrentList().isEmpty() && position != 0 && mAdapter.getCurrentList().get(position - 1).getValue().getUid().equals(mViewModel.getUser().getUid());

        menu.setHeaderTitle("작업선택");
        menu.add(Menu.NONE, 1, Menu.NONE, "내용 복사");
        if (position != 0 && auth) {
            menu.add(Menu.NONE, 2, Menu.NONE, "댓글 수정");
            menu.add(Menu.NONE, 3, Menu.NONE, "댓글 삭제");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final String replyKey = mAdapter.getCurrentList().isEmpty() || info.position == 0 ? null : mAdapter.getCurrentList().get(info.position - 1).getKey();
        ReplyItem replyItem = mAdapter.getCurrentList().isEmpty() || info.position == 0 ? null : mAdapter.getCurrentList().get(info.position - 1).getValue(); // 헤더가 있기때문에 포지션에서 -1을 해준다.

        if (replyItem != null) {
            final String replyId = replyItem.getId();

            switch (item.getItemId()) {
                case 1:
                    android.content.ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                    clipboard.setText(info.position == 0 ? mArticleDetailBinding.tvContent.getText().toString() : replyItem.getReply());
                    Toast.makeText(getApplicationContext(), "클립보드에 복사되었습니다!", Toast.LENGTH_SHORT).show();
                    return true;
                case 2:
                    Intent intent = new Intent(getBaseContext(), UpdateReplyActivity.class);
                    String reply = replyItem.getReply();

                    intent.putExtra("grp_id", mViewModel.mGroupId);
                    intent.putExtra("cmt", reply);
                    intent.putExtra("cmmt_num", replyId);
                    intent.putExtra("artl_key", mViewModel.mArticleKey);
                    intent.putExtra("cmmt_key", replyKey);
                    startActivityForResult(intent, UPDATE_REPLY);
                    return true;
                case 3:
                    mViewModel.deleteReply(replyKey);
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onReplyFocusChange(boolean hasFocus) {
        if (hasFocus)
            mActivityArticleBinding.lvArticle.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
    }

    @Override
    public void onRefresh() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewModel.refresh();
                mActivityArticleBinding.srlArticle.setRefreshing(false);
            }
        }, 1000);
    }

    private void setAppBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void observeViewModelData() {
        mViewModel.isSuccess().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSuccess) {
                if (isSuccess) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        mViewModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Snackbar.make(mActivityArticleBinding.lvArticle, message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
        mViewModel.getReplyItemList().observe(this, new Observer<List<Map.Entry<String, ReplyItem>>>() {
            @Override
            public void onChanged(List<Map.Entry<String, ReplyItem>> replyItemList) {
                mAdapter.submitList(replyItemList);
            }
        });
        mViewModel.getReplyError().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                Toast.makeText(ArticleActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
        mViewModel.getArticle().observe(this, new Observer<ArticleItem>() {
            @Override
            public void onChanged(ArticleItem articleItem) {
                mArticleDetailBinding.setArticleItem(articleItem);
                mArticleDetailBinding.setCookie(mViewModel.getCookie());
                mArticleDetailBinding.setOnImageClickListener(position -> {
                    Intent intent = new Intent(ArticleActivity.this, PictureActivity.class);

                    intent.putStringArrayListExtra("images", (ArrayList<String>) articleItem.getImages());
                    intent.putExtra("position", position);
                    startActivity(intent);
                    return false;
                });
            }
        });
        mViewModel.isArticleUpdated().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUpdateArticle) {
                if (isUpdateArticle) {
                    ArticleItem articleItem = mViewModel.getArticle().getValue();

                    if (articleItem != null) {
                        Intent intent = new Intent(getApplicationContext(), Tab1Fragment.class);

                        intent.putExtra("position", mViewModel.mPosition);
                        intent.putExtra("sbjt", articleItem.getTitle());
                        intent.putExtra("txt", articleItem.getContent());
                        intent.putStringArrayListExtra("img", (ArrayList<String>) articleItem.getImages());
                        intent.putExtra("cmmt_cnt", articleItem.getReplyCount());
                        intent.putExtra("youtube", articleItem.getYoutube());
                        setResult(RESULT_OK, intent);
                    }
                }
            }
        });
        mViewModel.getScrollToLastState().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isScrollToLast) {
                if (isScrollToLast) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputMethodManager.hideSoftInputFromWindow(mActivityArticleBinding.lvArticle.getWindowToken(), 0);
                    setListViewBottom();
                }
            }
        });
    }

    /**
     * 리스트뷰 하단으로 간다.
     */
    private void setListViewBottom() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                int articleHeight = mArticleDetailBinding.getRoot().getMeasuredHeight();

                mActivityArticleBinding.lvArticle.setSelection(articleHeight);
            }
        }, 500);
    }
}