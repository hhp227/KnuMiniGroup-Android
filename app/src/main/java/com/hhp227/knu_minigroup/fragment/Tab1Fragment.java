package com.hhp227.knu_minigroup.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.ArticleActivity;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.WriteActivity;
import com.hhp227.knu_minigroup.adapter.ArticleListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.ui.floatingactionbutton.FloatingActionButton;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab1Fragment extends BaseFragment {
    public static final int LIMIT = 10;
    public static final int UPDATE_ARTICLE = 20;
    public static boolean mIsAdmin;
    public static String mGroupId, mGroupName, mGroupImage, mKey;

    private boolean mHasRequestedMore; // 데이터 불러올때 중복안되게 하기위한 변수
    private int mOffSet, mMinId;
    private long mLastClickTime; // 클릭시 걸리는 시간
    private ArticleListAdapter mAdapter;
    private FloatingActionButton mFloatingActionButton;
    private List<String> mArticleItemKeys;
    private List<ArticleItem> mArticleItemValues;
    private ListView mListView;
    private ProgressDialog mProgressDialog;
    private RelativeLayout mRelativeLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mFooterLoading;

    public Tab1Fragment() {
    }

    public static Tab1Fragment newInstance(boolean isAdmin, String grpId, String grpNm, String grpImg, String key) {
        Tab1Fragment fragment = new Tab1Fragment();
        Bundle args = new Bundle();

        args.putBoolean("admin", isAdmin);
        args.putString("grp_id", grpId);
        args.putString("grp_nm", grpNm);
        args.putString("grp_img", grpImg);
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsAdmin = getArguments().getBoolean("admin");
            mGroupId = getArguments().getString("grp_id");
            mGroupName = getArguments().getString("grp_nm");
            mGroupImage = getArguments().getString("grp_img");
            mKey = getArguments().getString("key");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);
        mFloatingActionButton = rootView.findViewById(R.id.fab_button);
        mFooterLoading = View.inflate(getContext(), R.layout.load_more, null);
        mListView = rootView.findViewById(R.id.lv_article);
        mRelativeLayout = rootView.findViewById(R.id.rl_write);
        mSwipeRefreshLayout = rootView.findViewById(R.id.srl_article_list);
        mArticleItemKeys = new ArrayList<>();
        mArticleItemValues = new ArrayList<>();
        mAdapter = new ArticleListAdapter(getActivity(), mArticleItemKeys, mArticleItemValues, mKey);
        mOffSet = 1; // offSet 초기화
        mProgressDialog = ProgressDialog.show(getActivity(), "", "불러오는중...");

        mListView.addFooterView(mFooterLoading);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WriteActivity.class);

                intent.putExtra("admin", mIsAdmin);
                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("grp_nm", mGroupName);
                intent.putExtra("grp_img", mGroupImage);
                intent.putExtra("key", mKey);
                startActivity(intent);
                return;
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // 두번 클릭시 방지
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                ArticleItem articleItem = mArticleItemValues.get(position);
                Intent intent = new Intent(getContext(), ArticleActivity.class);

                intent.putExtra("admin", mIsAdmin);
                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("grp_nm", mGroupName);
                intent.putExtra("grp_img", mGroupImage);
                intent.putExtra("artl_num", articleItem.getId());
                intent.putExtra("position", position + 1);
                intent.putExtra("auth", articleItem.isAuth() || app.AppController.getInstance().getPreferenceManager().getUser().getUid().equals(articleItem.getUid()));
                intent.putExtra("grp_key", mKey);
                intent.putExtra("artl_key", mAdapter.getKey(position));
                startActivityForResult(intent, UPDATE_ARTICLE);
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean lastItemVisibleFlag;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && lastItemVisibleFlag && !mHasRequestedMore) {

                    // 화면이 바닦에 닿을때 처리
                    mHasRequestedMore = true;

                    // 다음 데이터를 불러온다.
                    mOffSet += LIMIT;

                    // 로딩중을 알리는 프로그레스바를 보인다.
                    mFooterLoading.setVisibility(View.VISIBLE);
                    fetchArticleList();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;
            }
        });
        mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                Intent intent = new Intent(getActivity(), WriteActivity.class);

                intent.putExtra("admin", mIsAdmin);
                intent.putExtra("grp_id", mGroupId);
                intent.putExtra("grp_nm", mGroupName);
                intent.putExtra("grp_img", mGroupImage);
                intent.putExtra("key", mKey);
                startActivity(intent);
                return;
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMinId = 0;
                        mOffSet = 1;

                        mArticleItemKeys.clear();
                        mArticleItemValues.clear();
                        mSwipeRefreshLayout.setRefreshing(false);
                        fetchArticleList();
                    }
                }, 2000);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright);
        fetchArticleList();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_ARTICLE && resultCode == Activity.RESULT_OK) {
            int position = data.getIntExtra("position", 0) - 1;
            ArticleItem articleItem = mArticleItemValues.get(position);

            articleItem.setTitle(data.getStringExtra("sbjt"));
            articleItem.setContent(data.getStringExtra("txt"));
            articleItem.setImages(data.getStringArrayListExtra("img")); // firebase data
            articleItem.setReplyCount(data.getStringExtra("cmmt_cnt"));
            articleItem.setYoutube((YouTubeItem) data.getParcelableExtra("youtube"));
            mArticleItemValues.set(position, articleItem);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mListView != null && mListView.canScrollVertically(direction);
    }

    private void fetchArticleList() {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + mOffSet + "&displayL=" + LIMIT;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);

                hideProgressDialog();
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
                        if (images.size() > 0) {
                            for (Element image : images) {
                                String imageUrl = !image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src");
                                imageList.add(imageUrl);
                            }
                        }
                        StringBuilder content = new StringBuilder();
                        for (Element childElement : viewArt.getFirstElementByClass("list_cont").getChildElements())
                            content.append(childElement.getTextExtractor().toString().concat("\n"));

                        String replyCnt = commentWrap.getContent().getFirstElement(HTMLElementName.P).getTextExtractor().toString();
                        mMinId = mMinId == 0 ? Integer.parseInt(id) : Math.min(mMinId, Integer.parseInt(id));
                        if (Integer.parseInt(id) > mMinId) {
                            mHasRequestedMore = true;
                            break;
                        } else
                            mHasRequestedMore = false;
                        ArticleItem articleItem = new ArticleItem();

                        articleItem.setId(id);
                        articleItem.setTitle(title.trim());
                        articleItem.setName(name.trim());
                        articleItem.setDate(date);
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

                        mArticleItemKeys.add(id);
                        mArticleItemValues.add(articleItem);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    initFirebaseData();
                }
                mAdapter.notifyDataSetChanged();
                mRelativeLayout.setVisibility(!mArticleItemValues.isEmpty() ? View.GONE : View.VISIBLE);
                mFloatingActionButton.setVisibility(!mArticleItemValues.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                hideProgressDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie());
                return headers;
            }
        };
        app.AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        fetchArticleListFromFirebase(databaseReference.child(mKey));
    }

    private void fetchArticleListFromFirebase(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ArticleItem value = snapshot.getValue(ArticleItem.class);
                    int index = mArticleItemKeys.indexOf(value.getId());
                    if (index > -1) {
                        ArticleItem articleItem = mArticleItemValues.get(index);
                        articleItem.setUid(value.getUid());
                        mArticleItemValues.set(index, articleItem);
                        mArticleItemKeys.set(index, key);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        mFooterLoading.setVisibility(View.GONE);
    }
}
