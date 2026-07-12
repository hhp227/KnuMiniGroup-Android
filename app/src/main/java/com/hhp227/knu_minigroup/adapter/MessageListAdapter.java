package com.hhp227.knu_minigroup.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.MessageItemLeftBinding;
import com.hhp227.knu_minigroup.databinding.MessageItemRightBinding;
import com.hhp227.knu_minigroup.dto.MessageItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageListAdapter extends BaseAdapter {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private final List<MessageItem> mMessageItems;

    private final String mUid;

    public MessageListAdapter(List<MessageItem> messageItems, String uid) {
        this.mMessageItems = messageItems;
        this.mUid = uid;
    }

    public void submitList(List<MessageItem> messageItems) {
        mMessageItems.clear();
        if (messageItems != null) {
            mMessageItems.addAll(messageItems);
        }
        notifyDataSetChanged();
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
        switch (getItemViewType(position)) {
            case MSG_TYPE_LEFT:
                MessageListLeftHolder leftHolder;

                if (convertView == null) {
                    MessageItemLeftBinding leftBinding = MessageItemLeftBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                    convertView = leftBinding.getRoot();
                    leftHolder = new MessageListLeftHolder(leftBinding);

                    convertView.setTag(leftHolder);
                } else
                    leftHolder = (MessageListLeftHolder) convertView.getTag();
                leftHolder.bind(mMessageItems.get(position), position);
                break;
            case MSG_TYPE_RIGHT:
                MessageListRightHolder rightHolder;

                if (convertView == null) {
                    MessageItemRightBinding rightBinding = MessageItemRightBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                    convertView = rightBinding.getRoot();
                    rightHolder = new MessageListRightHolder(rightBinding);

                    convertView.setTag(rightHolder);
                } else
                    rightHolder = (MessageListRightHolder) convertView.getTag();
                rightHolder.bind(mMessageItems.get(position), position);
                break;
        }
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

    /*이전 메시지와 같은 시간, 같은 사람이 보낸 메시지인지 판별*/
    private boolean isContinuation(MessageItem messageItem, int position) {
        return position > 0
                && getTimeStamp(mMessageItems.get(position - 1).getTimestamp()).equals(getTimeStamp(messageItem.getTimestamp()))
                && mMessageItems.get(position - 1).getFrom().equals(messageItem.getFrom());
    }

    /*다음 메시지와 같은 시간, 같은 사람이 보낸 메시지이면 타임스탬프를 숨김*/
    private boolean hideTimestamp(MessageItem messageItem, int position) {
        return position + 1 != mMessageItems.size()
                && getTimeStamp(messageItem.getTimestamp()).equals(getTimeStamp(mMessageItems.get(position + 1).getTimestamp()))
                && messageItem.getFrom().equals(mMessageItems.get(position + 1).getFrom());
    }

    private static String getTimeStamp(long time) {
        return new SimpleDateFormat("a h:mm", Locale.getDefault()).format(time);
    }

    private class MessageListLeftHolder {
        private final MessageItemLeftBinding mBinding;

        public MessageListLeftHolder(MessageItemLeftBinding binding) {
            this.mBinding = binding;
        }

        public void bind(MessageItem messageItem, int position) {
            mBinding.setMessageItem(messageItem);
            mBinding.setIsContinuation(isContinuation(messageItem, position));
            mBinding.setTimestamp(hideTimestamp(messageItem, position) ? "" : getTimeStamp(messageItem.getTimestamp()));
            mBinding.setCookie(AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
            mBinding.executePendingBindings();
        }
    }

    private class MessageListRightHolder {
        private final MessageItemRightBinding mBinding;

        public MessageListRightHolder(MessageItemRightBinding binding) {
            this.mBinding = binding;
        }

        public void bind(MessageItem messageItem, int position) {
            mBinding.setMessageItem(messageItem);
            mBinding.setIsContinuation(isContinuation(messageItem, position));
            mBinding.setTimestamp(hideTimestamp(messageItem, position) ? "" : getTimeStamp(messageItem.getTimestamp()));
            mBinding.executePendingBindings();
        }
    }
}
