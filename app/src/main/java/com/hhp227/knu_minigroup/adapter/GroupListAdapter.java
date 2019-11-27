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

public class GroupListAdapter extends BaseAdapter {
    private static final int NAME_MAX_LINE = 2;
    private Context context;
    private LayoutInflater layoutInflater;
    private List<GroupItem> groupItems;
    private TextView groupName, groupInfo;
    private ImageLoader imageLoader;

    public GroupListAdapter(Context context, List<GroupItem> groupItems) {
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
            convertView = layoutInflater.inflate(R.layout.group_list_item, null);
        if (imageLoader == null)
            imageLoader = app.AppController.getInstance().getImageLoader();

        groupName = convertView.findViewById(R.id.tv_group_name);
        groupInfo = convertView.findViewById(R.id.tv_info);
        NetworkImageView GroupImage = convertView.findViewById(R.id.niv_group_image);
        ImageView nullGroupImage = convertView.findViewById(R.id.null_group_image);
        GroupItem groupItem = groupItems.get(position);

        groupName.setText(groupItem.getName());
        groupName.setMaxLines(NAME_MAX_LINE);
        groupInfo.setText(groupItem.getInfo());

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
