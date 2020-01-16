package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.dto.GroupItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupGridAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mGroupItemKeys;
    private List<GroupItem> mGroupItemValues;
    private ViewHolder mViewHolder;

    public GroupGridAdapter(Context context, List<String> groupItemKeys, List<GroupItem> groupItemValues) {
        this.mContext = context;
        this.mGroupItemKeys = groupItemKeys;
        this.mGroupItemValues = groupItemValues;
    }

    @Override
    public int getCount() {
        return mGroupItemValues.size();
    }

    @Override
    public Object getItem(int position) {
        return mGroupItemValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mInflater == null)
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.group_grid_item, null);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else
            mViewHolder = (ViewHolder) convertView.getTag();

        GroupItem groupItem = mGroupItemValues.get(position);

        if (!groupItem.isAd()) {
            mViewHolder.groupLayout.setVisibility(View.VISIBLE);
            mViewHolder.adView.setVisibility(View.GONE);
        } else {
            AdLoader.Builder builder = new AdLoader.Builder(mContext, "ca-app-pub-3940256099942544/2247696110");
            final View finalConvertView = convertView;
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    MediaView mediaView = finalConvertView.findViewById(R.id.ad_media);
                    TextView headlineView = finalConvertView.findViewById(R.id.ad_headline);
                    TextView bodyView = finalConvertView.findViewById(R.id.ad_body);
                    TextView advertiser = finalConvertView.findViewById(R.id.ad_advertiser);
                    mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                    mViewHolder.adView.setMediaView(mediaView);
                    mViewHolder.adView.setHeadlineView(headlineView);
                    mViewHolder.adView.setBodyView(bodyView);
                    mViewHolder.adView.setAdvertiserView(advertiser);
                    headlineView.setText(unifiedNativeAd.getHeadline());
                    mViewHolder.adView.getMediaView().setMediaContent(unifiedNativeAd.getMediaContent());
                    if (unifiedNativeAd.getBody() != null) {
                        bodyView.setText(unifiedNativeAd.getBody());
                        mViewHolder.adView.getBodyView().setVisibility(View.VISIBLE);
                    } else
                        mViewHolder.adView.getBodyView().setVisibility(View.INVISIBLE);
                    if (unifiedNativeAd.getAdvertiser() != null) {
                        advertiser.setText(unifiedNativeAd.getAdvertiser());
                        mViewHolder.adView.getAdvertiserView().setVisibility(View.VISIBLE);
                    } else
                        mViewHolder.adView.getAdvertiserView().setVisibility(View.GONE);

                    mViewHolder.adView.setNativeAd(unifiedNativeAd);
                    mediaView.addView(getAdText());
                }
            });
            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int i) {
                    Toast.makeText(mContext, "광고 불러오기 실패 : " + i, Toast.LENGTH_LONG).show();
                }
            }).build();
            adLoader.loadAd(new AdRequest.Builder().build());
            mViewHolder.groupLayout.setVisibility(View.GONE);
            mViewHolder.adView.setVisibility(View.VISIBLE);
        }

        mViewHolder.groupName.setText(groupItem.getName());
        Glide.with(mContext).load(groupItem.getImage()).transition(new DrawableTransitionOptions().crossFade(150)).into(mViewHolder.groupImage);

        return convertView;
    }

    private TextView getAdText() {
        TextView adText = new TextView(mContext);
        adText.setText(mContext.getString(R.string.ad_attribution));
        adText.setTextSize(12);
        adText.setBackgroundColor(mContext.getResources().getColor(R.color.bg_ad_attribution));
        adText.setTextColor(mContext.getResources().getColor(R.color.txt_ad_attribution));
        adText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        adText.setGravity(Gravity.CENTER_VERTICAL);
        return adText;
    }

    public String getKey(int position) {
        return mGroupItemKeys.get(position);
    }

    public static class ViewHolder {
        private ImageView groupImage;
        private LinearLayout groupLayout;
        private TextView groupName;
        private UnifiedNativeAdView adView;

        ViewHolder(View itemView) {
            adView = itemView.findViewById(R.id.unav);
            groupLayout = itemView.findViewById(R.id.ll_group);
            groupImage = itemView.findViewById(R.id.iv_group_image);
            groupName = itemView.findViewById(R.id.tv_group_name);
        }
    }
}