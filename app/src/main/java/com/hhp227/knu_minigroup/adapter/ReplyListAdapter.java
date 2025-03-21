package com.hhp227.knu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ReplyItemBinding;
import com.hhp227.knu_minigroup.dto.ReplyItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReplyListAdapter extends BaseAdapter {
    private final List<Map.Entry<String, ReplyItem>> mReplyItemList = new ArrayList<>();

    @Override
    public int getCount() {
        return mReplyItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mReplyItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            ReplyItemBinding binding = ReplyItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            convertView = binding.getRoot();
            viewHolder = new ViewHolder(binding);

            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.bind(mReplyItemList.get(position).getValue());
        return convertView;
    }

    public String getKey(int position) {
        return mReplyItemList.get(position).getKey();
    }

    public List<Map.Entry<String, ReplyItem>> getCurrentList() {
        return mReplyItemList;
    }

    public void submitList(List<Map.Entry<String, ReplyItem>> replyItemList) {
        mReplyItemList.clear();
        mReplyItemList.addAll(replyItemList);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        private final ReplyItemBinding mBinding;

        ViewHolder(ReplyItemBinding binding) {
            this.mBinding = binding;
        }

        public void bind(ReplyItem replyItem) {
            mBinding.setCookie(AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
            mBinding.setReplyItem(replyItem);
            mBinding.executePendingBindings();
        }
    }
}
