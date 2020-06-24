package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.ui.loopviewpager.LoopViewPager;
import com.hhp227.knu_minigroup.ui.pageindicator.LoopingCirclePageIndicator;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class GroupGridAdapter extends RecyclerView.Adapter {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_AD = 2;
    public static final int TYPE_BANNER = 3;
    public static final int TYPE_VIEW_PAGER = 4;

    private static final String TAG = "어뎁터";
    private Context mContext;
    private List<String> mGroupItemKeys;
    private List<Object> mGroupItemValues;
    private OnItemClickListener mOnItemClickListener;
    private LoopViewPager mLoopViewPager;
    private LoopPagerAdapter mLoopPagerAdapter;
    private View.OnClickListener mOnClickListener;

    public GroupGridAdapter(Context context, List<String> groupItemKeys, List<Object> groupItemValues) {
        this.mContext = context;
        this.mGroupItemKeys = groupItemKeys;
        this.mGroupItemValues = groupItemValues;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TEXT:
                View headerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_header, parent, false);
                return new HeaderHolder(headerView);
            case TYPE_GROUP:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_item, parent, false);
                return new ItemHolder(itemView);
            case TYPE_AD:
                View adView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_ad, parent, false);
                return new AdHolder(adView);
            case TYPE_BANNER:
                View bannerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_no_item, parent, false);
                return new BannerHolder(bannerView);
            case TYPE_VIEW_PAGER:
                View popularView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_grid_view_pager, parent, false);
                return new ViewPagerHolder(popularView);
        }
        throw new NullPointerException();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof HeaderHolder) {
            Map<String, String> map = (Map<String, String>) mGroupItemValues.get(position);
            ((HeaderHolder) holder).text.setText(map.get("text"));
        } else if (holder instanceof ItemHolder) {
            GroupItem groupItem = (GroupItem) mGroupItemValues.get(position);

            ((ItemHolder) holder).groupLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onItemClick(v, position);
                }
            });
            Glide.with(mContext).load(groupItem.getImage()).transition(new DrawableTransitionOptions().crossFade(150)).into(((ItemHolder) holder).groupImage);
            ((ItemHolder) holder).groupName.setText(groupItem.getName());
            ((ItemHolder) holder).more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext, v);
                    MenuInflater inflater = popupMenu.getMenuInflater();

                    inflater.inflate(R.menu.menu_group, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_group_menu1:
                                    Toast.makeText(mContext, "테스트1", Toast.LENGTH_LONG).show();
                                    return true;
                                case R.id.action_group_menu2:
                                    Toast.makeText(mContext, "테스트2", Toast.LENGTH_LONG).show();
                                    return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
            ((ItemHolder) holder).groupLayout.setVisibility(View.VISIBLE);
        } else if (holder instanceof AdHolder) {
            AdLoader.Builder builder = new AdLoader.Builder(mContext, mContext.getString(R.string.native_ad));
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    ((AdHolder) holder).mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                    ((AdHolder) holder).adView.setMediaView(((AdHolder) holder).mediaView);
                    ((AdHolder) holder).adView.setHeadlineView(((AdHolder) holder).headlineView);
                    ((AdHolder) holder).adView.setBodyView(((AdHolder) holder).bodyView);
                    ((AdHolder) holder).adView.setAdvertiserView(((AdHolder) holder).advertiser);
                    ((AdHolder) holder).headlineView.setText(unifiedNativeAd.getHeadline());
                    ((AdHolder) holder).adView.getMediaView().setMediaContent(unifiedNativeAd.getMediaContent());
                    if (unifiedNativeAd.getBody() != null) {
                        ((AdHolder) holder).bodyView.setText(unifiedNativeAd.getBody());
                        ((AdHolder) holder).adView.getBodyView().setVisibility(View.VISIBLE);
                    } else
                        ((AdHolder) holder).adView.getBodyView().setVisibility(View.INVISIBLE);
                    if (unifiedNativeAd.getAdvertiser() != null) {
                        ((AdHolder) holder).advertiser.setText(unifiedNativeAd.getAdvertiser());
                        ((AdHolder) holder).adView.getAdvertiserView().setVisibility(View.VISIBLE);
                    } else
                        ((AdHolder) holder).adView.getAdvertiserView().setVisibility(View.GONE);

                    ((AdHolder) holder).adView.setNativeAd(unifiedNativeAd);
                    ((AdHolder) holder).mediaView.addView(getAdText());
                }
            });
            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    Toast.makeText(mContext, "광고", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Toast.makeText(mContext, "광고 불러오기 실패 : " + i, Toast.LENGTH_LONG).show();
                }
            }).build();
            adLoader.loadAd(new AdRequest.Builder().build());
            ((AdHolder) holder).adView.setVisibility(View.VISIBLE);
        } else if (holder instanceof BannerHolder) {
            List<String> temp = new ArrayList<>();
            temp.add("메인");
            temp.add("이미지1");
            temp.add("이미지2");
            mLoopPagerAdapter = new LoopPagerAdapter(temp);
            mLoopViewPager = ((BannerHolder) holder).loopViewPager;

            mLoopViewPager.setAdapter(mLoopPagerAdapter);
            mLoopPagerAdapter.setOnClickListener(mOnClickListener);
            ((BannerHolder) holder).circlePageIndicator.setViewPager(mLoopViewPager);
        } else if (holder instanceof ViewPagerHolder) {
            final int margin = 120;
            final List<GroupItem> popularItemList = new ArrayList<>();
            final GroupPagerAdapter groupPagerAdapter = new GroupPagerAdapter(popularItemList);

            ((ViewPagerHolder) holder).viewPager.setAdapter(groupPagerAdapter);
            ((ViewPagerHolder) holder).viewPager.setClipToPadding(false);
            ((ViewPagerHolder) holder).viewPager.setPadding(margin, 0, margin, 0);
            ((ViewPagerHolder) holder).viewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                    if (((ViewPagerHolder) holder).viewPager.getCurrentItem() == 0) {
                        page.setTranslationX(-(margin * 3) / 4);
                    } else if (((ViewPagerHolder) holder).viewPager.getCurrentItem() == groupPagerAdapter.getCount() - 1) {
                        page.setTranslationX(margin * 3 / 4);
                    } else {
                        page.setTranslationX(-((margin / 2) + (margin / 8)));
                    }
                }
            });
            ((ViewPagerHolder) holder).viewPager.setPageMargin(margin / 4);
            ((ViewPagerHolder) holder).progressBar.setVisibility(View.VISIBLE);
            AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Source source = new Source(response);
                    List<Element> list = source.getAllElements("id", "accordion", false);
                    for (Element element : list) {
                        try {
                            Element menuList = element.getFirstElementByClass("menu_list");
                            if (element.getAttributeValue("class").equals("accordion")) {
                                int id = groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"));
                                String imageUrl = EndPoint.BASE_URL + element.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                                String name = element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                                StringBuilder info = new StringBuilder();
                                String description = menuList.getAllElementsByClass("info").get(0).getContent().toString();
                                String joinType = menuList.getAllElementsByClass("info").get(1).getTextExtractor().toString().trim();
                                for (Element span : element.getFirstElement(HTMLElementName.A).getAllElementsByClass("info")) {
                                    String extractedText = span.getTextExtractor().toString();
                                    info.append(extractedText.contains("회원수") ?
                                            extractedText.substring(0, extractedText.lastIndexOf("생성일")).trim() + "\n" :
                                            extractedText + "\n");
                                }
                                GroupItem groupItem = new GroupItem();

                                groupItem.setId(String.valueOf(id));
                                groupItem.setImage(imageUrl);
                                groupItem.setName(name);
                                groupItem.setInfo(info.toString().trim());
                                groupItem.setDescription(description);
                                groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                                popularItemList.add(groupItem);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    groupPagerAdapter.notifyDataSetChanged();
                    ((ViewPagerHolder) holder).progressBar.setVisibility(View.GONE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e(TAG, error.getMessage());
                    ((ViewPagerHolder) holder).progressBar.setVisibility(View.GONE);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
                }

                @Override
                public byte[] getBody() {
                    Map<String, String> params = new HashMap<>();

                    params.put("panel_id", "3");
                    params.put("encoding", "utf-8");
                    if (params.size() > 0) {
                        StringBuilder encodedParams = new StringBuilder();
                        try {
                            for (Map.Entry<String, String> entry : params.entrySet()) {
                                encodedParams.append(URLEncoder.encode(entry.getKey(), getParamsEncoding()));
                                encodedParams.append('=');
                                encodedParams.append(URLEncoder.encode(entry.getValue(), getParamsEncoding()));
                                encodedParams.append('&');
                            }
                            return encodedParams.toString().getBytes(getParamsEncoding());
                        } catch (UnsupportedEncodingException uee) {
                            throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                        }
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mGroupItemValues.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mGroupItemValues.get(position) instanceof Map ? TYPE_TEXT
                : mGroupItemValues.get(position) instanceof GroupItem ? TYPE_GROUP
                : mGroupItemValues.get(position) instanceof String && mGroupItemValues.get(position).equals("광고") ? TYPE_AD
                : mGroupItemValues.get(position) instanceof String && mGroupItemValues.get(position).equals("없음") ? TYPE_BANNER
                : mGroupItemValues.get(position) instanceof String && mGroupItemValues.get(position).equals("뷰페이져") ? TYPE_VIEW_PAGER
                : -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
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

    public void addHeaderView(String text) {
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("text", text);
        mGroupItemKeys.add(text);
        mGroupItemValues.add(headerMap);
        notifyItemInserted(mGroupItemValues.size() - 1);
    }

    public void addHeaderView(String text, int position) {
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("text", text);
        mGroupItemKeys.add(position, text);
        mGroupItemValues.add(position, headerMap);
        notifyItemInserted(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public String getKey(int position) {
        return mGroupItemKeys.get(position);
    }

    public void moveSliderPager() {
        if (mLoopViewPager == null || mLoopPagerAdapter.getCount() <= 0) {
            return;
        }

        LoopViewPager loopViewPager = mLoopViewPager;
        loopViewPager.setCurrentItem(loopViewPager.getCurrentItem() + 1);
    }

    private int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("[(]|[)]|[,]")[1].trim());
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView text;

        HeaderHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_title);
        }
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        private ImageView groupImage, more;
        private RelativeLayout groupLayout;
        private TextView groupName;

        ItemHolder(View itemView) {
            super(itemView);
            groupLayout = itemView.findViewById(R.id.rl_group);
            groupImage = itemView.findViewById(R.id.iv_group_image);
            groupName = itemView.findViewById(R.id.tv_title);
            more = itemView.findViewById(R.id.iv_more);
        }
    }

    public static class AdHolder extends RecyclerView.ViewHolder {
        private MediaView mediaView;
        private TextView headlineView, bodyView, advertiser;
        private UnifiedNativeAdView adView;

        AdHolder(View itemView) {
            super(itemView);
            adView = itemView.findViewById(R.id.unav);
            mediaView = itemView.findViewById(R.id.ad_media);
            headlineView = itemView.findViewById(R.id.ad_headline);
            bodyView = itemView.findViewById(R.id.ad_body);
            advertiser = itemView.findViewById(R.id.ad_advertiser);
        }
    }

    public static class BannerHolder extends RecyclerView.ViewHolder {
        private LoopingCirclePageIndicator circlePageIndicator;
        private LoopViewPager loopViewPager;

        BannerHolder(View itemView) {
            super(itemView);
            circlePageIndicator = itemView.findViewById(R.id.cpi_theme_slider_indicator);
            loopViewPager = itemView.findViewById(R.id.lvp_theme_slider_pager);
        }
    }

    public static class ViewPagerHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;
        private ViewPager viewPager;

        ViewPagerHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.pb_group);
            viewPager = itemView.findViewById(R.id.view_pager);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}