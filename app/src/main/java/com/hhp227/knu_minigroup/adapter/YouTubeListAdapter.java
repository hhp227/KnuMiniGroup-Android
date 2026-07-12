package com.hhp227.knu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.knu_minigroup.databinding.YoutubeItemBinding;
import com.hhp227.knu_minigroup.dto.YouTubeItem;

import java.util.ArrayList;
import java.util.List;

public class YouTubeListAdapter extends RecyclerView.Adapter<YouTubeListAdapter.YouTubeListHolder> {
    private final List<YouTubeItem> mYouTubeItemList = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;

    @NonNull
    @Override
    public YouTubeListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new YouTubeListHolder(YoutubeItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(YouTubeListHolder holder, final int position) {
        holder.bind(mYouTubeItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mYouTubeItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public List<YouTubeItem> getCurrentList() {
        return mYouTubeItemList;
    }

    public void submitList(List<YouTubeItem> youTubeItems) {
        mYouTubeItemList.clear();
        mYouTubeItemList.addAll(youTubeItems);
        notifyDataSetChanged();
    }

    public class YouTubeListHolder extends RecyclerView.ViewHolder {
        private final YoutubeItemBinding mBinding;

        public YouTubeListHolder(YoutubeItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }

        private void bind(YouTubeItem youTubeItem) {
            mBinding.setYouTubeItem(youTubeItem);
            mBinding.executePendingBindings();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
