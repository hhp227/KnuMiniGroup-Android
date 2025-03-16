package com.hhp227.knu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.activity.MainActivity;
import com.hhp227.knu_minigroup.activity.WebViewActivity;
import com.hhp227.knu_minigroup.adapter.BbsListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.FragmentListBinding;
import com.hhp227.knu_minigroup.dto.BbsItem;
import com.hhp227.knu_minigroup.handler.OnFragmentListEventListener;
import com.hhp227.knu_minigroup.viewmodel.UnivNoticeViewModel;

import java.util.List;

public class UnivNoticeFragment extends Fragment implements OnFragmentListEventListener {
    private BbsListAdapter mAdapter;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private FragmentListBinding mBinding;

    private UnivNoticeViewModel mViewModel;

    public static UnivNoticeFragment newInstance() {
        return new UnivNoticeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentListBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(UnivNoticeViewModel.class);
        mAdapter = new BbsListAdapter();
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(RecyclerView.LAYOUT_DIRECTION_RTL)) {
                    mViewModel.fetchNextPage();
                }
            }
        };
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.knu_board));
        mBinding.recyclerView.addOnScrollListener(mOnScrollListener);
        mBinding.recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BbsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                BbsItem bbsItem = mAdapter.getCurrentList().get(position);
                Intent intent = new Intent(v.getContext(), WebViewActivity.class);

                intent.putExtra("url", EndPoint.URL_KNU + bbsItem.getUrl());
                startActivity(intent);
            }
        });
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mOnScrollListener != null)
            mBinding.recyclerView.removeOnScrollListener(mOnScrollListener);
        mOnScrollListener = null;
        mBinding = null;
    }

    @Override
    public void onRefresh() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewModel.refresh();
                mBinding.srl.setRefreshing(false);
            }
        }, 1000);
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(getViewLifecycleOwner(), new Observer<List<BbsItem>>() {
            @Override
            public void onChanged(List<BbsItem> bbsItemList) {
                mAdapter.submitList(bbsItemList);
            }
        });
        mViewModel.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Snackbar.make(mBinding.recyclerView, message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}