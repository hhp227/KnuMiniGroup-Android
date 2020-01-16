package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.MemberListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.MemberItem;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberManagementFragment extends Fragment {
    private static String mGroupId;
    private List<MemberItem> mMemberItemList;
    private MemberListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public MemberManagementFragment() {
        // Required empty public constructor
    }

    public static MemberManagementFragment newInstance(String grpId) {
        MemberManagementFragment fragment = new MemberManagementFragment();
        Bundle args = new Bundle();
        args.putString("grp_id", grpId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroupId = getArguments().getString("grp_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_member, container, false);
        ListView listView = rootView.findViewById(R.id.lv_member);
        mSwipeRefreshLayout = rootView.findViewById(R.id.srl_member);
        mMemberItemList = new ArrayList<>();
        mAdapter = new MemberListAdapter(getContext(), mMemberItemList);

        listView.setAdapter(mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "새로고침", Toast.LENGTH_LONG).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1500);
            }
        });
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_MEMBER_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                List<Element> listZone = source.getElementById("listZone").getChildElements();
                for (Element element : listZone) {
                    List<Element> tdList = element.getAllElements(HTMLElementName.TD);
                    String studentNumber = tdList.get(0).getContent().getFirstElement().getAttributeValue("value");
                    String imageUrl = tdList.get(1).getContent().getFirstElement().getAttributeValue("src");
                    String uid = imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&ext"));
                    String name = tdList.get(2).getContent().toString();
                    String deptName = tdList.get(3).getTextExtractor().toString();
                    String division = tdList.get(5).getContent().toString();
                    String date = tdList.get(6).getContent().toString();

                    MemberItem memberItem = new MemberItem(uid, name, null, studentNumber, deptName, division, date);
                    mMemberItemList.add(memberItem);
                }
                mAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(error.getMessage());
                Toast.makeText(getContext(), "에러 : " + error.getMessage(), Toast.LENGTH_LONG).show();
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
                params.put("CLUB_GRP_ID", mGroupId);
                return params;
            }
        });
        return rootView;
    }
}
