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
    private Context context;
    private LayoutInflater layoutInflater;
    private int resource;
    private ImageView imageView;

    public WriteListAdapter(Context context, int resource, List<WriteItem> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (layoutInflater == null)
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = layoutInflater.inflate(resource, null);

        imageView = convertView.findViewById(R.id.iv_image_preview);

        WriteItem writeItem = getItem(position);

        imageView.setVisibility(writeItem.getImage() != null || writeItem.getBitmap() != null ? View.VISIBLE : View.GONE);
        if (writeItem.getFileUri() != null)
            imageView.setImageBitmap(writeItem.getBitmap());
        if (writeItem.getImage() != null)
            Glide.with(context).load(writeItem.getImage()).into(imageView);

        return convertView;
    }
}
