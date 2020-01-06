package com.hhp227.knu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
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

import static android.app.Activity.RESULT_OK;

public class GroupFragment extends Fragment {
    public static final int CREATE_CODE = 10;
    public static final int REGISTER_CODE = 20;
    private long mLastClickTime; // 클릭시 걸리는 시간
    private GroupGridAdapter groupGridAdapter;
    private List<GroupItem> groupItems;
    private PreferenceManager preferenceManager;
    private ProgressBar progressBar;
    private RelativeLayout relativeLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Integer> groupItemIdList;
    private List<String> groupItemKeyList;

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
        GridView myGroupList = rootView.findViewById(R.id.gv_my_grouplist);
        progressBar = rootView.findViewById(R.id.pb_group);
        relativeLayout = rootView.findViewById(R.id.rl_group);
        swipeRefreshLayout = rootView.findViewById(R.id.srl_group);

        preferenceManager = new PreferenceManager(getActivity());
        groupItems = new ArrayList<>();
        groupItemIdList = new ArrayList<>();
        groupItemKeyList = new ArrayList<>();
        groupGridAdapter = new GroupGridAdapter(getContext(), groupItems);

        myGroupList.setAdapter(groupGridAdapter);
        myGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 두번 클릭시 방지
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
                    return;
                mLastClickTime = SystemClock.elapsedRealtime();
                GroupItem groupItem = groupItems.get(position);
                if (groupItem.isAd()) {
                    Toast.makeText(getContext(), "광고", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getContext(), GroupActivity.class);
                    intent.putExtra("admin", groupItem.isAdmin());
                    intent.putExtra("grp_id", groupItem.getId());
                    intent.putExtra("grp_nm", groupItem.getName());
                    startActivity(intent);
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        groupItems.clear();
                        fetchDataTask();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);

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

        progressBar.setVisibility(View.VISIBLE);

        fetchDataTask();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_CODE || requestCode == REGISTER_CODE) && resultCode == RESULT_OK) {
            groupItems.clear();
            fetchDataTask();
        }
    }

    private void fetchDataTask() {
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                List<Element> listElementA = source.getAllElements(HTMLElementName.A);
                for (Element elementA : listElementA) {
                    try {
                        int id = groupIdExtract(elementA.getAttributeValue("onclick"));
                        boolean isAdmin = adminCheck(elementA.getAttributeValue("onclick"));
                        String image = EndPoint.BASE_URL + elementA.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                        String name = elementA.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();

                        GroupItem groupItem = new GroupItem();
                        groupItem.setId(id);
                        groupItem.setAdmin(isAdmin);
                        groupItem.setImage(image);
                        groupItem.setName(name);

                        groupItems.add(groupItem);
                        groupItemIdList.add(groupItem.getId()); // id타입을 String 으로 변경후 지울예정
                        groupItemKeyList.add(String.valueOf(groupItem.getId()));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                groupGridAdapter.notifyDataSetChanged();
                insertAdvertisement();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
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
        preferenceManager.clear();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }

    private void insertAdvertisement() {
        setFirebaseData();
        if (groupItems.size() % 2 != 0) {
            GroupItem ad = new GroupItem();
            ad.setAd(true);
            ad.setName("광고 : 소모임앱이 출시되었습니다.");
            groupItems.add(ad);
        }
        progressBar.setVisibility(View.GONE);
        relativeLayout.setVisibility(groupItems.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        fetchDataTaskFromFirebase(databaseReference.child(app.AppController.getInstance().getPreferenceManager().getUser().getUid()).orderByChild("id"));
    }

    private void fetchDataTaskFromFirebase(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    GroupItem value = snapshot.getValue(GroupItem.class);
                    assert value != null;
                    if (groupItemIdList.indexOf(value.getId()) > -1) {
                        groupItemKeyList.set(groupItemIdList.indexOf(value.getId()), key);
                        groupItems.set(groupItemIdList.indexOf(value.getId()), value);
                    }
                }
                groupGridAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("GroupGridAdapter", "데이터 가져오기 실패", databaseError.toException());
            }
        });
    }

    private int groupIdExtract(String href) {
        return Integer.parseInt(href.split("'")[3].trim());
    }

    private boolean adminCheck(String onClick) {
        return onClick.split("'")[1].trim().equals("0");
    }
}
