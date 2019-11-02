package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
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

public class MainActivity extends Activity {
    public static final int CREATE_CODE = 10;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button logout, createGroup;
    private GridView myGroupList;
    private GroupGridAdapter groupGridAdapter;
    private List<GroupItem> groupItems;
    private PreferenceManager preferenceManager;
    private ProgressDialog progressDialog;
    private Source source;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout = findViewById(R.id.b_logout);
        createGroup = findViewById(R.id.b_create);
        myGroupList = findViewById(R.id.gr_my_grouplist);
        swipeRefreshLayout = findViewById(R.id.srl_group);

        progressDialog = new ProgressDialog(this);
        preferenceManager = new PreferenceManager(getApplicationContext());
        groupItems = new ArrayList<>();
        groupGridAdapter = new GroupGridAdapter(getApplicationContext(), groupItems);

        if (app.AppController.getInstance().getPreferenceManager().getUser() == null)
            logout();

        progressDialog.setCancelable(false);
        progressDialog.setMessage("불러오는중...");

        myGroupList.setAdapter(groupGridAdapter);
        myGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GroupItem groupItem = groupItems.get(position);
                Intent intent = new Intent(getApplicationContext(), GroupActivity.class);
                intent.putExtra("grp_id", groupItem.getId());
                intent.putExtra("grp_name", groupItem.getName());
                startActivity(intent);
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

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, CreateActivity.class), CREATE_CODE);
            }
        });

        showProgressDialog();

        fetchDataTask();
    }

    private void fetchDataTask() {
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                source = new Source(response);
                List<Element> listElementA = source.getAllElements(HTMLElementName.A);
                for (Element elementA : listElementA) {
                    GroupItem groupItem = new GroupItem();
                    groupItem.setId(groupIdExtract(elementA.getAttributeValue("onclick")));
                    groupItem.setImage(EndPoint.BASE_URL + elementA.getFirstElement(HTMLElementName.IMG).getAttributeValue("src"));
                    groupItem.setName(elementA.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString());

                    groupItems.add(groupItem);
                }
                groupGridAdapter.notifyDataSetChanged();
                hideProgressDialog();
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
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private int groupIdExtract(String href) {
        return Integer.parseInt(href.split("'")[3].trim());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_CODE && resultCode == RESULT_OK) {
            groupItems.clear();
            fetchDataTask();
        }
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
