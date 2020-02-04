package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.YouTubeItem;

import java.util.List;

public class YouTubeListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<YouTubeItem> mYouTubeItemList;

    public YouTubeListAdapter(Context mContext, List<YouTubeItem> mYouTubeItemList) {
        this.mContext = mContext;
        this.mYouTubeItemList = mYouTubeItemList;
    }

    @Override
    public int getCount() {
        return mYouTubeItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mYouTubeItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (mInflater == null)
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.youtube_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        YouTubeItem youTubeItem = mYouTubeItemList.get(position);
        Glide.with(mContext)
                .load(youTubeItem.thumbnail)
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(viewHolder.image);
        viewHolder.title.setText(youTubeItem.title);
        viewHolder.channelTitle.setText(youTubeItem.channelTitle);

        return convertView;
    }

    public static class ViewHolder {
        private ImageView image;
        private TextView title, channelTitle;

        ViewHolder(View itemView) {
            image = itemView.findViewById(R.id.iv_youtube);
            title = itemView.findViewById(R.id.tv_title);
            channelTitle = itemView.findViewById(R.id.tv_channel_title);
        }
    }
}
