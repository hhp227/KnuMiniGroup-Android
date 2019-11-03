package com.hhp227.knu_minigroup.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.WriteActivity;
import com.hhp227.knu_minigroup.adapter.FeedListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.FeedItem;
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
    private static final int LIMIT = 10;
    private FeedListAdapter feedListAdapter;
    private FloatingActionButton floatingActionButton;
    private List<FeedItem> feedItems;
    private ListView listView;
    private ProgressDialog progressDialog;
    private Source source;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View footerLoading;

    private int offSet;
    private boolean hasRequestedMore; // 데이터 불러올때 중복안되게 하기위한 변수

    private int groupId;

    public Tab1Fragment() {
    }

    public static Tab1Fragment newInstance(int param) {
        Tab1Fragment fragment = new Tab1Fragment();
        Bundle args = new Bundle();
        args.putInt("grp_id", param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getInt("grp_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);

        floatingActionButton = rootView.findViewById(R.id.fab_button);
        footerLoading = View.inflate(getContext(), R.layout.load_more, null);
        listView = rootView.findViewById(R.id.lv_feed);
        swipeRefreshLayout = rootView.findViewById(R.id.srl_feed_list);

        offSet = 1; // offSet 초기화
        feedItems = new ArrayList<>();
        feedListAdapter = new FeedListAdapter(getActivity(), feedItems);

        listView.addFooterView(footerLoading);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WriteActivity.class);
                startActivity(intent);
                return;
            }
        });
        listView.setAdapter(feedListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FeedItem feedItem = feedItems.get(position);
                Toast.makeText(getContext(), "id : " + feedItem.getId(), Toast.LENGTH_LONG).show();
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean lastItemVisibleFlag = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && lastItemVisibleFlag && hasRequestedMore == false) {
                    // 화면이 바닦에 닿을때 처리
                    // 로딩중을 알리는 프로그레스바를 보인다.
                    footerLoading.setVisibility(View.VISIBLE);

                    // 다음 데이터를 불러온다.
                    offSet += LIMIT;
                    hasRequestedMore = true;
                    fetchDataTask();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light, android.R.color.holo_blue_bright);

        progressDialog = ProgressDialog.show(getActivity(), "", "불러오는중...");
        fetchDataTask();

        return rootView;
    }

    private void fetchDataTask() {
        String param = "?CLUB_GRP_ID=" + groupId + "&startL=" + offSet + "&displayL=" + LIMIT;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_FEED_LIST + param, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                source = new Source(response);
                try {
                    List<Element> list = source.getAllElementsByClass("listbox2");
                    Element paging = source.getFirstElementByClass("paging");

                    // 페이징 처리
                    String page = paging.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();

                    for (Element element : list) {
                        Element viewArt = element.getFirstElementByClass("view_art");
                        Element commentWrap = element.getFirstElementByClass("comment_wrap");

                        int id = Integer.parseInt(commentWrap.getAttributeValue("num"));
                        String title = viewArt.getFirstElementByClass("list_title").getTextExtractor().toString();
                        String timeStamp = viewArt.getFirstElement(HTMLElementName.TD).getTextExtractor().toString();
                        String imageUrl;
                        try {
                            imageUrl = viewArt.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                            imageUrl = !imageUrl.contains("http") ? EndPoint.BASE_URL + imageUrl : imageUrl;
                        } catch (Exception e) {
                            e.printStackTrace();
                            imageUrl = null;
                        }
                        StringBuilder content = new StringBuilder();
                        for (Element p : viewArt.getFirstElementByClass("list_cont").getAllElements(HTMLElementName.P)) {
                            content.append(p.getTextExtractor().toString().concat("\n"));
                        }
                        String replyCnt = commentWrap.getContent().getFirstElement(HTMLElementName.P).getTextExtractor().toString();

                        FeedItem feedItem = new FeedItem();
                        feedItem.setId(id);
                        feedItem.setName(title);
                        feedItem.setTimeStamp(timeStamp);
                        feedItem.setContent(content.toString().trim());
                        feedItem.setImage(imageUrl);
                        feedItem.setReplyCount(replyCnt);

                        feedItems.add(feedItem);
                    }
                    feedListAdapter.notifyDataSetChanged();
                    // 중복 로딩 체크하는 Lock을 했던 HasRequestedMore변수를 풀어준다.
                    hasRequestedMore = false;
                } catch (Exception e) {
                    Toast.makeText(getContext(), "값이 없습니다.", Toast.LENGTH_LONG).show();
                } finally {
                    hideProgressDialog();
                }
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

    @Override
    public boolean canScrollVertically(int direction) {
        return listView != null && listView.canScrollVertically(direction);
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        footerLoading.setVisibility(View.GONE);
    }
}
