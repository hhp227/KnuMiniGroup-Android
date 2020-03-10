package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.WriteItem;

import java.util.List;

public class WriteListAdapter extends ArrayAdapter<WriteItem> {
    private int resource;
    private Context mContext;
    private LayoutInflater mInflater;

    public WriteListAdapter(Context context, int resource, List<WriteItem> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (mInflater == null)
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(resource, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        WriteItem writeItem = getItem(position);

        viewHolder.imageView.setVisibility(writeItem.getImage() != null || writeItem.getBitmap() != null ? View.VISIBLE : View.GONE);
        if (writeItem.getFileUri() != null)
            viewHolder.imageView.setImageBitmap(writeItem.getBitmap());
        if (writeItem.getImage() != null)
            Glide.with(mContext).load(writeItem.getImage()).into(viewHolder.imageView);

        return convertView;
    }

    private static class ViewHolder {
        private ImageView imageView;

        ViewHolder(View itemView) {
            imageView = itemView.findViewById(R.id.iv_image_preview);
        }
    }
}
