package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.adapter.SeatListAdapter;
import com.hhp227.knu_minigroup.databinding.FragmentSeatBinding;
import com.hhp227.knu_minigroup.viewmodel.SangjuSeatViewModel;

// TODO
public class SangjuSeatFragment extends Fragment {
    private SeatListAdapter mAdapter;

    private FragmentSeatBinding mBinding;

    private SangjuSeatViewModel mViewModel;

    public static SangjuSeatFragment newInstance() {
        return new SangjuSeatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentSeatBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SangjuSeatViewModel.class);
        mAdapter = new SeatListAdapter();

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.refresh();
                        mBinding.srl.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        mViewModel.getState().observe(getViewLifecycleOwner(), new Observer<SangjuSeatViewModel.State>() {
            @Override
            public void onChanged(SangjuSeatViewModel.State state) {
                if (state.isLoading) {
                    showProgressBar();
                } else if (!state.seatItemList.isEmpty()) {
                    hideProgressBar();
                    mAdapter.submitList(state.seatItemList);
                } else if (state.message != null && !state.message.isEmpty()) {
                    hideProgressBar();
                    Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void showProgressBar() {
        if (mBinding.progressCircular.getVisibility() == View.GONE)
            mBinding.progressCircular.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.progressCircular.getVisibility() == View.VISIBLE)
            mBinding.progressCircular.setVisibility(View.GONE);
    }
}
