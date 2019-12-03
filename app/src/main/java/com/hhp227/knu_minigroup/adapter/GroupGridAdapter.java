package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.GroupItem;

import java.util.List;

public class GroupGridAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<GroupItem> groupItems;
    private TextView groupName;

    public GroupGridAdapter(Context context, List<GroupItem> groupItems) {
        this.context = context;
        this.groupItems = groupItems;
    }

    @Override
    public int getCount() {
        return groupItems.size();
    }

    @Override
    public Object getItem(int position) {
        return groupItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (layoutInflater == null)
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = layoutInflater.inflate(R.layout.group_grid_item, null);

        groupName = convertView.findViewById(R.id.tv_group_name);
        ImageView groupImage = convertView.findViewById(R.id.iv_group_image);
        GroupItem groupItem = groupItems.get(position);

        groupName.setText(groupItem.getName());
        Glide.with(context).load(groupItem.getImage()).crossFade(150).into(groupImage);

        return convertView;
    }
}
