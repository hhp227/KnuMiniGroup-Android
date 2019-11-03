package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.android.volley.toolbox.ImageLoader;
import com.hhp227.knu_minigroup.volley.util.FeedImageView;

import java.util.List;

public class WriteListAdapter extends ArrayAdapter<String> {
    private Context context;
    private LayoutInflater layoutInflater;
    private int resource;
    private FeedImageView feedImageView;
    private ImageView imageView;
    private ImageLoader imageLoader;


    public WriteListAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }
}
