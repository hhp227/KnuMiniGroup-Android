package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.adapter.SeatListAdapter;
import com.hhp227.knu_minigroup.databinding.FragmentSeatBinding;
import com.hhp227.knu_minigroup.dto.SeatItem;
import com.hhp227.knu_minigroup.handler.OnFragmentListEventListener;
import com.hhp227.knu_minigroup.viewmodel.SangjuSeatViewModel;

import java.util.List;

public class SangjuSeatFragment extends Fragment implements OnFragmentListEventListener {
    private SeatListAdapter mAdapter;

    private FragmentSeatBinding mBinding;

    private SangjuSeatViewModel mViewModel;

    public static SangjuSeatFragment newInstance() {
        return new SangjuSeatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentSeatBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(SangjuSeatViewModel.class);
        mAdapter = new SeatListAdapter();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
        mBinding.recyclerView.setAdapter(mAdapter);
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        mViewModel.getItemList().observe(getViewLifecycleOwner(), new Observer<List<SeatItem>>() {
            @Override
            public void onChanged(List<SeatItem> seatItemList) {
                mAdapter.submitList(seatItemList);
            }
        });
        mViewModel.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
