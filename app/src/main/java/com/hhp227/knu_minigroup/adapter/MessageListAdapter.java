package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.hhp227.knu_minigroup.dto.MessageItem;

import java.util.List;

public class MessageListAdapter extends BaseAdapter {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<MessageItem> messageItems;
    private String imageId;

    public MessageListAdapter(Context context, List<MessageItem> messageItems, String imageId) {
        this.context = context;
        this.messageItems = messageItems;
        this.imageId = imageId;
    }

    @Override
    public int getCount() {
        return messageItems.size();
    }

    @Override
    public Object getItem(int position) {
        return messageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
