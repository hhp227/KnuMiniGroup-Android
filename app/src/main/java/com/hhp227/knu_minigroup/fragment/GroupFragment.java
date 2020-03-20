package com.hhp227.knu_minigroup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.*;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.knu_minigroup.adapter.GroupPagerAdapter;
import com.hhp227.knu_minigroup.adapter.LoopPagerAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import com.hhp227.knu_minigroup.ui.loopviewpager.LoopViewPager;
import com.hhp227.knu_minigroup.ui.pageindicator.LoopingCirclePageIndicator;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class GroupFragment extends Fragment {
    public static final int CREATE_CODE = 10;
    public static final int REGISTER_CODE = 20;
    public static final int UPDATE_GROUP = 30;

    private static final int MARGIN = 120;
    private static final String TAG = GroupFragment.class.getSimpleName();
    private long mLastClickTime; // 클릭시 걸리는 시간
    private CountDownTimer mCountDownTimer;
    private GroupGridAdapter mAdapter;
    private List<String> mGroupItemKeys;
    private List<GroupItem> mGroupItemValues;
    private LoopViewPager mLoopViewPager;
    private LoopPagerAdapter mLoopPagerAdapter;
    private LoopingCirclePageIndicator mCirclePageIndicator;
    private PreferenceManager mPreferenceManager;
    private ProgressBar mProgressBar, mPopularProgressBar;
    private RelativeLayout mRelativeLayout;
    private List<GroupItem> mPopularItemList;
    private GroupPagerAdapter mGroupPagerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ViewPager mPopularViewPager;

    public GroupFragment() {
    }

    public static GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        Button findGroup = rootView.findViewById(R.id.b_find);
        Button requestGroup = rootView.findViewById(R.id.b_request);
        Button createGroup = rootView.findViewById(R.id.b_create);
        GridView gridView = rootView.findViewById(R.id.gv_my_grouplist);
        mProgressBar = rootView.findViewById(R.id.pb_group);
        mRelativeLayout = rootView.findViewById(R.id.rl_group);//
        mSwipeRefreshLayout = rootView.findViewById(R.id.srl_group);
        mPreferenceManager = new PreferenceManager(getActivity());
        mGroupItemKeys = new ArrayList<>();
        mGroupItemValues = new ArrayList<>();
        mAdapter = new GroupGridAdapter(getContext(), mGroupItemKeys, mGroupItemValues);
        mLoopViewPager = rootView.findViewById(R.id.lvp_theme_slider_pager);//
        mLoopPagerAdapter = new LoopPagerAdapter(Arrays.asList("이미지2", "메인", "이미지1"));//
        mCirclePageIndicator = rootView.findViewById(R.id.cpi_theme_slider_indicator);//
        mPopularViewPager = rootView.findViewById(R.id.view_pager);//
        mPopularProgressBar = rootView.findViewById(R.id.pb_group2);//
        mPopularItemList = new ArrayList<>();//
        mGroupPagerAdapter = new GroupPagerAdapter(mPopularItemList);//
        mCountDownTimer = new CountDownTimer(80000, 8000) {
            @Override
            public void onTick(long millisUntilFinished) {
                moveSliderPager();
            }

            @Override
            public void onFinish() {
                start();
            }
        };

        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 두번 클릭시 방지
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                GroupItem groupItem = mGroupItemValues.get(position);
                if (groupItem.isAd())
                    Toast.makeText(getContext(), "광고", Toast.LENGTH_LONG).show();
                else {
                    Intent intent = new Intent(getContext(), GroupActivity.class);

                    intent.putExtra("admin", groupItem.isAdmin());
                    intent.putExtra("grp_id", groupItem.getId());
                    intent.putExtra("grp_nm", groupItem.getName());
                    intent.putExtra("grp_img", groupItem.getImage());
                    intent.putExtra("pos", position);
                    intent.putExtra("key", mAdapter.getKey(position));
                    startActivityForResult(intent, UPDATE_GROUP);
                }
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mGroupItemKeys.clear();
                        mGroupItemValues.clear();
                        fetchDataTask();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        findGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();

                startActivityForResult(new Intent(getContext(), FindActivity.class), REGISTER_CODE);
            }
        });
        requestGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();

                startActivity(new Intent(getActivity(), RequestActivity.class));
            }
        });
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();

                startActivityForResult(new Intent(getContext(), CreateActivity.class), CREATE_CODE);
            }
        });
        if (app.AppController.getInstance().getPreferenceManager().getUser() == null)
            logout();
        showProgressBar();
        fetchDataTask();

        // 업데이트하면 지워질것
        mLoopViewPager.setAdapter(mLoopPagerAdapter);
        mCirclePageIndicator.setViewPager(mLoopViewPager);
        mLoopPagerAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.b_find:
                        startActivityForResult(new Intent(getContext(), FindActivity.class), REGISTER_CODE);
                        return;
                    case R.id.b_create:
                        startActivityForResult(new Intent(getContext(), CreateActivity.class), CREATE_CODE);
                }
            }
        });
        mPopularViewPager.setAdapter(mGroupPagerAdapter);
        mPopularViewPager.setClipToPadding(false);
        mPopularViewPager.setPadding(MARGIN, 0, MARGIN, 0);
        mPopularViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                if (mPopularViewPager.getCurrentItem() == 0) {
                    page.setTranslationX(-(MARGIN * 3) / 4);
                } else if (mPopularViewPager.getCurrentItem() == mGroupPagerAdapter.getCount() - 1) {
                    page.setTranslationX(MARGIN * 3 / 4);
                } else {
                    page.setTranslationX(-((MARGIN / 2) + (MARGIN / 8)));
                }
            }
        });
        mPopularViewPager.setPageMargin(MARGIN / 4);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCountDownTimer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        CountDownTimer countDownTimer = mCountDownTimer;
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_CODE || requestCode == REGISTER_CODE) && resultCode == Activity.RESULT_OK) {
            mGroupItemKeys.clear();
            mGroupItemValues.clear();
            fetchDataTask();
        } else if (requestCode == UPDATE_GROUP && resultCode == Activity.RESULT_OK && data != null) {//
            int position = data.getIntExtra("position", 0);
            GroupItem groupItem = mGroupItemValues.get(position);

            groupItem.setName(data.getStringExtra("grp_nm"));
            groupItem.setDescription(data.getStringExtra("grp_desc"));
            groupItem.setJoinType(data.getStringExtra("join_div"));
            mGroupItemValues.set(position, groupItem);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void fetchDataTask() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                try {
                    List<Element> listElementA = source.getAllElements(HTMLElementName.A);
                    for (Element elementA : listElementA) {
                        try {
                            String id = groupIdExtract(elementA.getAttributeValue("onclick"));
                            boolean isAdmin = adminCheck(elementA.getAttributeValue("onclick"));
                            String image = EndPoint.BASE_URL + elementA.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                            String name = elementA.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                            GroupItem groupItem = new GroupItem();

                            groupItem.setId(id);
                            groupItem.setAdmin(isAdmin);
                            groupItem.setImage(image);
                            groupItem.setName(name);
                            mGroupItemKeys.add(id);
                            mGroupItemValues.add(groupItem);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    insertAdvertisement();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    initFirebaseData();
                }
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

                headers.put("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("panel_id", "2");
                params.put("start", "1");
                params.put("display", "10");
                params.put("encoding", "utf-8");
                return params;
            }
        });
    }

    private void logout() {
        mPreferenceManager.clear();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }

    private void insertAdvertisement() {
        if (!mGroupItemValues.isEmpty()) {
            if (mGroupItemValues.size() % 2 != 0) {
                GroupItem ad = new GroupItem();

                ad.setAd(true);
                ad.setName("광고");
                mGroupItemValues.add(ad);
            }
            mRelativeLayout.setVisibility(View.GONE);
        } else {
            setNothingGroup();
            mRelativeLayout.setVisibility(View.VISIBLE);
        }
        hideProgressBar();
    }

    private void setNothingGroup() {
        mPopularProgressBar.setVisibility(View.VISIBLE);
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                List<Element> list = source.getAllElements("id", "accordion", false);
                for (Element element : list) {
                    try {
                        Element menuList = element.getFirstElementByClass("menu_list");
                        if (element.getAttributeValue("class").equals("accordion")) {
                            int id = Integer.parseInt(menuList.getFirstElementByClass("button").getAttributeValue("onclick").split("[(]|[)]|[,]")[1].trim());
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
                            groupItem.setId(String.valueOf(id));
                            groupItem.setImage(imageUrl);
                            groupItem.setName(name);
                            groupItem.setInfo(info.toString().trim());
                            groupItem.setDescription(description);
                            groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                            mPopularItemList.add(groupItem);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                mGroupPagerAdapter.notifyDataSetChanged();
                mPopularProgressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                mPopularProgressBar.setVisibility(View.GONE);
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
                params.put("panel_id", "3");
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

    private void moveSliderPager() {
        if (mLoopViewPager == null || mLoopPagerAdapter.getCount() <= 0) {
            return;
        }

        LoopViewPager loopViewPager = mLoopViewPager;
        loopViewPager.setCurrentItem(loopViewPager.getCurrentItem() + 1);
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");

        fetchDataTaskFromFirebase(databaseReference.child(mPreferenceManager.getUser().getUid()).orderByValue().equalTo(true), false);
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
                    } finally {
                        if (getActivity() != null)
                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                } else {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

                            fetchDataTaskFromFirebase(databaseReference.child(snapshot.getKey()), true);
                        }
                    } else
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    private String groupIdExtract(String href) {
        return href.split("'")[3].trim();
    }

    private boolean adminCheck(String onClick) {
        return onClick.split("'")[1].trim().equals("0");
    }

    private void showProgressBar() {
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.GONE);
    }
}
