package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.GroupListItemBinding;
import com.hhp227.knu_minigroup.databinding.LoadMoreBinding;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.fragment.GroupInfoFragment;

import java.util.List;
import java.util.Map;

public class GroupListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_GROUP = 0;
    private static final int TYPE_LOADER = 1;
    private static final int NAME_MAX_LINE = 2;

    private final Activity mActivity;

    private final List<Map.Entry<String, GroupItem>> mGroupItemList;

    private int mProgressBarVisibility, mButtonType;

    public GroupListAdapter(Activity activity, List<Map.Entry<String, GroupItem>> groupItemList) {
        this.mActivity = activity;
        this.mGroupItemList = groupItemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_GROUP:
                return new ItemHolder(GroupListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_LOADER:
                return new FooterHolder(LoadMoreBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        throw new RuntimeException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind(mGroupItemList.get(position).getKey(), mGroupItemList.get(position).getValue(), mButtonType, mActivity);
        } else if (holder instanceof FooterHolder) {
            ((FooterHolder) holder).bind(mProgressBarVisibility);
        }
    }

    @Override
    public int getItemCount() {
        return mGroupItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mGroupItemList.get(position) != null ? TYPE_GROUP : TYPE_LOADER;
    }

    public void setFooterProgressBarVisibility(int visibility) {
        this.mProgressBarVisibility = visibility;

        notifyDataSetChanged();
    }

    public void setButtonType(int type) {
        this.mButtonType = type;
    }

    public String getKey(int position) {
        return mGroupItemList.get(position).getKey();
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        private final GroupListItemBinding mBinding;

        public ItemHolder(GroupListItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        private void bind(final String key, final GroupItem groupItem, final int buttonType, final Activity activity) {
            mBinding.tvGroupName.setText(groupItem.getName());
            mBinding.tvGroupName.setMaxLines(NAME_MAX_LINE);
            mBinding.tvInfo.setText(groupItem.getJoinType().equals("0") ? "가입방식: 자동 승인" : "가입방식: 운영자 승인 확인");
            Glide.with(itemView.getContext())
                    .load(groupItem.getImage())
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                    .into(mBinding.ivGroupImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putString("grp_id", groupItem.getId());
                    args.putString("grp_nm", groupItem.getName());
                    args.putString("img", groupItem.getImage());
                    args.putString("info", groupItem.getInfo());
                    args.putString("desc", groupItem.getDescription());
                    args.putString("type", groupItem.getJoinType());
                    args.putInt("btn_type", buttonType);
                    args.putString("key", key);

                    GroupInfoFragment newFragment = GroupInfoFragment.newInstance();
                    newFragment.setArguments(args);
                    newFragment.show(((FragmentActivity) activity).getSupportFragmentManager(), "dialog");
                }
            });
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        private final LoadMoreBinding mBinding;

        public FooterHolder(LoadMoreBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        private void bind(int v) {
            mBinding.pbMore.setVisibility(v);
        }
    }
}
