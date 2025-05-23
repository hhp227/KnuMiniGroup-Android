package com.hhp227.knu_minigroup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.YouTubeListAdapter;
import com.hhp227.knu_minigroup.databinding.ActivityListBinding;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.handler.OnActivityListEventListener;
import com.hhp227.knu_minigroup.viewmodel.YoutubeSearchViewModel;

import java.util.List;

public class YouTubeSearchActivity extends AppCompatActivity implements OnActivityListEventListener {
    private YouTubeListAdapter mAdapter;

    private ActivityListBinding mBinding;

    private YoutubeSearchViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_list);
        mViewModel = new ViewModelProvider(this).get(YoutubeSearchViewModel.class);
        mAdapter = new YouTubeListAdapter();

        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setHandler(this);
        setAppBar(mBinding.toolbar);
        mAdapter.setOnItemClickListener(new YouTubeListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                YouTubeItem youTubeItem = mAdapter.getCurrentList().get(position);
                youTubeItem.position = -1;
                Intent intent = new Intent(getApplicationContext(), CreateArticleActivity.class);

                intent.putExtra("youtube", youTubeItem);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mAdapter.setHasStableIds(true);
        mBinding.recyclerView.setAdapter(mAdapter);
        observeViewModelData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.sflGroup.clearAnimation();
        mBinding.sflGroup.removeAllViews();
        mBinding = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search, menu);
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });
        searchView.setQueryHint("검색어를 입력하세요.");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mViewModel.setQuery(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRefresh() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewModel.refresh();
                mBinding.srlList.setRefreshing(false);
            }
        }, 1000);
    }

    private void setAppBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(this, new Observer<List<YouTubeItem>>() {
            @Override
            public void onChanged(List<YouTubeItem> youTubeItems) {
                mAdapter.submitList(youTubeItems);
            }
        });
        mViewModel.getMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
        mViewModel.getQuery().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                mViewModel.requestData(s);
            }
        });
    }
}
