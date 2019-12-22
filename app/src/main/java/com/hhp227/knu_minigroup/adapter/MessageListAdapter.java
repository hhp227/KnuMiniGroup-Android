package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LayoutInflater inflater;
    private List<MessageItem> messageItems;
    private String imageId;
    private ViewHoler viewHoler;

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
        if (convertView == null) {
            convertView = inflater.inflate(getItemViewType(position) == MSG_TYPE_RIGHT ? R.layout.message_item_right : R.layout.message_item_left, null);
            viewHoler = new ViewHoler(convertView);
            convertView.setTag(viewHoler);
        } else
            viewHoler = (ViewHoler) convertView.getTag();
        MessageItem messageItem = messageItems.get(position);
        viewHoler.name.setText(messageItem.getName());
        viewHoler.message.setText(messageItem.getMessage());
        viewHoler.timeStamp.setText(getTimeStamp(messageItem.getTime()));
        if (position > 0 && getTimeStamp(messageItems.get(position - 1).getTime()).equals(getTimeStamp(messageItem.getTime())) && messageItems.get(position - 1).getFrom().equals(messageItem.getFrom())) {
            viewHoler.name.setVisibility(View.GONE);
            viewHoler.messageBox.setPadding(viewHoler.messageBox.getPaddingLeft(), 0, viewHoler.messageBox.getPaddingRight(), viewHoler.messageBox.getPaddingBottom());
            viewHoler.profileImage.setVisibility(View.INVISIBLE);
        } else {
            viewHoler.name.setVisibility(getItemViewType(position) == MSG_TYPE_RIGHT ? View.GONE : View.VISIBLE);
            viewHoler.profileImage.setVisibility(View.VISIBLE);
            viewHoler.messageBox.setPadding(10, 10, 10, 10);
            Glide.with(context).load(EndPoint.USER_IMAGE.replace("{UID}", messageItem.getFrom())).apply(new RequestOptions().circleCrop()).into(viewHoler.profileImage);
        }
        if (position + 1 != messageItems.size() && getTimeStamp(messageItem.getTime()).equals(getTimeStamp(messageItems.get(position + 1).getTime())) && messageItem.getFrom().equals(messageItems.get(position + 1).getFrom()))
            viewHoler.timeStamp.setText("");

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return !messageItems.get(position).getFrom().equals(imageId) ? MSG_TYPE_LEFT : MSG_TYPE_RIGHT;
    }

    public String getTimeStamp(long time) {
        return new SimpleDateFormat("a h:mm", Locale.getDefault()).format(time);
    }

    public class ViewHoler {
        public ImageView profileImage;
        public LinearLayout messageBox;
        public TextView name, message, timeStamp;

        public ViewHoler(View itemView) {
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            messageBox = itemView.findViewById(R.id.ll_message);
            name = itemView.findViewById(R.id.tv_name);
            message = itemView.findViewById(R.id.tv_message);
            timeStamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
