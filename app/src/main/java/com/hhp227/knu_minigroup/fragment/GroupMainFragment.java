package com.hhp227.knu_minigroup.fragment;

import static com.hhp227.knu_minigroup.adapter.GroupGridAdapter.TYPE_AD;
import static com.hhp227.knu_minigroup.adapter.GroupGridAdapter.TYPE_GROUP;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.activity.CreateGroupActivity;
import com.hhp227.knu_minigroup.activity.FindGroupActivity;
import com.hhp227.knu_minigroup.activity.GroupActivity;
import com.hhp227.knu_minigroup.activity.MainActivity;
import com.hhp227.knu_minigroup.activity.RequestActivity;
import com.hhp227.knu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.knu_minigroup.databinding.FragmentGroupMainBinding;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.handler.OnFragmentGroupMainEventListener;
import com.hhp227.knu_minigroup.viewmodel.GroupMainViewModel;

import java.util.List;
import java.util.Map;

public class GroupMainFragment extends Fragment implements OnFragmentGroupMainEventListener {
    private GroupGridAdapter mAdapter;

    private FragmentGroupMainBinding mBinding;

    private GroupMainViewModel mViewModel;

    private ActivityResultLauncher<Intent> mActivityResultLauncher;

    public static GroupMainFragment newInstance() {
        return new GroupMainFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentGroupMainBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(GroupMainViewModel.class);
        mAdapter = new GroupGridAdapter();
        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    mViewModel.refresh();
                    ((MainActivity) requireActivity()).updateProfileImage();
                }
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(getViewLifecycleOwner());
        mBinding.setHandler(this);
        mBinding.setSpanCount(getResources().getInteger(R.integer.group_main_span_count));
        mBinding.setOnSpanSizeListener(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer position) {
                return mAdapter.getItemViewType(position) == TYPE_AD || mAdapter.getItemViewType(position) == TYPE_GROUP ? 1 : mBinding.getSpanCount();
            }
        });
        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.main));
        mAdapter.setHasStableIds(true);
        mAdapter.setOnItemClickListener(new GroupGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                GroupItem groupItem = (GroupItem) mAdapter.getCurrentList().get(position).getValue();
                Intent intent = new Intent(getContext(), GroupActivity.class);

                intent.putExtra("admin", groupItem.isAdmin());
                intent.putExtra("grp_id", groupItem.getId());
                intent.putExtra("grp_nm", groupItem.getName());
                intent.putExtra("grp_img", groupItem.getImage()); // 경북대 소모임에는 없음
                intent.putExtra("key", mAdapter.getKey(position));
                mActivityResultLauncher.launch(intent);
            }
        });
        mAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.b_find:
                        mActivityResultLauncher.launch(new Intent(getContext(), FindGroupActivity.class));
                        return;
                    case R.id.b_create:
                        mActivityResultLauncher.launch(new Intent(getContext(), CreateGroupActivity.class));
                }
            }
        });
        mBinding.rvGroup.setAdapter(mAdapter);
        mBinding.srlGroup.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mBinding.bnvGroupButton.getMenu().getItem(0).setCheckable(false);
        if (mViewModel.getUser() == null) {
            ((MainActivity) requireActivity()).logout();
        }
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mActivityResultLauncher = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.startCountDownTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.cancelCountDownTimer();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mBinding.setSpanCount(getResources().getInteger(R.integer.group_main_span_count));
    }

    @Override
    public void onRefresh() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mBinding != null) {
                    mBinding.srlGroup.setRefreshing(false);
                    mViewModel.refresh();
                }
            }
        }, 1700);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setCheckable(false);
        switch (item.getItemId()) {
            case R.id.navigation_find:
                mActivityResultLauncher.launch(new Intent(getContext(), FindGroupActivity.class));
                return true;
            case R.id.navigation_request:
                startActivity(new Intent(getContext(), RequestActivity.class));
                return true;
            case R.id.navigation_create:
                mActivityResultLauncher.launch(new Intent(getContext(), CreateGroupActivity.class));
                return true;
        }
        return false;
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(getViewLifecycleOwner(), new Observer<List<Map.Entry<String, Object>>>() {
            @Override
            public void onChanged(List<Map.Entry<String, Object>> groupItemList) {
                mAdapter.submitList(groupItemList);
            }
        });
        mViewModel.getPopularItemList().observe(getViewLifecycleOwner(), new Observer<List<GroupItem>>() {
            @Override
            public void onChanged(List<GroupItem> groupItemList) {
                mAdapter.submitPopularGroupList(groupItemList);
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
        mViewModel.getTick().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                mAdapter.moveSliderPager();
            }
        });
    }
}
