package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.FragmentShuttleScheduleScBinding;
import com.hhp227.knu_minigroup.handler.OnFragmentListEventListener;
import com.hhp227.knu_minigroup.viewmodel.SCShuttleScheduleViewModel;

import java.util.HashMap;
import java.util.List;

public class SCShuttleScheduleFragment extends Fragment implements OnFragmentListEventListener {
    private ProgressDialog mProgressDialog;

    private SimpleAdapter mAdapter;

    private FragmentShuttleScheduleScBinding mBinding;

    private SCShuttleScheduleViewModel mViewModel;

    public static SCShuttleScheduleFragment newInstance() {
        return new SCShuttleScheduleFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentShuttleScheduleScBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SCShuttleScheduleViewModel.class);
        mProgressDialog = new ProgressDialog(getActivity());

        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setViewModel(mViewModel);
        mBinding.setHandler(this);
        mProgressDialog.setMessage("불러오는중...");
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
            public void run() {
                mViewModel.refresh();
                mBinding.srlShuttle.setRefreshing(false); // 당겨서 새로고침 숨김
            }
        }, 1000);
    }

    private void observeViewModelData() {
        mViewModel.isLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (isLoading) {
                    showProgressDialog();
                }
            }
        });
        mViewModel.getItemList().observe(getViewLifecycleOwner(), new Observer<List<HashMap<String, String>>>() {
            @Override
            public void onChanged(List<HashMap<String, String>> shuttleList) {
                if (!shuttleList.isEmpty()) {
                    mAdapter = new SimpleAdapter(
                            getActivity(),
                            shuttleList,
                            R.layout.shuttle_sc_item,
                            new String[] {"col1", "col2", "col3", "col4", "col5", "col6", "col7"},
                            new int[] {R.id.column1, R.id.column2, R.id.column3, R.id.column4, R.id.column5, R.id.column6, R.id.column7}
                    );

                    mBinding.lvShuttle.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged(); // 모든 작업이 끝나면 리스트 갱신
                    hideProgressDialog();
                }
            }
        });
        mViewModel.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }
        });
    }

    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
