package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.bumptech.glide.Glide;
import com.hhp227.knu_minigroup.databinding.WriteContentBinding;
import com.hhp227.knu_minigroup.dto.YouTubeItem;

import java.util.List;

public class WriteListAdapter extends ArrayAdapter<Object> {
    public WriteListAdapter(Context context, int resource, List<Object> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            WriteContentBinding binding = WriteContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            convertView = binding.getRoot();
            viewHolder = new ViewHolder(binding);

            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();
        if (getItem(position) instanceof Bitmap) {
            viewHolder.bind((Bitmap) getItem(position));
        } else if (getItem(position) instanceof String) {
            viewHolder.bind((String) getItem(position));
        } else if (getItem(position) instanceof YouTubeItem) { // 수정
            viewHolder.bind((YouTubeItem) getItem(position));
        }
        return convertView;
    }

    private static class ViewHolder {
        private final WriteContentBinding mBinding;

        ViewHolder(WriteContentBinding binding) {
            this.mBinding = binding;
        }

        public void bind(Bitmap bitmap) {
            mBinding.ivImagePreview.setVisibility(bitmap != null ? View.VISIBLE : View.GONE);
            Glide.with(mBinding.getRoot().getContext()).load(bitmap).into(mBinding.ivImagePreview);
            mBinding.ivVideoPreview.setVisibility(View.GONE);
        }

        public void bind(String imageUrl) {
            mBinding.ivImagePreview.setVisibility(imageUrl != null ? View.VISIBLE : View.GONE);
            Glide.with(mBinding.getRoot().getContext()).load(imageUrl).into(mBinding.ivImagePreview);
            mBinding.ivVideoPreview.setVisibility(View.GONE);
        }

        public void bind(YouTubeItem youTubeItem) {
            mBinding.ivVideoPreview.setVisibility(View.VISIBLE);
            Glide.with(mBinding.getRoot().getContext()).load(youTubeItem.thumbnail).into(mBinding.ivImagePreview);
        }
    }
}
