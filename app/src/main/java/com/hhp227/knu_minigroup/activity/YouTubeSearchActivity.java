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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.YouTubeListAdapter;
import com.hhp227.knu_minigroup.databinding.ActivityListBinding;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.viewmodel.YoutubeSearchViewModel;

// TODO
public class YouTubeSearchActivity extends AppCompatActivity {
    private YouTubeListAdapter mAdapter;

    private ActivityListBinding mBinding;

    private YoutubeSearchViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityListBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(YoutubeSearchViewModel.class);
        mAdapter = new YouTubeListAdapter();

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
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
        mBinding.srlList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
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
        });
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.setAdapter(mAdapter);
        mViewModel.getState().observe(this, new Observer<YoutubeSearchViewModel.State>() {
            @Override
            public void onChanged(YoutubeSearchViewModel.State state) {
                if (state.isLoading) {
                    showProgressBar();
                } else if (!state.youTubeItems.isEmpty()) {
                    hideProgressBar();
                    mAdapter.submitList(state.youTubeItems);
                } else if (state.message != null && !state.message.isEmpty()) {
                    hideProgressBar();
                    Snackbar.make(findViewById(android.R.id.content), state.message, Snackbar.LENGTH_LONG).show();
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

    private void showProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.GONE)
            mBinding.pbGroup.setVisibility(View.VISIBLE);
        if (!mBinding.sflGroup.isShimmerStarted())
            mBinding.sflGroup.startShimmer();
        if (mBinding.sflGroup.getVisibility() == View.GONE)
            mBinding.sflGroup.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.VISIBLE)
            mBinding.pbGroup.setVisibility(View.GONE);
        if (mBinding.sflGroup.isShimmerStarted())
            mBinding.sflGroup.stopShimmer();
        if (mBinding.sflGroup.getVisibility() == View.VISIBLE)
            mBinding.sflGroup.setVisibility(View.GONE);
    }
}
