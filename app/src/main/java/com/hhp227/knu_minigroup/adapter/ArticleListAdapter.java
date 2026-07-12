package com.hhp227.knu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ArticleItemBinding;
import com.hhp227.knu_minigroup.databinding.LoadMoreBinding;
import com.hhp227.knu_minigroup.dto.ArticleItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArticleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ARTICLE = 0;
    private static final int TYPE_LOADER = 1;

    private final List<Map.Entry<String, ArticleItem>> mArticleItemList = new ArrayList<>(Collections.singletonList(null));

    private int mProgressBarVisibility;

    private OnItemClickListener mOnItemClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ARTICLE:
                return new ItemHolder(ArticleItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_LOADER:
                return new FooterHolder(LoadMoreBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind(mArticleItemList.get(position).getValue());
        } else if (holder instanceof FooterHolder)
            ((FooterHolder) holder).bind(mProgressBarVisibility);
    }

    @Override
    public int getItemCount() {
        return mArticleItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mArticleItemList.get(position) != null ? TYPE_ARTICLE : TYPE_LOADER;
    }

    public void setFooterProgressBarVisibility(int visibility) {
        this.mProgressBarVisibility = visibility;

        notifyItemChanged(getItemCount() - 1);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public String getKey(int position) {
        return mArticleItemList.get(position).getKey();
    }

    public List<Map.Entry<String, ArticleItem>> getCurrentList() {
        return mArticleItemList;
    }

    public void submitList(List<Map.Entry<String, ArticleItem>> articleItemList) {
        mArticleItemList.clear();
        mArticleItemList.add(null);
        mArticleItemList.addAll(mArticleItemList.size() - 1, articleItemList);
        notifyDataSetChanged();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private final ArticleItemBinding mBinding;

        ItemHolder(ArticleItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.cvArticle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            mBinding.llReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }

        private void bind(final ArticleItem articleItem) {
            mBinding.setArticleItem(articleItem);
            mBinding.setCookie(AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
            mBinding.executePendingBindings();
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        private final LoadMoreBinding mBinding;

        FooterHolder(LoadMoreBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        private void bind(int progressBarVisibility) {
            mBinding.pbMore.setVisibility(progressBarVisibility);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
