package com.hhp227.knu_minigroup.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.databinding.BindingAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.YouTubeItem;

import java.util.List;
import java.util.function.Consumer;

public class BindingAdapters {
    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(view.getContext())
                    .load(url)
                    .into(view);
        }
    }

    @BindingAdapter(value = {"userImageUrl", "cookie"}, requireAll = false)
    public static void loadUserImage(ImageView view, String url, String cookie) {
        if (url != null && !url.isEmpty()) {
            Glide.with(view.getContext())
                    .load(new GlideUrl(url, new LazyHeaders.Builder()
                            .addHeader("Cookie", cookie)
                            .build()))
                    .apply(new RequestOptions().circleCrop()
                            .error(R.drawable.user_image_view_circle)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(view);
        }
    }

    @BindingAdapter("onFocusChange")
    public static void focusChange(View view, View.OnFocusChangeListener onFocusChangeListener) {
        view.setOnFocusChangeListener(onFocusChangeListener);
    }

    @BindingAdapter("onRefresh")
    public static void refresh(SwipeRefreshLayout swipeRefreshLayout, SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
    }

    @BindingAdapter(value = {"imageList", "onImageClick", "youtube"}, requireAll = false)
    public static void bindImageList(LinearLayout view, List<String> list, Consumer<Integer> onImageClickListener, YouTubeItem youTubeItem) {

    }
}
