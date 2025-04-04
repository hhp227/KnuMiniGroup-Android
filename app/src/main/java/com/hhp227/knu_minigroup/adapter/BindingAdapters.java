package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.arch.core.util.Function;
import androidx.databinding.BindingAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.activity.ArticleActivity;
import com.hhp227.knu_minigroup.calendar.ExtendedCalendarView;
import com.hhp227.knu_minigroup.dto.YouTubeItem;

import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

import static com.hhp227.knu_minigroup.viewmodel.YoutubeSearchViewModel.API_KEY;

public class BindingAdapters {
    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(view.getContext())
                    .load(url)
                    .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background))
                    .transition(DrawableTransitionOptions.withCrossFade(150))
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

    @BindingAdapter("userImageBitmap")
    public static void loadUserImage(ImageView view, Bitmap bitmap) {
        if (bitmap != null) {
            Glide.with(view.getContext())
                    .load(bitmap)
                    .apply(RequestOptions.errorOf(R.drawable.user_image_view_circle).circleCrop())
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
    public static void bindImageList(LinearLayout view, List<String> list, Function<Integer, Boolean> onImageClickListener, YouTubeItem youTubeItem) {
        view.removeAllViews();
        if (list != null) {
            view.removeAllViews();
            for (int i = 0; i < list.size(); i++) {
                int finalI = i;
                ImageView articleImage = new ImageView(view.getContext());

                articleImage.setAdjustViewBounds(true);
                articleImage.setPadding(0, 0, 0, 30);
                articleImage.setScaleType(ImageView.ScaleType.FIT_XY);
                articleImage.setOnClickListener(v -> onImageClickListener.apply(finalI));
                Glide.with(view)
                        .load(list.get(i))
                        .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                        .into(articleImage);
                view.addView(articleImage);
            }
        }
        if (youTubeItem != null) {
            LinearLayout youtubeContainer = new LinearLayout(view.getContext());
            YouTubePlayerView youTubePlayerView = new YouTubePlayerView(view.getContext());

            youTubePlayerView.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    youTubePlayer.setShowFullscreenButton(true);
                    if (b) {
                        youTubePlayer.play();
                    } else {
                        try {
                            youTubePlayer.cueVideo(youTubeItem.videoId);
                        } catch (IllegalStateException e) {
                            youTubePlayerView.initialize(API_KEY, this);
                        }
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    try {
                        if (youTubeInitializationResult.isUserRecoverableError())
                            youTubeInitializationResult.getErrorDialog((Activity) view.getContext(), 0).show();
                    } catch (Exception e) {
                        if (e.getMessage() != null) {
                            Log.e(ArticleActivity.class.getSimpleName(), e.getMessage());
                        }
                    }
                }
            });
            youtubeContainer.addView(youTubePlayerView);
            youtubeContainer.setPadding(0, 0, 0, 30);
            view.addView(youtubeContainer, youTubeItem.position);
        }
    }

    @BindingAdapter("calendar")
    public static void bindCalendar(ExtendedCalendarView view, Calendar calendar) {
        view.setCalendar(calendar);
    }

    @BindingAdapter(value = {"onPrevClick", "onNextClick"}, requireAll = false)
    public static void setOnCalendarClickListener(ExtendedCalendarView view, View.OnClickListener onPrevClickListener, View.OnClickListener onNextClickListener) {
        view.prev.setOnClickListener(onPrevClickListener);
        view.next.setOnClickListener(onNextClickListener);
    }
}