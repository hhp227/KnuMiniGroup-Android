package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.MemberItem;

import java.util.List;

public class MemberGridAdapter extends BaseAdapter {
    private Activity activity;
    private ImageView profileImg;
    private LayoutInflater inflater;
    private List<MemberItem> memberItems;
    private TextView name;

    public MemberGridAdapter(Activity activity, List<MemberItem> memberItems) {
        this.activity = activity;
        this.memberItems = memberItems;
    }

    @Override
    public int getCount() {
        return memberItems.size();
    }

    @Override
    public Object getItem(int position) {
        return memberItems.get(position);
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
            convertView = inflater.inflate(R.layout.member_item, null);

        name = convertView.findViewById(R.id.tv_name);
        profileImg = convertView.findViewById(R.id.iv_profile_image);
        MemberItem memberItem = memberItems.get(position);

        name.setText(memberItem.name);
        profileImg.setImageBitmap(memberItem.profileImg);
        return convertView;
    }
}
