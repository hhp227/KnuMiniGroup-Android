package com.hhp227.knu_minigroup.adapter;

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
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.MemberItem;

import java.util.List;

public class MemberListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<MemberItem> mMemberItems;

    public MemberListAdapter(Context context, List<MemberItem> memberItems) {
        this.mContext = context;
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
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.member_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        MemberItem memberItem = mMemberItems.get(position);
        Glide.with(mContext)
                .load(new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", memberItem.uid), new LazyHeaders.Builder()
                        .addHeader("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN))
                        .build()))
                .apply(RequestOptions.circleCropTransform()
                        .error(R.drawable.user_image_view_circle))
                .into(viewHolder.profileImage);
        viewHolder.name.setText(memberItem.name);
        viewHolder.department.setText(memberItem.dept);
        viewHolder.division.setText(memberItem.div);
        viewHolder.registerDate.setText(memberItem.regDate);

        return convertView;
    }

    public static class ViewHolder {
        private ImageView profileImage;
        private TextView name, department, division, registerDate;

        public ViewHolder(View itemView) {
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            name = itemView.findViewById(R.id.column1);
            department = itemView.findViewById(R.id.column2);
            division = itemView.findViewById(R.id.column3);
            registerDate = itemView.findViewById(R.id.column4);
        }
    }
}
