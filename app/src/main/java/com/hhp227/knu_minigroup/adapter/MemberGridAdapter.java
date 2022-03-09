package com.hhp227.knu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.MemberItemBinding;
import com.hhp227.knu_minigroup.dto.MemberItem;

import java.util.List;

public class MemberGridAdapter extends RecyclerView.Adapter<MemberGridAdapter.MemberGridHolder> {
    private final List<MemberItem> mMemberItemList;

    private OnItemClickListener mOnItemClickListener;

    public MemberGridAdapter(List<MemberItem> memberItemList) {
        this.mMemberItemList = memberItemList;
    }

    @NonNull
    @Override
    public MemberGridHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MemberGridHolder(MemberItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(MemberGridHolder holder, final int position) {
        holder.bind(mMemberItemList.get(position));
    }

    @Override
    public int getItemCount() {
        return mMemberItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public class MemberGridHolder extends RecyclerView.ViewHolder {
        private final MemberItemBinding mBinding;

        public MemberGridHolder(MemberItemBinding binding) {
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

        private void bind(MemberItem memberItem) {
            mBinding.tvName.setText(memberItem.name);
            Glide.with(itemView.getContext())
                    .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", memberItem.uid), new LazyHeaders.Builder()
                            .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN))
                            .build()))
                    .apply(new RequestOptions().centerCrop()
                            .error(R.drawable.user_image_view)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(mBinding.ivProfileImage);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}