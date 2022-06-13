package com.hhp227.knu_minigroup.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.helper.DateUtil;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleRepository {
    private final String mKey;

    private boolean mStopRequestMore = false;

    private long mMinId;

    public ArticleRepository(String key) {
        this.mKey = key;
    }

    public boolean isStopRequestMore() {
        return mStopRequestMore;
    }

    public void setMinId(long minId) {
        this.mMinId = minId;
    }

    public void getArticleList(String cookie, String params, Callback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                List<Map.Entry<String, ArticleItem>> articleItemList = new ArrayList<>();

                try {
                    List<Element> list = source.getAllElementsByClass("listbox2");

                    for (Element element : list) {
                        Element viewArt = element.getFirstElementByClass("view_art");
                        Element commentWrap = element.getFirstElementByClass("comment_wrap");
                        boolean auth = viewArt.getAllElementsByClass("btn-small-gray").size() > 0;
                        String id = commentWrap.getAttributeValue("num");
                        String listTitle = viewArt.getFirstElementByClass("list_title").getTextExtractor().toString();
                        String title = listTitle.substring(0, listTitle.lastIndexOf("-"));
                        String name = listTitle.substring(listTitle.lastIndexOf("-") + 1);
                        String date = viewArt.getFirstElement(HTMLElementName.TD).getTextExtractor().toString();
                        List<Element> images = viewArt.getAllElements(HTMLElementName.IMG);
                        List<String> imageList = new ArrayList<>();
                        StringBuilder content = new StringBuilder();
                        String replyCnt = commentWrap.getFirstElementByClass("commentBtn").getTextExtractor().toString(); // 댓글 + commentWrap.getFirstElementByClass("comment_cnt").getTextExtractor();
                        ArticleItem articleItem = new ArticleItem();
                        mMinId = mMinId == 0 ? Long.parseLong(id) : Math.min(mMinId, Long.parseLong(id));

                        if (images.size() > 0) {
                            for (Element image : images) {
                                String imageUrl = !image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src");

                                imageList.add(imageUrl);
                            }
                        }
                        for (Element childElement : viewArt.getFirstElementByClass("list_cont").getChildElements()) {
                            content.append(childElement.getTextExtractor().toString().concat("\n"));
                        }
                        if (Long.parseLong(id) > mMinId) {
                            mStopRequestMore = true;
                            break;
                        } else
                            mStopRequestMore = false;
                        articleItem.setId(id);
                        articleItem.setTitle(title.trim());
                        articleItem.setName(name.trim());
                        // 언어설정을 영어로 변역하면 따로 처리를 해줘야함
                        articleItem.setTimestamp(DateUtil.getTimeStamp(date));
                        articleItem.setContent(content.toString().trim());
                        articleItem.setImages(imageList);
                        articleItem.setReplyCount(replyCnt);
                        articleItem.setAuth(auth);
                        if (viewArt.getFirstElementByClass("youtube-player") != null) {
                            String youtubeUrl = viewArt.getFirstElementByClass("youtube-player").getAttributeValue("src");
                            String youtubeId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1, youtubeUrl.lastIndexOf("?"));
                            String thumbnail = "https://i.ytimg.com/vi/" + youtubeId + "/mqdefault.jpg";
                            YouTubeItem youTubeItem = new YouTubeItem(youtubeId, null, null, thumbnail, null);

                            articleItem.setYoutube(youTubeItem);
                        }
                        articleItemList.add(new AbstractMap.SimpleEntry<>(id, articleItem));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    initFirebaseData(articleItemList, callback);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                callback.onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }
        };

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void initFirebaseData(List<Map.Entry<String, ArticleItem>> articleItemList, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        fetchArticleListFromFirebase(databaseReference.child(mKey), articleItemList, callback);
    }

    private void fetchArticleListFromFirebase(Query query, List<Map.Entry<String, ArticleItem>> articleItemList, Callback callback) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ArticleItem value = snapshot.getValue(ArticleItem.class);

                    if (value != null) {
                        int index = -1;

                        for (int i = 0; i < articleItemList.size(); i++) {
                            Map.Entry<String, ArticleItem> entry = articleItemList.get(i);

                            if (entry.getKey().equals(value.getId())) {
                                index = i;
                                break;
                            }
                        }
                        if (index > -1) {
                            ArticleItem articleItem = articleItemList.get(index).getValue();

                            articleItem.setUid(value.getUid());
                            articleItemList.set(index, new AbstractMap.SimpleEntry<>(key, articleItem));
                        }
                    }
                }
                callback.onSuccess(articleItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
    }
}
