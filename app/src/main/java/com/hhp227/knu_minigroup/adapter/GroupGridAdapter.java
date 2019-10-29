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
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.GroupItem;

import java.util.List;

public class GroupGridAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private List<GroupItem> groupItems;
    private TextView groupName;
    private ImageLoader imageLoader;

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
            convertView = layoutInflater.inflate(R.layout.group_item, null);
        if (imageLoader == null)
            imageLoader = app.AppController.getInstance().getImageLoader();

        groupName = convertView.findViewById(R.id.tvGroupName);
        NetworkImageView GroupImage = convertView.findViewById(R.id.nivGroupImage);
        ImageView nullGroupImage = convertView.findViewById(R.id.nullGroupImage);
        GroupItem groupItem = groupItems.get(position);

        groupName.setText(groupItem.getName());

        if (groupItem.getImage() != null) {
            GroupImage.setImageUrl(groupItem.getImage(), imageLoader);
            GroupImage.setVisibility(View.VISIBLE);
            nullGroupImage.setVisibility(View.GONE);
        } else {
            nullGroupImage.setVisibility(View.VISIBLE);
            GroupImage.setVisibility(View.GONE);
        }

        return convertView;
    }
}
