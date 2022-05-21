package com.hhp227.knu_minigroup.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.ReplyItem;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.helper.DateUtil;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleViewModel extends ViewModel {
    public final List<String> mReplyItemKeys = new ArrayList<>();

    public final List<ReplyItem> mReplyItemValues = new ArrayList<>();

    private static final String TAG = ArticleViewModel.class.getSimpleName(), STATE = "state", REPLY_FORM_STATE = "replyFormState";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final Integer mPosition;

    private final String mGroupId, mArticleId, mGroupKey, mArticleKey;

    private final SavedStateHandle mSavedStateHandle;

    public ArticleViewModel(SavedStateHandle savedStateHandle) {
        this.mSavedStateHandle = savedStateHandle;
        this.mGroupId = savedStateHandle.get("grp_id");
        this.mArticleId = savedStateHandle.get("artl_num");

        this.mGroupKey = savedStateHandle.get("grp_key");
        this.mArticleKey = savedStateHandle.get("artl_key");
        this.mPosition = savedStateHandle.get("position");
        Log.e("TEST", "ArticleViewModel init");
        //fetchArticleData(mArticleId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.e("TEST", "ArticleViewModel onCleared");
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public LiveData<ReplyFormState> getReplyFormState() {
        return mSavedStateHandle.getLiveData(REPLY_FORM_STATE);
    }

    public void setScrollToLastState(boolean bool) {
        mSavedStateHandle.set("isbottom", bool);
    }

    public LiveData<Boolean> getScrollToLastState() {
        return mSavedStateHandle.getLiveData("isbottom");
    }

    private void fetchArticleData(String articleId) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + mPosition + "&displayL=1";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response.trim());
                ArticleItem articleItem = new ArticleItem();

                try {
                    Element element = source.getFirstElementByClass("listbox2");
                    Element viewArt = element.getFirstElementByClass("view_art");
                    Element commentWrap = element.getFirstElementByClass("comment_wrap");
                    Element listCont = viewArt.getFirstElementByClass("list_cont");
                    List<Element> commentList = element.getAllElementsByClass("comment-list");
                    String listTitle = viewArt.getFirstElementByClass("list_title").getTextExtractor().toString();
                    String title = listTitle.substring(0, listTitle.lastIndexOf("-")).trim();
                    String name = listTitle.substring(listTitle.lastIndexOf("-") + 1).trim();
                    String timeStamp = viewArt.getFirstElement(HTMLElementName.TD).getTextExtractor().toString();
                    String content = contentExtractor(listCont);
                    List<String> imageList = imageExtract(listCont);
                    YouTubeItem youTubeItem = youtubeExtract(listCont);
                    String replyCnt = commentWrap.getFirstElementByClass("commentBtn").getTextExtractor().toString();

                    articleItem.setId(articleId);
                    articleItem.setName(name);
                    articleItem.setTitle(title);
                    articleItem.setContent(content);
                    articleItem.setImages(imageList);
                    articleItem.setYoutube(youTubeItem);
                    articleItem.setTimestamp(DateUtil.getTimeStamp(timeStamp));
                    articleItem.setReplyCount(replyCnt);
                    Log.e("TEST", "articleItem: " + articleItem);
                    mSavedStateHandle.set(STATE, new State(false, articleItem, Collections.emptyList(), Collections.emptyList(), false, null));
                    fetchReplyData(commentList);
                    /*if (mIsUpdate)
                        deliveryUpdate(title, content, replyCnt);*/
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, "값이 없습니다."));
                } finally {
                    fetchArticleDataFromFirebase();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }
        };

        mSavedStateHandle.set(STATE, new State(true, null, Collections.emptyList(), Collections.emptyList(), false, null));
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void fetchReplyData(List<Element> commentList) {
        List<String> replyItemKeys = new ArrayList<>();
        List<ReplyItem> replyItemValues = new ArrayList<>();

        try {
            for (Element comment : commentList) {
                Element commentName = comment.getFirstElementByClass("comment-name");
                Element commentAddr = comment.getFirstElementByClass("comment-addr");
                String replyId = commentAddr.getAttributeValue("id").replace("cmt_txt_", "");
                String name = commentName.getTextExtractor().toString().trim();
                String timeStamp = commentName.getFirstElement(HTMLElementName.SPAN).getContent().toString().trim();
                String replyContent = commentAddr.getContent().toString().trim();
                boolean authorization = commentName.getAllElements(HTMLElementName.INPUT).size() > 0;
                ReplyItem replyItem = new ReplyItem();

                replyItem.setId(replyId);
                replyItem.setName(name.substring(0, name.lastIndexOf("(")));
                replyItem.setReply(Html.fromHtml(replyContent).toString());
                replyItem.setDate(timeStamp.replaceAll("[(]|[)]", ""));
                replyItem.setAuth(authorization);
                replyItemKeys.add(replyId);
                replyItemValues.add(replyItem);
            }
            // isBotoom이 참이면 화면 아래로 이동
            /*if (mIsBottom)
                setListViewBottom();*/
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, e.getMessage()));
        } finally {
            fetchReplyListFromFirebase(replyItemKeys, replyItemValues);
        }
    }

    public void actionSend(String text) {
        if (text.length() > 0) {
            String tag_string_req = "req_send";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.INSERT_REPLY, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Source source = new Source(response);
                    List<Element> commentList = source.getAllElementsByClass("comment-list");

                    try {
                        refreshReply(commentList);

                        // 전송할때마다 리스트뷰 아래로
                        setScrollToLastState(true);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        insertReplyToFirebase(commentList.get(commentList.size() - 1).getFirstElementByClass("comment-addr").getAttributeValue("id").replace("cmt_txt_", ""), text);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e(TAG, error.getMessage());
                    mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, error.getMessage()));
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("CLUB_GRP_ID", mGroupId);
                    params.put("ARTL_NUM", mArticleId);
                    params.put("CMT", text);
                    return params;
                }
            };

            mSavedStateHandle.set(STATE, new State(true, null, Collections.emptyList(), Collections.emptyList(), false, null));
            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);




            /*actionSend(text);

            // 전송하면 텍스트 초기화
            mActivityArticleBinding.etReply.setText("");
            if (v != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }*/
        } else {
            mSavedStateHandle.set(REPLY_FORM_STATE, new ReplyFormState("댓글을 입력하세요."));
        }
    }

    public void deleteArticle() {
        String tag_string_req = "req_delete";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_ARTICLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("isError");

                    if (!error) {
                        mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), true, "삭제완료"));
                    } else {
                        mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, "삭제할수 없습니다."));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, e.getMessage()));
                } finally {
                    // 로직 애매함 get은 파이어베이스에서 데이터 처리후 state에 postValue하는데 여기서는 파이어베이스 처리전에 postValue함
                    deleteArticleFromFirebase();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGroupId);
                params.put("ARTL_NUM", mArticleId);
                return params;
            }
        };

        mSavedStateHandle.set(STATE, new State(true, null, Collections.emptyList(), Collections.emptyList(), false, null));
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    public void deleteReply(String replyId, String replyKey) {
        String tag_string_req = "req_delete";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_REPLY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);

                try {
                    if (!response.contains("처리를 실패했습니다")) {
                        List<Element> commentList = source.getAllElementsByClass("comment-list");

                        refreshReply(commentList);
                    }
                } catch (Exception e) {
                    mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, e.getMessage()));
                    Log.e(TAG, e.getMessage());
                } finally {
                    deleteReplyFromFirebase(replyKey);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGroupId);
                params.put("CMMT_NUM", replyId);
                params.put("ARTL_NUM", mArticleId);
                return params;
            }
        };

        mSavedStateHandle.set(STATE, new State(true, null, Collections.emptyList(), Collections.emptyList(), false, null));
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    public void refreshReply(List<Element> commentList) {
        mReplyItemKeys.clear();
        mReplyItemValues.clear();
        fetchReplyData(commentList);
    }

    public void addAll(List<String> replyItemKeys, List<ReplyItem> replyItemValues) {
        if (replyItemKeys.size() == replyItemValues.size()) {
            mReplyItemKeys.addAll(replyItemKeys);
            mReplyItemValues.addAll(replyItemValues);
        }
    }

    private void fetchArticleDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        databaseReference.child(mGroupKey).child(mArticleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem value = dataSnapshot.getValue(ArticleItem.class);

                if (value != null) {
                    mSavedStateHandle.set(STATE, new State(false, value, Collections.emptyList(), Collections.emptyList(), false, null));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, databaseError.getMessage()));
            }
        });
    }

    private void deleteArticleFromFirebase() {
        DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");

        articlesReference.child(mGroupKey).child(mArticleKey).removeValue();
        replysReference.child(mArticleKey).removeValue();
    }

    private void fetchReplyListFromFirebase(List<String> replyItemKeys, List<ReplyItem> replyItemValues) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");

        databaseReference.child(mArticleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ReplyItem value = snapshot.getValue(ReplyItem.class);

                    if (value != null) {
                        int index = replyItemKeys.indexOf(value.getId());

                        if (index > -1) {
                            ReplyItem replyItem = replyItemValues.get(index);

                            replyItem.setUid(value.getUid());
                            replyItemKeys.set(index, key);
                            replyItemValues.set(index, replyItem);
                        }
                    }
                }
                mSavedStateHandle.set(STATE, new State(false, null, replyItemKeys, replyItemValues, false, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), Collections.emptyList(), false, databaseError.getMessage()));
            }
        });
    }

    private void insertReplyToFirebase(String replyId, String text) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        ReplyItem replyItem = new ReplyItem();

        replyItem.setId(replyId);
        replyItem.setUid(mPreferenceManager.getUser().getUid());
        replyItem.setName(mPreferenceManager.getUser().getName());
        replyItem.setTimestamp(System.currentTimeMillis());
        replyItem.setReply(text);
        databaseReference.child(mArticleKey).push().setValue(replyItem);
    }

    private void deleteReplyFromFirebase(String replyKey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");

        databaseReference.child(mArticleKey).child(replyKey).removeValue();
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
                Log.e(TAG, e.getMessage());
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
                Log.e(TAG, e.getMessage());
            }
        }
        return youTubeItem;
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public ArticleItem articleItem;

        public List<String> replyItemKeys;

        public List<ReplyItem> replyItemValues;

        public boolean isSetResultOK;

        public String message;

        public State(boolean isLoading, ArticleItem articleItem, List<String> replyItemKeys, List<ReplyItem> replyItemValues, boolean isSetResultOK, String message) {
            this.isLoading = isLoading;
            this.articleItem = articleItem;
            this.replyItemKeys = replyItemKeys;
            this.replyItemValues = replyItemValues;
            this.isSetResultOK = isSetResultOK;
            this.message = message;
        }

        protected State(Parcel in) {
            isLoading = in.readByte() != 0;
            replyItemKeys = in.createStringArrayList();
            isSetResultOK = in.readByte() != 0;
            message = in.readString();
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeByte((byte) (isLoading ? 1 : 0));
            parcel.writeStringList(replyItemKeys);
            parcel.writeByte((byte) (isSetResultOK ? 1 : 0));
            parcel.writeString(message);
        }
    }

    public static final class ReplyFormState implements Parcelable {
        public String replyError;

        public ReplyFormState(String replyError) {
            this.replyError = replyError;
        }

        protected ReplyFormState(Parcel in) {
            replyError = in.readString();
        }

        public static final Creator<ReplyFormState> CREATOR = new Creator<ReplyFormState>() {
            @Override
            public ReplyFormState createFromParcel(Parcel in) {
                return new ReplyFormState(in);
            }

            @Override
            public ReplyFormState[] newArray(int size) {
                return new ReplyFormState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(replyError);
        }
    }
}
