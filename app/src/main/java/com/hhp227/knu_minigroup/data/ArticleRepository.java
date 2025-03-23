package com.hhp227.knu_minigroup.data;

import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.NetworkResponse;
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
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.helper.DateUtil;
import com.hhp227.knu_minigroup.volley.util.MultipartRequest;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleRepository {
    private final String mGroupId, mGroupKey;

    private String mLastKey = null; // 마지막으로 가져온 데이터의 키

    private boolean mStopRequestMore = false;

    public ArticleRepository(String groupId, String key) {
        this.mGroupId = groupId;
        this.mGroupKey = key;
    }

    public boolean isStopRequestMore() {
        return mStopRequestMore;
    }

    public void setLastKey(String lastKey) {
        this.mLastKey = lastKey;
    }

    public void getArticleList(String cookie, int limit, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        Query query = databaseReference.child(mGroupKey).orderByKey().limitToLast(limit);

        if (mLastKey != null) {
            query = query.endBefore(mLastKey);
        }
        callback.onLoading();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newLastKey = null;
                List<Map.Entry<String, ArticleItem>> articleItemList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ArticleItem value = snapshot.getValue(ArticleItem.class);

                    if (articleItemList.isEmpty()) {
                        newLastKey = key; // 마지막 키 저장
                    }
                    if (value != null) {
                        articleItemList.add(0, new AbstractMap.SimpleEntry<>(key, value));
                    }
                }
                if (newLastKey == null) {
                    mStopRequestMore = true;
                }
                mLastKey = newLastKey; // 다음 페이지 요청을 위해 키 업데이트
                callback.onSuccess(articleItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
    }

    public void getArticleData(String cookie, String articleId, String articleKey, String params, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        callback.onLoading();
        databaseReference.child(mGroupKey).child(articleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem value = dataSnapshot.getValue(ArticleItem.class);

                if (value != null) {
                    callback.onSuccess(value);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public void addArticle(String cookie, User user, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.WRITE_ARTICLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("isError");

                    if (!error) {
                        getArticleId(cookie, user, title, Html.fromHtml(content).toString().trim(), imageList, youTubeItem, callback);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFailure(e);
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("SBJT", title);
                params.put("CLUB_GRP_ID", mGroupId);
                params.put("TXT", content);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    public void setArticle(String cookie, String articleId, String articleKey, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_ARTICLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    initFirebaseData(articleKey, title, Html.fromHtml(content).toString().trim(), imageList, youTubeItem, callback);
                } catch (Exception e) {
                    e.printStackTrace();
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGroupId);
                params.put("ARTL_NUM", articleId);
                params.put("SBJT", title);
                params.put("TXT", content);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    public void removeArticle(String cookie, String articleId, String articleKey, Callback callback) {
        String tag_string_req = "req_delete";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_ARTICLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("isError");

                    if (!error) {
                        deleteArticleFromFirebase(articleKey, callback);
                    }
                } catch (JSONException e) {
                    callback.onFailure(e);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGroupId);
                params.put("ARTL_NUM", articleId);
                return params;
            }
        };

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    public void addArticleImage(String cookie, Bitmap bitmap, Callback callback) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, EndPoint.IMAGE_UPLOAD, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String imageSrc = new String(response.data);
                imageSrc = EndPoint.BASE_URL + imageSrc.substring(imageSrc.lastIndexOf("/ilosfiles2/"), imageSrc.lastIndexOf("\""));

                callback.onSuccess(imageSrc);
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

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                params.put("file", new DataPart(System.currentTimeMillis() + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        };

        AppController.getInstance().addToRequestQueue(multipartRequest);
    }

    private void getArticleId(String cookie, User user, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&displayL=1";

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                String artlNum = source.getFirstElementByClass("comment_wrap").getAttributeValue("num");

                insertArticleToFirebase(artlNum, user, title, content, imageList, youTubeItem, callback);
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
        });
    }

    private void initFirebaseData(List<Map.Entry<String, ArticleItem>> articleItemList, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        fetchArticleListFromFirebase(databaseReference.child(mGroupKey), articleItemList, callback);
    }

    private void initFirebaseData(String articleKey, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        updateArticleDataToFirebase(databaseReference.child(mGroupKey).child(articleKey), title, content, imageList, youTubeItem, callback);
    }

    private void fetchArticleListFromFirebase(Query query, List<Map.Entry<String, ArticleItem>> articleItemList, Callback callback) {

    }

    private void fetchArticleDataFromFirebase(ArticleItem articleItem, String articleKey, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        databaseReference.child(mGroupKey).child(articleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem value = dataSnapshot.getValue(ArticleItem.class);

                if (value != null) {
                    articleItem.setUid(value.getUid());
                }
                callback.onSuccess(articleItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private void insertArticleToFirebase(String artlNum, User user, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        Map<String, Object> map = new HashMap<>();

        map.put("id", artlNum);
        map.put("uid", user.getUid());
        map.put("name", user.getName());
        map.put("title", title);
        map.put("timestamp", System.currentTimeMillis());
        map.put("content", TextUtils.isEmpty(content) ? null : content);
        map.put("images", imageList);
        map.put("youtube", youTubeItem);
        databaseReference.child(mGroupKey).push().setValue(map);
        callback.onSuccess(artlNum);
    }

    private void updateArticleDataToFirebase(final Query query, final String title, final String content, final List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);

                if (articleItem != null) {
                    articleItem.setTitle(title);
                    articleItem.setContent(TextUtils.isEmpty(content) ? null : content);
                    articleItem.setImages(imageList.isEmpty() ? null : imageList);
                    articleItem.setYoutube(youTubeItem);
                    query.getRef().setValue(articleItem);
                    callback.onSuccess(articleItem);
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private void deleteArticleFromFirebase(String articleKey, Callback callback) {
        DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");

        articlesReference.child(mGroupKey).child(articleKey).removeValue();
        replysReference.child(articleKey).removeValue();
        callback.onSuccess(null);
    }

    private String contentExtractor(Element listCont) {
        StringBuilder sb = new StringBuilder();

        for (Element childElement : listCont.getChildElements()) {
            sb.append(childElement.getTextExtractor().toString().concat("\n"));
        }
        return sb.toString().trim();
    }

    private List<String> imageExtract(Element listCont) {
        List<String> result = new ArrayList<>();

        for (Element p : listCont.getAllElements(HTMLElementName.P)) {
            try {
                if (p.getFirstElement(HTMLElementName.IMG) != null) {
                    Element image = p.getFirstElement(HTMLElementName.IMG);
                    String imageUrl = !image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src");

                    result.add(imageUrl);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private YouTubeItem youtubeExtract(Element listCont) {
        YouTubeItem youTubeItem = null;
        int position = 0;

        for (Element p : listCont.getAllElements(HTMLElementName.P)) {
            try {
                if (p.getFirstElement(HTMLElementName.IMG) != null) {
                    position++;
                } else if (p.getFirstElementByClass("youtube-player") != null) {
                    Element youtube = p.getFirstElementByClass("youtube-player");
                    String youtubeUrl = youtube.getAttributeValue("src");
                    String youtubeId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1, youtubeUrl.lastIndexOf("?"));
                    String thumbnail = "https://i.ytimg.com/vi/" + youtubeId + "/mqdefault.jpg";
                    youTubeItem = new YouTubeItem(youtubeId, null, null, thumbnail, null);
                    youTubeItem.position = position;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return youTubeItem;
    }
}