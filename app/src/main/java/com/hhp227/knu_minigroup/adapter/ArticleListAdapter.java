package com.hhp227.knu_minigroup.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.hhp227.knu_minigroup.ArticleActivity;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.fragment.Tab1Fragment;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.hhp227.knu_minigroup.fragment.Tab1Fragment.UPDATE_ARTICLE;

public class ArticleListAdapter extends BaseAdapter {
    private static final int CONTENT_MAX_LINE = 4;
    private Activity mActivity;
    private LayoutInflater mInflater;
    private List<String> mArticleItemKeys;
    private List<ArticleItem> mArticleItemValues;
    private String mGroupKey;

    public ArticleListAdapter(Activity activity, List<String> articleItemKeys, List<ArticleItem> articleItemValues, String groupKey) {
        this.mActivity = activity;
        this.mArticleItemKeys = articleItemKeys;
        this.mArticleItemValues = articleItemValues;
        this.mGroupKey = groupKey;
    }

    @Override
    public int getCount() {
        return mArticleItemValues.size();
    }

    @Override
    public Object getItem(int position) {
        return mArticleItemValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (mInflater == null)
            mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.article_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        ArticleItem articleItem = mArticleItemValues.get(position);

        Glide.with(mActivity)
                .load(articleItem.getUid() != null ? new GlideUrl(EndPoint.USER_IMAGE.replace("{UID}", articleItem.getUid()), new LazyHeaders.Builder()
                        .addHeader("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie())
                        .build()) : null)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(viewHolder.profileImage);
        viewHolder.title.setText(articleItem.getName() != null ? articleItem.getTitle() + " - " + articleItem.getName() : articleItem.getTitle());
        viewHolder.timestamp.setText(articleItem.getDate() != null ? articleItem.getDate() : new SimpleDateFormat("yyyy.MM.dd a h:mm:ss").format(articleItem.getTimestamp()));
        // 피드의 메시지가 비었는지 확인
        if (!TextUtils.isEmpty(articleItem.getContent())) {
            viewHolder.content.setText(articleItem.getContent());
            viewHolder.content.setMaxLines(CONTENT_MAX_LINE);
            viewHolder.content.setVisibility(View.VISIBLE);
        } else {
            // 피드 내용이 비었으면 화면에서 삭제
            viewHolder.content.setVisibility(View.GONE);
        }
        viewHolder.contentMore.setVisibility(!TextUtils.isEmpty(articleItem.getContent()) && viewHolder.content.getLineCount() > CONTENT_MAX_LINE ? View.VISIBLE : View.GONE);

        // 피드 이미지
        if (articleItem.getImages() != null && articleItem.getImages().size() > 0) {
            viewHolder.articleImage.setVisibility(View.VISIBLE);
            Glide.with(mActivity)
                    .load(articleItem.getImages().get(0))
                    .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                    .transition(DrawableTransitionOptions.withCrossFade(150))
                    .into(viewHolder.articleImage);
        } else
            viewHolder.articleImage.setVisibility(View.GONE);
        viewHolder.replyCount.setText(articleItem.getReplyCount());

        // 댓글 버튼을 누르면 댓글쓰는곳으로 이동
        viewHolder.replyButton.setTag(position);
        viewHolder.replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                ArticleItem articleItem = mArticleItemValues.get(position);

                Intent intent = new Intent(mActivity, ArticleActivity.class);
                intent.putExtra("grp_id", Tab1Fragment.mGroupId);
                intent.putExtra("grp_nm", Tab1Fragment.mGroupName);
                intent.putExtra("artl_num", articleItem.getId());
                intent.putExtra("position", position + 1);
                intent.putExtra("auth", articleItem.isAuth());
                intent.putExtra("isbottom", true);
                intent.putExtra("grp_key", mGroupKey);
                intent.putExtra("artl_key", getKey(position));
                mActivity.startActivityForResult(intent, UPDATE_ARTICLE);
            }
        });

        return convertView;
    }

    public String getKey(int position) {
        return mArticleItemKeys.get(position);
    }

    public static class ViewHolder {
        private ImageView profileImage, articleImage;
        private LinearLayout replyButton, likeButton;
        private TextView title, timestamp, content, contentMore, replyCount, likeCount;

        ViewHolder(View itemView) {
            profileImage = itemView.findViewById(R.id.iv_profile_image);
            title = itemView.findViewById(R.id.tv_title);
            timestamp = itemView.findViewById(R.id.tv_timestamp);
            content = itemView.findViewById(R.id.tv_content);
            contentMore = itemView.findViewById(R.id.tv_content_more);
            articleImage = itemView.findViewById(R.id.iv_article_image);
            replyCount = itemView.findViewById(R.id.tv_replycount);
            replyButton = itemView.findViewById(R.id.ll_reply);
        }
    }
}
