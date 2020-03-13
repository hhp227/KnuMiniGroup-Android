package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.YouTubeItem;

import java.util.List;

public class WriteListAdapter extends ArrayAdapter<Object> {
    private int resource;
    private Context mContext;
    private LayoutInflater mInflater;

    public WriteListAdapter(Context context, int resource, List<Object> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (mInflater == null)
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(resource, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        if (getItem(position) instanceof Bitmap) {
            Bitmap bitmap = (Bitmap) getItem(position);
            viewHolder.imageView.setVisibility(bitmap != null ? View.VISIBLE : View.GONE);
            Glide.with(mContext).load(bitmap).into(viewHolder.imageView);
            viewHolder.videoMark.setVisibility(View.GONE);
        } else if (getItem(position) instanceof String) {
            String imageUrl = (String) getItem(position);
            viewHolder.imageView.setVisibility(imageUrl != null ? View.VISIBLE : View.GONE);
            Glide.with(mContext).load(imageUrl).into(viewHolder.imageView);
            viewHolder.videoMark.setVisibility(View.GONE);
        } else if (getItem(position) instanceof YouTubeItem) { // 수정
            YouTubeItem youTubeItem = (YouTubeItem) getItem(position);
            viewHolder.videoMark.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(youTubeItem.thumbnail).into(viewHolder.imageView);
        }

        return convertView;
    }

    private static class ViewHolder {
        private ImageView imageView, videoMark;

        ViewHolder(View itemView) {
            imageView = itemView.findViewById(R.id.iv_image_preview);
            videoMark = itemView.findViewById(R.id.iv_video_preview);
        }
    }
}
