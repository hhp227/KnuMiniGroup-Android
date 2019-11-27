package com.hhp227.knu_minigroup;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.adapter.GroupListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.fragment.GroupInfoFragment;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindActivity extends FragmentActivity {
    private static final int LIMIT = 10;
    private static final String TAG = FindActivity.class.getSimpleName();
    private GroupListAdapter listAdapter;
    private List<GroupItem> groupItems;
    private ListView listView;
    private ProgressDialog progressDialog;
    private Source source;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View footerLoading;
    private boolean hasRequestedMore;
    private int offSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        footerLoading = View.inflate(this, R.layout.load_more, null);
        listView = findViewById(R.id.lv_group);
        swipeRefreshLayout = findViewById(R.id.srl_group_list);
        offSet = 1;
        groupItems = new ArrayList<>();
        listAdapter = new GroupListAdapter(getBaseContext(), groupItems);
        progressDialog = new ProgressDialog(this);

        listView.addFooterView(footerLoading);
        listView.setAdapter(listAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean lastItemVisibleFlag = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && lastItemVisibleFlag && hasRequestedMore == false) {
                    footerLoading.setVisibility(View.VISIBLE);
                    offSet += LIMIT;
                    hasRequestedMore = true;
                    fetchGroupList();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();

                GroupInfoFragment newFragment = GroupInfoFragment.newInstance();
                newFragment.setArguments(args);
                newFragment.show(getSupportFragmentManager(), "dialog");
            }
        });
        progressDialog.setCancelable(false);
        progressDialog.setMessage("불러오는중...");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "새로고침", Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        showProgressDialog();
        fetchGroupList();
    }

    private void fetchGroupList() {
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                source = new Source(response);
                try {
                    List<Element> list = source.getAllElements("id", "accordion", false);
                    for (Element element : list) {
                        Element menuList = element.getFirstElementByClass("menu_list");
                        if (element.getAttributeValue("class").equals("accordion")) {
                            GroupItem groupItem = new GroupItem();
                            groupItem.setId(groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick")));
                            groupItem.setImage(EndPoint.BASE_URL + element.getFirstElement(HTMLElementName.IMG).getAttributeValue("src"));
                            groupItem.setName(element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString());
                            groupItem.setInfo(menuList.getAllElementsByClass("info").get(1).getContent().toString());
                            groupItems.add(groupItem);
                        }
                    }
                    listAdapter.notifyDataSetChanged();
                    hasRequestedMore = false;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    hideProgressDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                hideProgressDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie());
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();
                params.put("panel_id", "1");
                params.put("gubun", "select_share_total");
                params.put("start", String.valueOf(offSet));
                params.put("display", "10");
                params.put("encoding", "utf-8");
                if (params != null && params.size() > 0) {
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

    private int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("[(]|[)]|[,]")[1].trim());
    }

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        footerLoading.setVisibility(View.GONE);
    }
}
