package com.hhp227.knu_minigroup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.*;
import com.hhp227.knu_minigroup.*;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.*;

public class GroupFragment extends Fragment {
    public static final int CREATE_CODE = 10;
    public static final int REGISTER_CODE = 20;
    public static final int UPDATE_GROUP = 30;

    private static final String TAG = GroupFragment.class.getSimpleName();
    private long mLastClickTime; // 클릭시 걸리는 시간
    private GroupGridAdapter mAdapter;
    private List<String> mGroupItemKeys;
    private List<GroupItem> mGroupItemValues;
    private PreferenceManager mPreferenceManager;
    private ProgressBar mProgressBar;
    private RelativeLayout mRelativeLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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
        mRelativeLayout = rootView.findViewById(R.id.rl_group);
        mSwipeRefreshLayout = rootView.findViewById(R.id.srl_group);
        mPreferenceManager = new PreferenceManager(getActivity());
        mGroupItemKeys = new ArrayList<>();
        mGroupItemValues = new ArrayList<>();
        mAdapter = new GroupGridAdapter(getContext(), mGroupItemKeys, mGroupItemValues);

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

        return rootView;
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
        if (mGroupItemValues.size() % 2 != 0) {
            GroupItem ad = new GroupItem();

            ad.setAd(true);
            ad.setName("광고");
            mGroupItemValues.add(ad);
        }
        hideProgressBar();
        mRelativeLayout.setVisibility(mGroupItemValues.isEmpty() ? View.VISIBLE : View.GONE);
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
