package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
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

public class RequestActivity extends FragmentActivity {
    private static final int LIMIT = 100;
    private static final String TAG = RequestActivity.class.getSimpleName();
    private boolean mHasRequestedMore;
    private int mOffSet;
    private GroupListAdapter mAdapter;
    private List<String> mGroupItemKeys;
    private List<GroupItem> mGroupItemValues;
    private ProgressBar mProgressBar;
    private RelativeLayout mRelativeLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mFooterLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ActionBar actionBar = getActionBar();
        ListView listView = findViewById(R.id.lv_group);
        mFooterLoading = View.inflate(this, R.layout.load_more, null);
        mRelativeLayout = findViewById(R.id.rl_group);
        mProgressBar = findViewById(R.id.pb_group);
        mSwipeRefreshLayout = findViewById(R.id.srl_group_list);
        mGroupItemKeys = new ArrayList<>();
        mGroupItemValues = new ArrayList<>();
        mAdapter = new GroupListAdapter(getBaseContext(), mGroupItemKeys, mGroupItemValues);
        mOffSet = 1;
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
        mFooterLoading.setVisibility(View.GONE);
        listView.addFooterView(mFooterLoading);
        listView.setAdapter(mAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean lastItemVisibleFlag = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && lastItemVisibleFlag && !mHasRequestedMore) {
                    mFooterLoading.setVisibility(View.VISIBLE);
                    mOffSet += LIMIT;
                    mHasRequestedMore = true;
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
                GroupItem groupItem = mGroupItemValues.get(position);

                Bundle args = new Bundle();
                args.putString("grp_id", groupItem.getId());
                args.putString("grp_nm", groupItem.getName());
                args.putString("img", groupItem.getImage());
                args.putString("info", groupItem.getInfo());
                args.putString("desc", groupItem.getDescription());
                args.putString("type", groupItem.getJoinType());
                args.putInt("btn_type", 1);
                args.putString("key", mGroupItemKeys.get(position));

                GroupInfoFragment newFragment = GroupInfoFragment.newInstance();
                newFragment.setArguments(args);
                newFragment.show(getSupportFragmentManager(), "dialog");
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        mProgressBar.setVisibility(View.VISIBLE);
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
                            String joinType = menuList.getAllElementsByClass("info").get(1).getContent().toString();
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
                            groupItem.setInfo(info.toString());
                            groupItem.setDescription(description);
                            groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                            mGroupItemKeys.add(id);
                            mGroupItemValues.add(groupItem);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                mAdapter.notifyDataSetChanged();
                mHasRequestedMore = false;
                mFooterLoading.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                mRelativeLayout.setVisibility(mGroupItemValues.isEmpty() ? View.VISIBLE : View.GONE);
                initFirebaseData();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                mProgressBar.setVisibility(View.GONE);
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
                params.put("start", String.valueOf(mOffSet));
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

    public void refresh() {
        mOffSet = 1;
        mGroupItemKeys.clear();
        mGroupItemValues.clear();
        fetchGroupList();
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        fetchDataTaskFromFirebase(databaseReference.child(app.AppController.getInstance().getPreferenceManager().getUser().getUid()).orderByValue().equalTo(false), false);
    }

    private void fetchDataTaskFromFirebase(Query query, final boolean isRecursion) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isRecursion) {
                    try {
                        String key = dataSnapshot.getKey();
                        GroupItem value = dataSnapshot.getValue(GroupItem.class);
                        assert value != null;
                        int index = mGroupItemKeys.indexOf(value.getId());
                        if (index > -1) {
                            //mGroupItemValues.set(index, value); //isAdmin값때문에 주석처리
                            mGroupItemKeys.set(index, key);
                        }
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                        fetchDataTaskFromFirebase(databaseReference.child(snapshot.getKey()), true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    private String groupIdExtract(String onclick) {
        return onclick.split("'")[1].trim();
    }
}
