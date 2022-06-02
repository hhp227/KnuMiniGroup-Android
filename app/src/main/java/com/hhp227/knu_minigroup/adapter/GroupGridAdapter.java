package com.hhp227.knu_minigroup.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.GroupGridAdBinding;
import com.hhp227.knu_minigroup.databinding.GroupGridHeaderBinding;
import com.hhp227.knu_minigroup.databinding.GroupGridItemBinding;
import com.hhp227.knu_minigroup.databinding.GroupGridNoItemBinding;
import com.hhp227.knu_minigroup.databinding.GroupGridViewPagerBinding;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.ui.loopviewpager.LoopViewPager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupGridAdapter extends RecyclerView.Adapter {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_AD = 2;
    public static final int TYPE_BANNER = 3;
    public static final int TYPE_VIEW_PAGER = 4;
    private static final String TAG = "어뎁터";

    private final List<Map.Entry<String, Object>> mCurrentList = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;

    private LoopViewPager mLoopViewPager;

    private LoopPagerAdapter mLoopPagerAdapter;

    private View.OnClickListener mOnClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TEXT:
                return new HeaderHolder(GroupGridHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_GROUP:
                return new ItemHolder(GroupGridItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_AD:
                return new AdHolder(GroupGridAdBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_BANNER:
                return new BannerHolder(GroupGridNoItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case TYPE_VIEW_PAGER:
                return new ViewPagerHolder(GroupGridViewPagerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        throw new NullPointerException();
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind((Map<String, String>) mCurrentList.get(position).getValue());
        } else if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind((GroupItem) mCurrentList.get(position).getValue());
        } else if (holder instanceof AdHolder) {
            ((AdHolder) holder).bind(holder.itemView.getContext());
        } else if (holder instanceof BannerHolder) {
            mLoopPagerAdapter = new LoopPagerAdapter(Arrays.asList("메인", "이미지1", "이미지2"));
            mLoopViewPager = ((BannerHolder) holder).mBinding.lvpThemeSliderPager;

            mLoopViewPager.setAdapter(mLoopPagerAdapter);
            mLoopPagerAdapter.setOnClickListener(mOnClickListener);
            ((BannerHolder) holder).bind();
        } else if (holder instanceof ViewPagerHolder) {
            ((ViewPagerHolder) holder).bind();
        }
    }

    @Override
    public int getItemCount() {
        return mCurrentList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mCurrentList.get(position).getValue() instanceof Map ? TYPE_TEXT
                : mCurrentList.get(position).getValue() instanceof GroupItem ? TYPE_GROUP
                : mCurrentList.get(position).getValue() instanceof String && mCurrentList.get(position).getValue().equals("광고") ? TYPE_AD
                : mCurrentList.get(position).getValue() instanceof String && mCurrentList.get(position).getValue().equals("없음") ? TYPE_BANNER
                : mCurrentList.get(position).getValue() instanceof String && mCurrentList.get(position).getValue().equals("뷰페이져") ? TYPE_VIEW_PAGER
                : -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<Map.Entry<String, Object>> getCurrentList() {
        return mCurrentList;
    }

    public void submitList(List<Map.Entry<String, Object>> groupItemList) {
        mCurrentList.clear();
        mCurrentList.addAll(groupItemList);
        notifyDataSetChanged();
    }

    private TextView getAdText(Context context) {
        TextView adText = new TextView(context);

        adText.setText(context.getString(R.string.ad_attribution));
        adText.setTextSize(12);
        adText.setBackgroundColor(context.getResources().getColor(R.color.bg_ad_attribution));
        adText.setTextColor(context.getResources().getColor(R.color.txt_ad_attribution));
        adText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        adText.setGravity(Gravity.CENTER_VERTICAL);
        return adText;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    public String getKey(int position) {
        return mCurrentList.get(position).getKey();
    }

    public void moveSliderPager() {
        if (mLoopViewPager == null || mLoopPagerAdapter.getCount() <= 0) {
            return;
        }

        LoopViewPager loopViewPager = mLoopViewPager;
        loopViewPager.setCurrentItem(loopViewPager.getCurrentItem() + 1);
    }

    private static int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("[(]|[)]|[,]")[1].trim());
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        private final GroupGridHeaderBinding mBinding;

        HeaderHolder(GroupGridHeaderBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        private void bind(Map<String, String> map) {
            mBinding.tvTitle.setText(map.get("text"));
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private final GroupGridItemBinding mBinding;

        ItemHolder(GroupGridItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.rlGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onItemClick(v, getAdapterPosition());
                }
            });
            mBinding.ivMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    MenuInflater inflater = popupMenu.getMenuInflater();

                    inflater.inflate(R.menu.menu_group, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_group_menu1:
                                    Toast.makeText(v.getContext(), "테스트1", Toast.LENGTH_LONG).show();
                                    return true;
                                case R.id.action_group_menu2:
                                    Toast.makeText(v.getContext(), "테스트2", Toast.LENGTH_LONG).show();
                                    return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        private void bind(GroupItem groupItem) {
            Glide.with(itemView.getContext())
                    .load(groupItem.getImage())
                    .transition(new DrawableTransitionOptions().crossFade(150))
                    .into(mBinding.ivGroupImage);
            mBinding.tvTitle.setText(groupItem.getName());
            mBinding.rlGroup.setVisibility(View.VISIBLE);
        }
    }

    public class AdHolder extends RecyclerView.ViewHolder {
        private final GroupGridAdBinding mBinding;

        AdHolder(GroupGridAdBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        private void bind(final Context context) {
            AdLoader.Builder builder = new AdLoader.Builder(context, context.getString(R.string.native_ad));
            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    mBinding.adMedia.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                    mBinding.unav.setMediaView(mBinding.adMedia);
                    mBinding.unav.setHeadlineView(mBinding.adHeadline);
                    mBinding.unav.setBodyView(mBinding.adBody);
                    mBinding.unav.setAdvertiserView(mBinding.adAdvertiser);
                    mBinding.adHeadline.setText(unifiedNativeAd.getHeadline());
                    mBinding.unav.getMediaView().setMediaContent(unifiedNativeAd.getMediaContent());
                    if (unifiedNativeAd.getBody() != null) {
                        mBinding.adBody.setText(unifiedNativeAd.getBody());
                        mBinding.unav.getBodyView().setVisibility(View.VISIBLE);
                    } else
                        mBinding.unav.getBodyView().setVisibility(View.INVISIBLE);
                    if (unifiedNativeAd.getAdvertiser() != null) {
                        mBinding.adAdvertiser.setText(unifiedNativeAd.getAdvertiser());
                        mBinding.unav.getAdvertiserView().setVisibility(View.VISIBLE);
                    } else
                        mBinding.unav.getAdvertiserView().setVisibility(View.GONE);
                    mBinding.unav.setNativeAd(unifiedNativeAd);
                    mBinding.adMedia.addView(getAdText(context));
                }
            });
            AdLoader adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    Toast.makeText(context, "광고", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Toast.makeText(context, "광고 불러오기 실패 : " + i, Toast.LENGTH_LONG).show();
                }
            }).build();
            adLoader.loadAd(new AdRequest.Builder().build());
            mBinding.unav.setVisibility(View.VISIBLE);
        }
    }

    public static class BannerHolder extends RecyclerView.ViewHolder {
        private final GroupGridNoItemBinding mBinding;

        BannerHolder(GroupGridNoItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind() {
            mBinding.cpiThemeSliderIndicator.setViewPager(mBinding.lvpThemeSliderPager);
        }
    }

    public static class ViewPagerHolder extends RecyclerView.ViewHolder {
        private final GroupGridViewPagerBinding mBinding;

        ViewPagerHolder(GroupGridViewPagerBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        private void bind() {
            final int margin = 120;
            final List<GroupItem> popularItemList = new ArrayList<>();
            final GroupPagerAdapter groupPagerAdapter = new GroupPagerAdapter(popularItemList);

            mBinding.viewPager.setAdapter(groupPagerAdapter);
            mBinding.viewPager.setClipToPadding(false);
            mBinding.viewPager.setPadding(margin, 0, margin, 0);
            mBinding.viewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                    if (mBinding.viewPager.getCurrentItem() == 0) {
                        page.setTranslationX(-(margin * 3) / 4);
                    } else if (mBinding.viewPager.getCurrentItem() == groupPagerAdapter.getCount() - 1) {
                        page.setTranslationX(margin * 3 / 4);
                    } else {
                        page.setTranslationX(-((margin / 2) + (margin / 8)));
                    }
                }
            });
            mBinding.viewPager.setPageMargin(margin / 4);
            mBinding.pbGroup.setVisibility(View.VISIBLE);
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
                            if (e.getMessage() != null)
                                Log.e(TAG, e.getMessage());
                        }
                    }
                    groupPagerAdapter.notifyDataSetChanged();
                    mBinding.pbGroup.setVisibility(View.GONE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e(TAG, error.getMessage());
                    mBinding.pbGroup.setVisibility(View.GONE);
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

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}