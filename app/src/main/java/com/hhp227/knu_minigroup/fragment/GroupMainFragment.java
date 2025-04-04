package com.hhp227.knu_minigroup.fragment;

import static com.hhp227.knu_minigroup.adapter.GroupGridAdapter.TYPE_AD;
import static com.hhp227.knu_minigroup.adapter.GroupGridAdapter.TYPE_GROUP;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationBarView;
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
import com.hhp227.knu_minigroup.viewmodel.GroupMainViewModel;

import java.util.List;
import java.util.Map;

// TODO
public class GroupMainFragment extends Fragment {
    private static final int PORTRAIT_SPAN_COUNT = 2;

    private static final int LANDSCAPE_SPAN_COUNT = 4;

    private int mSpanCount;

    private GridLayoutManager mGridLayoutManager;

    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup;

    private GroupGridAdapter mAdapter;

    private RecyclerView.ItemDecoration mItemDecoration;

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
        mSpanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? PORTRAIT_SPAN_COUNT :
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? LANDSCAPE_SPAN_COUNT :
                        0;
        mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_TEXT
                        || mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_BANNER
                        || mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_VIEW_PAGER ? mSpanCount : 1;
            }
        };
        mGridLayoutManager = new GridLayoutManager(getContext(), mSpanCount);
        mItemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getAdapter() != null && parent.getAdapter().getItemViewType(parent.getChildAdapterPosition(view)) == TYPE_GROUP || parent.getAdapter().getItemViewType(parent.getChildAdapterPosition(view)) == TYPE_AD) {
                    outRect.top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    outRect.bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                    if (parent.getChildAdapterPosition(view) % mSpanCount == 0) {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
                    } else if (parent.getChildAdapterPosition(view) % mSpanCount == 1) {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                    } else {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                    }
                }
            }
        };
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
        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.main));
        mAdapter.setHasStableIds(true);
        mAdapter.setOnItemClickListener(new GroupGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                if (mAdapter.getCurrentList().get(position).getValue() instanceof GroupItem) {
                    GroupItem groupItem = (GroupItem) mAdapter.getCurrentList().get(position).getValue();
                    Intent intent = new Intent(getContext(), GroupActivity.class);

                    intent.putExtra("admin", groupItem.getAuthorUid().equals(mViewModel.getUser().getUid()));
                    intent.putExtra("grp_id", groupItem.getId());
                    intent.putExtra("grp_nm", groupItem.getName());
                    intent.putExtra("grp_img", groupItem.getImage()); // 경북대 소모임에는 없음
                    intent.putExtra("key", mAdapter.getKey(position));
                    mActivityResultLauncher.launch(intent);
                }
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
        mGridLayoutManager.setSpanSizeLookup(mSpanSizeLookup);
        mBinding.rvGroup.setLayoutManager(mGridLayoutManager);
        mBinding.rvGroup.setAdapter(mAdapter);
        mBinding.rvGroup.addItemDecoration(mItemDecoration);
        mBinding.srlGroup.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBinding.srlGroup.setRefreshing(false);
                        mViewModel.refresh();
                    }
                }, 1700);
            }
        });
        mBinding.srlGroup.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mBinding.bnvGroupButton.getMenu().getItem(0).setCheckable(false);
        mBinding.bnvGroupButton.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
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
        });
        if (mViewModel.getUser() == null) {
            ((MainActivity) requireActivity()).logout();
        }
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.rvGroup.removeItemDecoration(mItemDecoration);
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
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                mSpanCount = PORTRAIT_SPAN_COUNT;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mSpanCount = LANDSCAPE_SPAN_COUNT;
                break;
        }
        mGridLayoutManager.setSpanSizeLookup(mSpanSizeLookup);
        mGridLayoutManager.setSpanCount(mSpanCount);
        mBinding.rvGroup.invalidateItemDecorations();
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(getViewLifecycleOwner(), new Observer<List<Map.Entry<String, Object>>>() {
            @Override
            public void onChanged(List<Map.Entry<String, Object>> groupItemList) {
                mAdapter.submitList(groupItemList);
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