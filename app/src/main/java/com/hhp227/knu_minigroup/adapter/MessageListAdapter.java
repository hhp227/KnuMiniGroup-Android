package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.MessageItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageListAdapter extends BaseAdapter {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private ImageView profileImage;
    private LayoutInflater inflater;
    private List<MessageItem> messageItems;
    private String imageId;
    private TextView name, message, timeStamp;

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
        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(getItemViewType(position) == MSG_TYPE_RIGHT ? R.layout.message_item_right : R.layout.message_item_left, null);
        profileImage = convertView.findViewById(R.id.iv_profile_image);
        name = convertView.findViewById(R.id.tv_name);
        message = convertView.findViewById(R.id.tv_message);
        timeStamp = convertView.findViewById(R.id.tv_timestamp);

        MessageItem messageItem = messageItems.get(position);
        Glide.with(context).load(EndPoint.USER_IMAGE.replace("{IMAGE_ID}", messageItem.getFrom())).apply(new RequestOptions().circleCrop()).into(profileImage);
        name.setText(messageItem.getName());
        message.setText(messageItem.getMessage());
        timeStamp.setText(new SimpleDateFormat("a h:mm", Locale.getDefault()).format(messageItem.getTime()));

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return !messageItems.get(position).getFrom().equals(imageId) ? MSG_TYPE_LEFT : MSG_TYPE_RIGHT;
    }
}
