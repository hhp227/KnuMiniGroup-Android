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
import androidx.recyclerview.widget.RecyclerView;

import com.hhp227.knu_minigroup.adapter.MemberGridAdapter;
import com.hhp227.knu_minigroup.databinding.FragmentTab3Binding;
import com.hhp227.knu_minigroup.dto.MemberItem;
import com.hhp227.knu_minigroup.handler.OnFragmentTab3EventListener;
import com.hhp227.knu_minigroup.viewmodel.Tab3ViewModel;

import java.util.List;

public class Tab3Fragment extends Fragment implements OnFragmentTab3EventListener {
    private MemberGridAdapter mAdapter;

    private FragmentTab3Binding mBinding;

    private Tab3ViewModel mViewModel;

    public static Tab3Fragment newInstance(String grpId, String key) {
        Tab3Fragment fragment = new Tab3Fragment();
        Bundle args = new Bundle();

        args.putString("grp_id", grpId);
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab3Binding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(Tab3ViewModel.class);
        mAdapter = new MemberGridAdapter();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
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
                mBinding.srlMember.setRefreshing(false);
            }
        }, 1000);
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(getViewLifecycleOwner(), new Observer<List<MemberItem>>() {
            @Override
            public void onChanged(List<MemberItem> memberItemList) {
                mAdapter.submitList(memberItemList);
            }
        });
        mViewModel.hasRequestMore().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasRequestMore) {
                if (hasRequestMore) {
                    mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
                }
            }
        });
        mViewModel.isEndReached().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEndReached) {
                mAdapter.setFooterProgressBarVisibility(isEndReached ? View.GONE : View.INVISIBLE);
            }
        });
        mViewModel.getMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String message) {
                if (message != null && !message.isEmpty()) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onProfileActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mAdapter.notifyDataSetChanged();
        }
    }
}