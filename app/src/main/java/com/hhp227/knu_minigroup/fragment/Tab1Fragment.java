package com.hhp227.knu_minigroup.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.FeedListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.FeedItem;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab1Fragment extends BaseFragment {
    private static final int OFFSET = 1;
    private static final int LIMIT = 10;
    private FeedListAdapter feedListAdapter;
    private List<FeedItem> feedItems;
    private ListView listView;
    private Source source;
    private SwipeRefreshLayout swipeRefreshLayout;

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

        listView = rootView.findViewById(R.id.lv_feed);
        swipeRefreshLayout = rootView.findViewById(R.id.srl_feed_list);

        feedItems = new ArrayList<>();
        feedListAdapter = new FeedListAdapter(getActivity(), feedItems);

        listView.setAdapter(feedListAdapter);

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
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);

        String param = "?CLUB_GRP_ID=" + groupId + "&startL=" + OFFSET + "&displayL=" + LIMIT;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_FEED_LIST + param, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                source = new Source(response);
                try {
                    List<Element> list = source.getAllElementsByClass("listbox2");
                    for (Element element : list) {
                        String title = element.getFirstElementByClass("list_title").getTextExtractor().toString();
                        FeedItem feedItem = new FeedItem();
                        feedItem.setName(title);

                        feedItems.add(feedItem);
                    }
                    feedListAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "값이 없습니다.", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
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

        return rootView;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return listView != null && listView.canScrollVertically(direction);
    }
}
