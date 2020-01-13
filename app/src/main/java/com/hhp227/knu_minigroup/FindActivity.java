package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.adapter.GroupListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.fragment.GroupInfoFragment;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;
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
    private static final int LIMIT = 15;
    private static final String TAG = FindActivity.class.getSimpleName();
    private GroupListAdapter listAdapter;
    private List<String> groupItemKeys;
    private List<GroupItem> groupItemValues;
    private ListView listView;
    private ProgressBar progressBar;
    private RelativeLayout relativeLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View footerLoading;
    private boolean hasRequestedMore;
    private int offSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        footerLoading = View.inflate(this, R.layout.load_more, null);
        listView = findViewById(R.id.lv_group);
        relativeLayout = findViewById(R.id.rl_group);
        progressBar = findViewById(R.id.pb_group);
        swipeRefreshLayout = findViewById(R.id.srl_group_list);
        offSet = 1;
        groupItemKeys = new ArrayList<>();
        groupItemValues = new ArrayList<>();
        listAdapter = new GroupListAdapter(getBaseContext(), groupItemKeys, groupItemValues);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
                @Override
                public boolean isLayoutRtl() {
                    return false;
                }
            });
        }
        footerLoading.setVisibility(View.GONE);
        listView.addFooterView(footerLoading);
        listView.setAdapter(listAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean lastItemVisibleFlag;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && lastItemVisibleFlag && !hasRequestedMore) {
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
                GroupItem groupItem = groupItemValues.get(position);

                Bundle args = new Bundle();
                args.putString("grp_id", groupItem.getId());
                args.putString("grp_nm", groupItem.getName());
                args.putString("img", groupItem.getImage());
                args.putString("info", groupItem.getInfo());
                args.putString("desc", groupItem.getDescription());
                args.putString("type", "0");
                args.putString("key", groupItemKeys.get(position));

                GroupInfoFragment newFragment = GroupInfoFragment.newInstance();
                newFragment.setArguments(args);
                newFragment.show(getSupportFragmentManager(), "dialog");
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        offSet = 1;
                        groupItemValues.clear();
                        fetchGroupList();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        fetchGroupList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void fetchGroupList() {
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                List<Element> list = source.getAllElements("id", "accordion", false);
                for (Element element : list) {
                    try {
                        Element menuList = element.getFirstElementByClass("menu_list");
                        if (element.getAttributeValue("class").equals("accordion")) {
                            String id = groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"));
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
                            groupItem.setId(id);
                            groupItem.setImage(imageUrl);
                            groupItem.setName(name);
                            groupItem.setInfo(info.toString().trim());
                            groupItem.setDescription(description);
                            groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                            groupItemKeys.add(id);
                            groupItemValues.add(groupItem);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                listAdapter.notifyDataSetChanged();
                hasRequestedMore = false;
                footerLoading.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                relativeLayout.setVisibility(groupItemValues.isEmpty() ? View.VISIBLE : View.GONE);
                initFirebaseData();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                progressBar.setVisibility(View.GONE);
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
                params.put("display", String.valueOf(LIMIT));
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

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        fetchGroupListFromFirebase(databaseReference.orderByKey());
    }

    private void fetchGroupListFromFirebase(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    GroupItem value = snapshot.getValue(GroupItem.class);
                    assert value != null;
                    int index = groupItemKeys.indexOf(value.getId());
                    if (index > -1) {
                        //groupItemValues.set(index, value); //getInfo 구현이 덜되어 주석처리
                        groupItemKeys.set(index, key);
                    }
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "가져오기 실패", databaseError.toException());
            }
        });
    }

    private String groupIdExtract(String onclick) {
        return onclick.split("[(]|[)]|[,]")[1].trim();
    }
}
