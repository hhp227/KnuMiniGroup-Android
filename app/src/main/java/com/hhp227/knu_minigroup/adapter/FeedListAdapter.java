package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
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
import com.hhp227.knu_minigroup.volley.util.FeedImageView;

import java.util.List;

public class FeedListAdapter extends BaseAdapter {
    public static boolean LIKED;
    private static final int CONTENT_MAX_LINE = 4;
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
        timestamp = convertView.findViewById(R.id.tv_timestamp);
        content = convertView.findViewById(R.id.tv_content);
        contentMore = convertView.findViewById(R.id.tv_content_more);
        FeedImageView feedImageView = convertView.findViewById(R.id.fiv_feed_image);
        replyCount = convertView.findViewById(R.id.tv_replycount);

        FeedItem feedItem = feedItems.get(position);

        name.setText(feedItem.getName());
        timestamp.setText(feedItem.getTimeStamp());
        // 피드의 메시지가 비었는지 확인
        if (!TextUtils.isEmpty(feedItem.getContent())) {
            content.setText(feedItem.getContent());
            content.setMaxLines(CONTENT_MAX_LINE);
            content.setVisibility(View.VISIBLE);
        } else {
            // 피드 내용이 비었으면 화면에서 삭제
            content.setVisibility(View.GONE);
        }
        contentMore.setVisibility(!TextUtils.isEmpty(feedItem.getContent()) && content.getLineCount() > CONTENT_MAX_LINE ? View.VISIBLE : View.GONE);

        // 피드 이미지
        if (feedItem.getImage() != null) {
            feedImageView.setImageUrl(feedItem.getImage(), imageLoader);
            feedImageView.setErrorImageResId(R.drawable.ic_launcher_background);
            feedImageView.setVisibility(View.VISIBLE);
            feedImageView.setResponseObserver(new FeedImageView.ResponseObserver() {
                @Override
                public void onError() {
                }

                @Override
                public void onSuccess() {
                }
            });
        } else
            feedImageView.setVisibility(View.GONE);
        replyCount.setText(feedItem.getReplyCount());

        return convertView;
    }
}
