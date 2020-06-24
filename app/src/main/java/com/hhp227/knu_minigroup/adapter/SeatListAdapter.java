package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.SeatItem;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SeatListAdapter extends RecyclerView.Adapter {
    private Activity mActivity;
    private List<SeatItem> mSearItemList;

    public SeatListAdapter(Activity mActivity, List<SeatItem> mSearItemList) {
        this.mActivity = mActivity;
        this.mSearItemList = mSearItemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.seat_item, parent, false);
        return new SeatListHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SeatListHolder) {
            SeatItem seatItem = mSearItemList.get(position);
            ((SeatListHolder) holder).name.setText(seatItem.name);
            ((SeatListHolder) holder).text.setText(seatItem.disable == null ? "사용중 좌석" : seatItem.disable[0]);
            ((SeatListHolder) holder).status.setText(seatItem.disable == null ?
                    new StringBuilder().append("[").append(seatItem.occupied).append("/").append(seatItem.activeTotal).append("]").toString() :
                    new StringBuilder().append(getPeriodTimeGenerator(mActivity, seatItem.disable[1])).append(" ~ ").append(getPeriodTimeGenerator(mActivity, seatItem.disable[2])).toString());
        }
    }

    @Override
    public int getItemCount() {
        return mSearItemList.size();
    }

    public static class SeatListHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView name, text, status;

        SeatListHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            name = itemView.findViewById(R.id.name);
            text = itemView.findViewById(R.id.text);
            status = itemView.findViewById(R.id.seat);
        }
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
}
