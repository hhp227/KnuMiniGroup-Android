package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.BbsItem;

import java.util.ArrayList;
import java.util.List;

public class BbsListAdapter extends BaseAdapter {
    private Activity mActivity;
    private LayoutInflater mInflater;
    private List<BbsItem> mListData;

    public BbsListAdapter(Activity activity, ArrayList<BbsItem> ListData) {
        this.mActivity = activity;
        this.mListData = ListData;
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
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
            convertView = mInflater.inflate(R.layout.bbs_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        BbsItem Data = mListData.get(position);

        //"공지" 의 색깔을 부분적으로 약간 진하게 수정.
        viewHolder.title.setText(Data.getType().equals("공지") ? Html.fromHtml("<font color=#616161>[공지] </font>" + Data.getTitle()) : Html.fromHtml("" + Data.getTitle()));
        viewHolder.writer.setText(Data.getWriter());
        viewHolder.date.setText(Data.getDate());

        return convertView;
    }

    private static class ViewHolder {
        private TextView title, writer, date;

        ViewHolder(View itemView) {
            title = itemView.findViewById(R.id.item_title);
            writer = itemView.findViewById(R.id.item_writer);
            date = itemView.findViewById(R.id.item_date);
        }
    }
}
