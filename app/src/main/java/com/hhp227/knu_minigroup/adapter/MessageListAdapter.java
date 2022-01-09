package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.MessageItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

//TODO viewBinding으로 이전할 것

public class MessageListAdapter extends BaseAdapter {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private final List<MessageItem> mMessageItems;

    private final String mUid;

    private LayoutInflater mInflater;

    public MessageListAdapter(List<MessageItem> messageItems, String uid) {
        this.mMessageItems = messageItems;
        this.mUid = uid;
    }

    @Override
    public int getCount() {
        return mMessageItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (mInflater == null)
            mInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(getItemViewType(position) == MSG_TYPE_RIGHT ? R.layout.message_item_right : R.layout.message_item_left, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();
        MessageItem messageItem = mMessageItems.get(position);
        viewHolder.name.setText(messageItem.getName());
        viewHolder.message.setText(messageItem.getMessage());
        viewHolder.timeStamp.setText(getTimeStamp(messageItem.getTimeStamp()));
        if (position > 0 && getTimeStamp(mMessageItems.get(position - 1).getTimeStamp()).equals(getTimeStamp(messageItem.getTimeStamp())) && mMessageItems.get(position - 1).getFrom().equals(messageItem.getFrom())) {
            viewHolder.name.setVisibility(View.GONE);
            viewHolder.messageBox.setPadding(viewHolder.messageBox.getPaddingLeft(), 0, viewHolder.messageBox.getPaddingRight(), viewHolder.messageBox.getPaddingBottom());
            viewHolder.profileImage.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.name.setVisibility(getItemViewType(position) == MSG_TYPE_RIGHT ? View.GONE : View.VISIBLE);
            viewHolder.profileImage.setVisibility(View.VISIBLE);
            viewHolder.messageBox.setPadding(10, 10, 10, 10);
            Glide.with(parent.getContext())
                    .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", messageItem.getFrom()), new LazyHeaders.Builder()
                            .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN))
                            .build()))
                    .apply(new RequestOptions()
                            .circleCrop()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(viewHolder.profileImage);
        }
        if (position + 1 != mMessageItems.size() && getTimeStamp(messageItem.getTimeStamp()).equals(getTimeStamp(mMessageItems.get(position + 1).getTimeStamp())) && messageItem.getFrom().equals(mMessageItems.get(position + 1).getFrom()))
            viewHolder.timeStamp.setText("");

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return !mMessageItems.get(position).getFrom().equals(mUid) ? MSG_TYPE_LEFT : MSG_TYPE_RIGHT;
    }

    private String getTimeStamp(long time) {
        return new SimpleDateFormat("a h:mm", Locale.getDefault()).format(time);
    }

    private static class ViewHolder {
        private final ImageView profileImage;

        private final LinearLayout messageBox;

        private final TextView name, message, timeStamp;

        private ViewHolder(View itemView) {
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            messageBox = itemView.findViewById(R.id.ll_message);
            name = itemView.findViewById(R.id.tv_name);
            message = itemView.findViewById(R.id.tv_message);
            timeStamp = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
