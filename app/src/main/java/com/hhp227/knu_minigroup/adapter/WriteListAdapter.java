package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.android.volley.toolbox.ImageLoader;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.WriteItem;
import com.hhp227.knu_minigroup.volley.util.ArticleImageView;

import java.util.List;

public class WriteListAdapter extends ArrayAdapter<WriteItem> {
    private Context context;
    private LayoutInflater layoutInflater;
    private int resource;
    private ArticleImageView articleImageView;
    private ImageView imageView;
    private ImageLoader imageLoader;


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

        if (imageLoader == null)
            imageLoader = app.AppController.getInstance().getImageLoader();

        imageView = convertView.findViewById(R.id.iv_image_preview);
        articleImageView = convertView.findViewById(R.id.fiv_image_preview);

        WriteItem writeItem = getItem(position);

        if (writeItem.getFileUri() != null) {
            imageView.setImageBitmap(writeItem.getBitmap());
            imageView.setVisibility(View.VISIBLE);
        } else
            imageView.setVisibility(View.GONE);

        if (writeItem.getImage() != null) {
            articleImageView.setImageUrl(writeItem.getImage(), imageLoader);
            articleImageView.setVisibility(View.VISIBLE);
        } else
            articleImageView.setVisibility(View.GONE);

        return convertView;
    }
}
