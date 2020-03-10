package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.SeatItem;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SeatListAdapter extends BaseAdapter {
    private Activity mActivity;
    private LayoutInflater mInflater;
    private List<SeatItem> mSeatItemList;

    public SeatListAdapter(Activity activity, List<SeatItem> seatItems) {
        this.mActivity = activity;
        this.mSeatItemList = seatItems;
    }

    @Override
    public int getCount() {
        return mSeatItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSeatItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (mInflater == null)
            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.seat_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        SeatItem seatItem = mSeatItemList.get(position);
        viewHolder.name.setText(seatItem.name);
        viewHolder.text.setText(seatItem.disable == null ? "사용중 좌석" : seatItem.disable[0]);
        viewHolder.status.setText(seatItem.disable == null ?
                new StringBuilder().append("[").append(seatItem.occupied).append("/").append(seatItem.activeTotal).append("]").toString() :
                new StringBuilder().append(getPeriodTimeGenerator(mActivity, seatItem.disable[1])).append(" ~ ").append(getPeriodTimeGenerator(mActivity, seatItem.disable[2])).toString());

        return convertView;
    }

    // 타임 제네레이터
    private static String getPeriodTimeGenerator(Context context, String strDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getDefault());
        Date date = null;

        if (TextUtils.isEmpty(strDate))
            return "";

        try {
            date = df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(context.getResources().getString(R.string.format_date2));
        return sdf.format(date);
    }

    private static class ViewHolder {
        private TextView name, text, status;

        ViewHolder(View itemView) {
            name = itemView.findViewById(R.id.name);
            text = itemView.findViewById(R.id.text);
            status = itemView.findViewById(R.id.seat);
        }
    }
}
