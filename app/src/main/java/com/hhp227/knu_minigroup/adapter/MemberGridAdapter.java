package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.MemberItem;

import java.util.List;

public class MemberGridAdapter extends BaseAdapter {
    private Activity mActivity;
    private LayoutInflater mInflater;
    private List<MemberItem> mMemberItems;

    public MemberGridAdapter(Activity activity, List<MemberItem> memberItems) {
        this.mActivity = activity;
        this.mMemberItems = memberItems;
    }

    @Override
    public int getCount() {
        return mMemberItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mMemberItems.get(position);
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
            convertView = mInflater.inflate(R.layout.member_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        MemberItem memberItem = mMemberItems.get(position);

        viewHolder.name.setText(memberItem.name);
        Glide.with(mActivity)
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", memberItem.uid), new LazyHeaders.Builder()
                        .addHeader("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie())
                        .build()))
                .apply(new RequestOptions().centerCrop().error(R.drawable.profile_img_square))
                .into(viewHolder.profileImg);
        return convertView;
    }

    private static class ViewHolder {
        private ImageView profileImg;
        private TextView name;

        ViewHolder(View itemView) {
            name = itemView.findViewById(R.id.tv_name);
            profileImg = itemView.findViewById(R.id.iv_profile_image);
        }
    }
}
