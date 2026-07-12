package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import com.hhp227.knu_minigroup.databinding.WriteContentBinding;

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
        viewHolder.bind(getItem(position));
        return convertView;
    }

    public void submitList(List<Object> contentList) {
        clear();
        addAll(contentList);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        private final WriteContentBinding mBinding;

        ViewHolder(WriteContentBinding binding) {
            this.mBinding = binding;
        }

        public void bind(Object content) {
            mBinding.setContent(content);
            mBinding.executePendingBindings();
        }
    }
}
