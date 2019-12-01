package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.*;
import com.hhp227.knu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class GroupFragment extends Fragment {
    public static final int CREATE_CODE = 10;
    public static final int REGISTER_CODE = 20;
    public static final int DELETE_CODE = 30;
    private Button findGroup, requestGroup, createGroup;
    private GridView myGroupList;
    private GroupGridAdapter groupGridAdapter;
    private List<GroupItem> groupItems;
    private PreferenceManager preferenceManager;
    private ProgressDialog progressDialog;
    private Source source;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long mLastClickTime; // 클릭시 걸리는 시간

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
        findGroup = rootView.findViewById(R.id.b_find);
        requestGroup = rootView.findViewById(R.id.b_request);
        createGroup = rootView.findViewById(R.id.b_create);
        myGroupList = rootView.findViewById(R.id.gv_my_grouplist);
        swipeRefreshLayout = rootView.findViewById(R.id.srl_group);

        progressDialog = new ProgressDialog(getContext());
        preferenceManager = new PreferenceManager(getActivity());
        groupItems = new ArrayList<>();
        groupGridAdapter = new GroupGridAdapter(getContext(), groupItems);

        progressDialog.setCancelable(false);
        progressDialog.setMessage("불러오는중...");

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
                    startActivityForResult(intent, DELETE_CODE);
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

        showProgressDialog();

        fetchDataTask();

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_CODE || requestCode == REGISTER_CODE || requestCode == DELETE_CODE) && resultCode == RESULT_OK) {
            groupItems.clear();
            fetchDataTask();
        }
    }

    private void fetchDataTask() {
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                source = new Source(response);
                List<Element> listElementA = source.getAllElements(HTMLElementName.A);
                for (Element elementA : listElementA) {
                    try {
                        GroupItem groupItem = new GroupItem();
                        groupItem.setId(groupIdExtract(elementA.getAttributeValue("onclick")));
                        groupItem.setAdmin(adminCheck(elementA.getAttributeValue("onclick")));
                        groupItem.setImage(EndPoint.BASE_URL + elementA.getFirstElement(HTMLElementName.IMG).getAttributeValue("src"));
                        groupItem.setName(elementA.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString());

                        groupItems.add(groupItem);
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
        if (groupItems.size() % 2 != 0) {
            GroupItem ad = new GroupItem();
            ad.setAd(true);
            ad.setName("광고");
            groupItems.add(ad);
        }
        hideProgressDialog();
    }

    private int groupIdExtract(String href) {
        return Integer.parseInt(href.split("'")[3].trim());
    }

    private boolean adminCheck(String onClick) {
        return onClick.split("'")[1].trim().equals("0");
    }

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
