package com.hhp227.knu_minigroup.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.GroupListAdapter;
import com.hhp227.knu_minigroup.databinding.ActivityListBinding;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.handler.OnActivityListEventListener;
import com.hhp227.knu_minigroup.viewmodel.GroupInfoViewModel;
import com.hhp227.knu_minigroup.viewmodel.RequestViewModel;

import java.util.List;
import java.util.Map;

public class RequestActivity extends AppCompatActivity implements OnActivityListEventListener {
    private GroupListAdapter mAdapter;

    private ActivityListBinding mBinding;

    private RequestViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_list);
        mViewModel = new ViewModelProvider(this).get(RequestViewModel.class);
        mAdapter = new GroupListAdapter(this);

        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setHandler(this);
        mBinding.setEmptyMessage("가입신청중인 그룹이 없습니다.");
        setAppBar(mBinding.toolbar);
        mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
        mAdapter.setButtonType(GroupInfoViewModel.TYPE_CANCEL);
        mBinding.recyclerView.setHasFixedSize(true);
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                RecyclerView.OnScrollListener onScrollListener = this;

                if (dy > 0 && manager != null && manager.findLastCompletelyVisibleItemPosition() >= manager.getItemCount() - 1) {
                    recyclerView.removeOnScrollListener(onScrollListener);
                    mViewModel.fetchNextPage();
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.addOnScrollListener(onScrollListener);
                        }
                    }, 500);
                }
            }
        });
        observeViewModelData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.recyclerView.clearOnScrollListeners();
        mBinding = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, 1000);
    }

    private void setAppBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(this, new Observer<List<Map.Entry<String, GroupItem>>>() {
            @Override
            public void onChanged(List<Map.Entry<String, GroupItem>> groupItemList) {
                mAdapter.submitList(groupItemList);
            }
        });
        mViewModel.hasRequestMore().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasRequestMore) {
                if (hasRequestMore) {
                    mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
                }
            }
        });
        mViewModel.isEndReached().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEndReached) {
                mAdapter.setFooterProgressBarVisibility(isEndReached ? View.GONE : View.INVISIBLE);
            }
        });
        mViewModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Snackbar.make(mBinding.recyclerView, message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public void refresh() {
        mBinding.srlList.setRefreshing(false);
        mViewModel.refresh();
    }
}
