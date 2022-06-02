package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.FragmentShuttleScheduleScBinding;
import com.hhp227.knu_minigroup.viewmodel.SCShuttleScheduleViewModel;

public class SCShuttleScheduleFragment extends Fragment {
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
        mAdapter = new SimpleAdapter(
                getActivity(),
                mViewModel.mShuttleList,
                R.layout.shuttle_sc_item,
                new String[] {"col1", "col2", "col3", "col4", "col5", "col6", "col7"},
                new int[] {R.id.column1, R.id.column2, R.id.column3, R.id.column4, R.id.column5, R.id.column6, R.id.column7}
        );

        mBinding.lvShuttle.setAdapter(mAdapter);
        mBinding.srlShuttle.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        mViewModel.refresh();
                        mBinding.srlShuttle.setRefreshing(false); // 당겨서 새로고침 숨김
                    }
                }, 1000);
            }
        });
        mProgressDialog.setMessage("불러오는중...");
        mViewModel.mState.observe(getViewLifecycleOwner(), new Observer<SCShuttleScheduleViewModel.State>() {
            @Override
            public void onChanged(SCShuttleScheduleViewModel.State state) {
                if (state.isLoading) {
                    showProgressDialog();
                } else if (!state.list.isEmpty()) {
                    TextView[] textViews = new TextView[] {
                            mBinding.tvCol1,
                            mBinding.tvCol2,
                            mBinding.tvCol3,
                            mBinding.tvCol4,
                            mBinding.tvCol5,
                            mBinding.tvCol6,
                            mBinding.tvCol7
                    };

                    for (int i = 0; i < state.list.size(); i++) {
                        textViews[i].setText(state.list.get(i));
                    }
                    mAdapter.notifyDataSetChanged(); // 모든 작업이 끝나면 리스트 갱신
                    hideProgressDialog();
                } else if (state.message != null && !state.message.isEmpty()) {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
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
