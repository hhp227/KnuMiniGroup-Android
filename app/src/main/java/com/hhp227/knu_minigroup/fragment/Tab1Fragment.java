package com.hhp227.knu_minigroup.fragment;

import static com.hhp227.knu_minigroup.viewmodel.Tab1ViewModel.mGroupId;
import static com.hhp227.knu_minigroup.viewmodel.Tab1ViewModel.mGroupImage;
import static com.hhp227.knu_minigroup.viewmodel.Tab1ViewModel.mGroupName;
import static com.hhp227.knu_minigroup.viewmodel.Tab1ViewModel.mIsAdmin;
import static com.hhp227.knu_minigroup.viewmodel.Tab1ViewModel.mKey;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.activity.ArticleActivity;
import com.hhp227.knu_minigroup.activity.CreateArticleActivity;
import com.hhp227.knu_minigroup.adapter.ArticleListAdapter;
import com.hhp227.knu_minigroup.databinding.FragmentTab1Binding;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.handler.OnFragmentTab1EventListener;
import com.hhp227.knu_minigroup.viewmodel.Tab1ViewModel;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class Tab1Fragment extends Fragment implements OnFragmentTab1EventListener {
    private long mLastClickTime;

    private ArticleListAdapter mAdapter;

    private FragmentTab1Binding mBinding;

    private ActivityResultLauncher<Intent> mArticleActivityResultLauncher;

    private Tab1ViewModel mViewModel;

    public static Tab1Fragment newInstance(boolean isAdmin, String grpId, String grpNm, String grpImg, String key) {
        Tab1Fragment fragment = new Tab1Fragment();
        Bundle args = new Bundle();

        args.putBoolean("admin", isAdmin);
        args.putString("grp_id", grpId);
        args.putString("grp_nm", grpNm);
        args.putString("grp_img", grpImg);
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab1Binding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(Tab1ViewModel.class);
        mAdapter = new ArticleListAdapter();
        mArticleActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                /*for (Fragment fragment : getParentFragmentManager().getFragments()) {
                    if (fragment instanceof Tab1Fragment) {
                        ((Tab1Fragment) fragment).onArticleActivityResult(result);
                    }
                }*/
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        int position = result.getData().getIntExtra("position", 0) - 1;
                        Map.Entry<String, ArticleItem> entry = mAdapter.getCurrentList().get(position);
                        String key = entry.getKey();
                        ArticleItem articleItem = entry.getValue();

                        articleItem.setTitle(result.getData().getStringExtra("sbjt"));
                        articleItem.setContent(result.getData().getStringExtra("txt"));
                        articleItem.setImages(result.getData().getStringArrayListExtra("img")); // firebase data
                        articleItem.setReplyCount(result.getData().getStringExtra("cmmt_cnt"));
                        articleItem.setYoutube(result.getData().getParcelableExtra("youtube"));
                        mViewModel.updateArticleItem(position, new AbstractMap.SimpleEntry<>(key, articleItem));
                    } else {
                        mViewModel.refresh();
                        mBinding.rvArticle.scrollToPosition(0);
                        ((TabHostLayoutFragment) requireParentFragment()).appbarLayoutExpand();
                    }
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
        mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
        mBinding.rvArticle.setAdapter(mAdapter);
        mBinding.rvArticle.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                Handler handler = new Handler(Looper.getMainLooper());
                RecyclerView.OnScrollListener onScrollListener = this;

                if (dy > 0 && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() >= layoutManager.getItemCount() - 1) {
                    recyclerView.removeOnScrollListener(onScrollListener);
                    mViewModel.fetchNextPage();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.addOnScrollListener(onScrollListener);
                        }
                    }, 1000);
                }
            }
        });
        mAdapter.setOnItemClickListener(new ArticleListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                ArticleItem articleItem = mAdapter.getCurrentList().get(position).getValue();
                Intent intent = new Intent(getContext(), ArticleActivity.class);

                intent.putExtra("admin", mIsAdmin);
                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("grp_nm", mGroupName);
                intent.putExtra("grp_img", mGroupImage);
                intent.putExtra("position", position + 1);
                intent.putExtra("auth", articleItem.isAuth() || mViewModel.getUser().getUid().equals(articleItem.getUid()));
                intent.putExtra("isbottom", v.getId() == R.id.ll_reply);
                intent.putExtra("grp_key", mKey);
                intent.putExtra("artl_key", mAdapter.getKey(position));
                mArticleActivityResultLauncher.launch(intent);
            }
        });
        mBinding.srlArticleList.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright);
        observeViewModelData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        mArticleActivityResultLauncher = null;
    }

    @Override
    public void onEmptyViewClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
            return;
        mLastClickTime = SystemClock.elapsedRealtime();
        Intent intent = new Intent(getActivity(), CreateArticleActivity.class);

        intent.putExtra("grp_id", mGroupId);
        intent.putExtra("grp_nm", mGroupName);
        intent.putExtra("grp_img", mGroupImage);
        intent.putExtra("grp_key", mKey);
        intent.putExtra("type", 0);
        ((TabHostLayoutFragment) requireParentFragment()).mCreateArticleResultLauncher.launch(intent);
    }

    @Override
    public void onRefresh() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewModel.refresh();
                mBinding.srlArticleList.setRefreshing(false);
            }
        }, 1000);
    }

    public void onCreateArticleActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mViewModel.refresh();
            mBinding.rvArticle.scrollToPosition(0);
            ((TabHostLayoutFragment) requireParentFragment()).appbarLayoutExpand();
        }
    }

    public void onProfileActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void observeViewModelData() {
        mViewModel.getItemList().observe(getViewLifecycleOwner(), new Observer<List<Map.Entry<String, ArticleItem>>>() {
            @Override
            public void onChanged(List<Map.Entry<String, ArticleItem>> articleItemList) {
                mAdapter.submitList(articleItemList);
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
                    Snackbar.make(mBinding.rvArticle, message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
