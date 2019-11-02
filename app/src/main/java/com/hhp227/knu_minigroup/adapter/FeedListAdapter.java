package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.FeedItem;

import java.util.List;

public class FeedListAdapter extends BaseAdapter {
    public static boolean LIKED;
    private Activity activity;
    private ImageLoader imageLoader;
    private ImageView favorite;
    private LayoutInflater inflater;
    private LinearLayout replyButton, likeButton;
    private List<FeedItem> feedItems;
    private TextView name, timestamp, content, contentMore, replyCount, likeCount;

    public FeedListAdapter(Activity activity, List<FeedItem> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
    }

    @Override
    public int getCount() {
        return feedItems.size();
    }

    @Override
    public Object getItem(int position) {
        return feedItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_item, null);

        if (imageLoader == null)
            imageLoader = app.AppController.getInstance().getImageLoader();

        name = convertView.findViewById(R.id.tv_name);

        FeedItem feedItem = feedItems.get(position);

        name.setText(feedItem.getName());

        return convertView;
    }
}
