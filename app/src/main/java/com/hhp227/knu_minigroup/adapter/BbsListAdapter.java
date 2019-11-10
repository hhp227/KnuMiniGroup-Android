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
    private List<BbsItem> ListData;
    private Activity activity;
    private LayoutInflater inflater;

    public BbsListAdapter(Activity activity, ArrayList<BbsItem> ListData) {
        this.activity = activity;
        this.ListData = ListData;
    }

    @Override
    public int getCount() {
        return ListData.size();
    }

    @Override
    public Object getItem(int position) {
        return ListData.get(position);
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
            convertView = inflater.inflate(R.layout.bbs_item, null);

        TextView Title = convertView.findViewById(R.id.item_title);
        TextView Writer = convertView.findViewById(R.id.item_writer);
        TextView Date = convertView.findViewById(R.id.item_date);

        BbsItem Data = ListData.get(position);

        //"공지" 의 색깔을 부분적으로 약간 진하게 수정.
        Title.setText(Data.getType().equals("공지") ? Html.fromHtml("<font color=#616161>[공지] </font>" + Data.getTitle()) : Html.fromHtml("" + Data.getTitle()));
        Writer.setText(Data.getWriter());
        Date.setText(Data.getDate());

        return convertView;
    }
}
