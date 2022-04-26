package com.hhp227.knu_minigroup.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.adapter.GroupListAdapter;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.ActivityListBinding;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.fragment.GroupInfoFragment;
import com.hhp227.knu_minigroup.viewmodel.FindGroupViewModel;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO
public class FindGroupActivity extends AppCompatActivity {
    private static final int LIMIT = 15;
    private static final String TAG = FindGroupActivity.class.getSimpleName();

    private boolean mHasRequestedMore;

    private int mOffSet, mMinId;

    private GroupListAdapter mAdapter;

    private List<String> mGroupItemKeys;

    private List<GroupItem> mGroupItemValues;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private ActivityListBinding mBinding;

    private FindGroupViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityListBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(FindGroupViewModel.class);
        mGroupItemKeys = new ArrayList<>();
        mGroupItemValues = new ArrayList<>();
        mAdapter = new GroupListAdapter(this, mGroupItemKeys, mGroupItemValues);
        mOffSet = 1;
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!mHasRequestedMore && dy > 0 && manager != null && manager.findLastCompletelyVisibleItemPosition() >= manager.getItemCount() - 1) {
                    mHasRequestedMore = true;
                    mOffSet += LIMIT;

                    mAdapter.setFooterProgressBarVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                    fetchGroupList();
                }
            }
        };

        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mBinding.recyclerView.setHasFixedSize(true);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.recyclerView.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
                mAdapter.addFooterView();
                mAdapter.setButtonType(GroupInfoFragment.TYPE_REQUEST);
            }
        });
        mBinding.recyclerView.addOnScrollListener(mOnScrollListener);
        mBinding.srlList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMinId = 0;
                        mOffSet = 1;

                        mGroupItemKeys.clear();
                        mGroupItemValues.clear();
                        mAdapter.addFooterView();
                        mBinding.srlList.setRefreshing(false);
                        fetchGroupList();
                    }
                }, 1000);
            }
        });
        showProgressBar();
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchGroupList();
            }
        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOnScrollListener != null)
            mBinding.recyclerView.removeOnScrollListener(mOnScrollListener);
        mOnScrollListener = null;

        mBinding.sflGroup.clearAnimation();
        mBinding.sflGroup.removeAllViews();
        mBinding = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void fetchGroupList() {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                List<Element> list = source.getAllElements("id", "accordion", false);

                for (Element element : list) {
                    try {
                        Element menuList = element.getFirstElementByClass("menu_list");

                        if (element.getAttributeValue("class").equals("accordion")) {
                            int id = groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"));
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
                            mMinId = mMinId == 0 ? id : Math.min(mMinId, id);

                            if (id > mMinId) {
                                mHasRequestedMore = true;
                                break;
                            } else
                                mHasRequestedMore = false;
                            GroupItem groupItem = new GroupItem();

                            groupItem.setId(String.valueOf(id));
                            groupItem.setImage(imageUrl);
                            groupItem.setName(name);
                            groupItem.setInfo(info.toString().trim());
                            groupItem.setDescription(description);
                            groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                            mGroupItemKeys.add(mGroupItemKeys.size() - 1, String.valueOf(id));
                            mGroupItemValues.add(mGroupItemValues.size() - 1, groupItem);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        initFirebaseData();
                    }
                }
                mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
                mAdapter.notifyDataSetChanged();
                hideProgressBar();
                mBinding.text.setText("그룹이 없습니다.");
                mBinding.rlGroup.setVisibility(mGroupItemValues.size() > 1 ? View.GONE : View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                hideProgressBar();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
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

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        fetchGroupListFromFirebase(databaseReference.orderByKey());
    }

    private void fetchGroupListFromFirebase(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    GroupItem value = snapshot.getValue(GroupItem.class);
                    assert value != null;
                    int index = mGroupItemKeys.indexOf(value.getId());

                    if (index > -1) {
                        //mGroupItemValues.set(index, value); //getInfo 구현이 덜되어 주석처리
                        mGroupItemKeys.set(index, key);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "가져오기 실패", databaseError.toException());
            }
        });
    }

    private int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("[(]|[)]|[,]")[1].trim());
    }

    private void showProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.GONE)
            mBinding.pbGroup.setVisibility(View.VISIBLE);
        if (!mBinding.sflGroup.isShimmerStarted())
            mBinding.sflGroup.startShimmer();
        if (!mBinding.sflGroup.isShimmerVisible())
            mBinding.sflGroup.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.VISIBLE)
            mBinding.pbGroup.setVisibility(View.GONE);
        if (mBinding.sflGroup.isShimmerStarted())
            mBinding.sflGroup.stopShimmer();
        if (mBinding.sflGroup.isShimmerVisible())
            mBinding.sflGroup.setVisibility(View.GONE);
    }
}
