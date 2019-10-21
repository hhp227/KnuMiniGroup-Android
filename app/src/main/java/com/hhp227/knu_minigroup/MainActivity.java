package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
    private static final String TAG = "소모임";
    private PreferenceManager preferenceManager;
    private GridView myGroupList;
    private GroupGridAdapter groupGridAdapter;
    private List<GroupItem> groupItems;
    private Button logout, createGroup;
    private Source source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout = findViewById(R.id.bLogout);
        createGroup = findViewById(R.id.bCreate);
        myGroupList = findViewById(R.id.grMyGroupList);

        preferenceManager = new PreferenceManager(getApplicationContext());
        groupItems = new ArrayList<>();
        groupGridAdapter = new GroupGridAdapter(getApplicationContext(), groupItems);
        myGroupList.setAdapter(groupGridAdapter);

        if(app.AppController.getInstance().getPreferenceManager().getUser() == null)
            logout();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                source = new Source(response);
                List<Element> list = source.getAllElements(HTMLElementName.A);
                for(int i = 0; i < list.size(); i++) {
                    Element elementA = list.get(i);
                    GroupItem groupItem = new GroupItem();
                    groupItem.setId(groupIdExtract(elementA.getAttributeValue("href")));
                    groupItem.setImage(EndPoint.BASE_URL + elementA.getFirstElement(HTMLElementName.IMG).getAttributeValue("src"));
                    groupItem.setName(elementA.getAllElements(HTMLElementName.DIV).get(3).getTextExtractor().toString());

                    groupItems.add(groupItem);
                }
                groupGridAdapter.notifyDataSetChanged();
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
        });

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateActivity.class));
            }
        });
    }

    private void logout() {
        preferenceManager.clear();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private int groupIdExtract(String href) {
        return Integer.parseInt(href.split("'")[1].trim());
    }
}
