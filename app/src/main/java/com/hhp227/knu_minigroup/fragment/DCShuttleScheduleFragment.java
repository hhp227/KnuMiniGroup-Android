package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.FragmentShuttleScheduleBinding;
import com.hhp227.knu_minigroup.handler.OnFragmentListEventListener;
import com.hhp227.knu_minigroup.viewmodel.DCShuttleScheduleViewModel;

import java.util.List;
import java.util.Map;

public class DCShuttleScheduleFragment extends Fragment implements OnFragmentListEventListener {
    private SimpleAdapter mAdapter;

    private FragmentShuttleScheduleBinding mBinding;

    private DCShuttleScheduleViewModel mViewModel;

    public static DCShuttleScheduleFragment newInstance() {
        return new DCShuttleScheduleFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentShuttleScheduleBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DCShuttleScheduleViewModel.class);

        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
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
                mBinding.srlShuttle.setRefreshing(false);
            }
        }, 1000);
    }

    private void observeViewModelData() {
        mViewModel.isLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading) {

                }
            }
        });
        mViewModel.getItemList().observe(getViewLifecycleOwner(), new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> shuttleList) {
                if (!shuttleList.isEmpty()) {
                    mAdapter = new SimpleAdapter(getContext(), shuttleList, R.layout.shuttle_item, new String[] {"col1", "col2"}, new int[] {R.id.division, R.id.time_label});

                    mBinding.lvShuttle.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
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
