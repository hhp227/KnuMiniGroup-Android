package com.hhp227.knu_minigroup.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hhp227.knu_minigroup.adapter.MemberGridAdapter;
import com.hhp227.knu_minigroup.databinding.FragmentTab3Binding;
import com.hhp227.knu_minigroup.dto.MemberItem;
import com.hhp227.knu_minigroup.viewmodel.Tab3ViewModel;

// TODO
public class Tab3Fragment extends Fragment {
    private MemberGridAdapter mAdapter;

    private FragmentTab3Binding mBinding;

    private Tab3ViewModel mViewModel;

    public static Tab3Fragment newInstance(String grpId) {
        Tab3Fragment fragment = new Tab3Fragment();
        Bundle args = new Bundle();

        args.putString("grp_id", grpId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab3Binding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(Tab3ViewModel.class);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        mAdapter = new MemberGridAdapter();

        mAdapter.setHasStableIds(true);
        mAdapter.setOnItemClickListener(new MemberGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MemberItem memberItem = mAdapter.getCurrentList().get(position);
                String uid = memberItem.uid;
                String name = memberItem.name;
                String value = memberItem.value;
                Bundle args = new Bundle();
                UserDialogFragment newFragment = UserDialogFragment.newInstance();

                args.putString("uid", uid);
                args.putString("name", name);
                args.putString("value", value);
                newFragment.setArguments(args);
                newFragment.show(getChildFragmentManager(), "dialog");
            }
        });
        mBinding.rvMember.setLayoutManager(layoutManager);
        mBinding.rvMember.setAdapter(mAdapter);
        mBinding.rvMember.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    mViewModel.fetchNextPage();
                }
            }
        });
        mBinding.srlMember.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.refresh();
                        mBinding.srlMember.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        mViewModel.getState().observe(getViewLifecycleOwner(), new Observer<Tab3ViewModel.State>() {
            @Override
            public void onChanged(Tab3ViewModel.State state) {
                if (state.isLoading) {
                    if (!state.hasRequestedMore) {
                        showProgressBar();
                    }
                } else if (state.hasRequestedMore) {
                    mViewModel.fetchMemberList(state.offset);
                } else if (!state.memberItems.isEmpty()) {
                    hideProgressBar();
                    mAdapter.submitList(state.memberItems);
                } else if (state.message != null && !state.message.isEmpty()) {
                    hideProgressBar();
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    public void onProfileActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showProgressBar() {
        if (mBinding.pbMember.getVisibility() == View.INVISIBLE)
            mBinding.pbMember.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbMember.getVisibility() == View.VISIBLE)
            mBinding.pbMember.setVisibility(View.INVISIBLE);
    }
}
