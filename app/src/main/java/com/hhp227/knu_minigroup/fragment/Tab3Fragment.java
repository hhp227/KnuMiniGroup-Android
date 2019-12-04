package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.GridView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.MemberGridAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.MemberItem;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public class Tab3Fragment extends BaseFragment {
    public static int groupId;
    private static final int LIMIT = 40;
    private static final String TAG = "맴버목록";
    private ProgressDialog progressDialog;
    private GridView gridView;
    private MemberGridAdapter memberGridAdapter;
    private List<MemberItem> memberItems;

    public Tab3Fragment() {
    }

    public static Tab3Fragment newInstance(int grpId) {
        Tab3Fragment fragment = new Tab3Fragment();
        Bundle args = new Bundle();
        args.putInt("grp_id", grpId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getInt("grp_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab3, container, false);
        gridView = rootView.findViewById(R.id.gv_member);
        memberItems = new ArrayList<>();
        memberGridAdapter = new MemberGridAdapter(getActivity(), memberItems);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("불러오는중...");
        progressDialog.setCancelable(false);
        gridView.setAdapter(memberGridAdapter);
        showProgressDialog();
        String params = "?CLUB_GRP_ID=" + groupId + "&startM=" + "1" + "&displayM=" + LIMIT;
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.MEMBER_LIST + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                Element memberList = source.getElementById("member_list");
                List<Element> inputElements = memberList.getAllElements("name", "memberIdCheck", false);
                List<Element> imgElements = memberList.getAllElements("title", "프로필", false);
                List<Element> spanElements = memberList.getAllElements(HTMLElementName.SPAN);
                for (int i = 0; i < inputElements.size(); i++) {
                    String name = spanElements.get(i).getContent().toString();
                    String url = imgElements.get(i).getAttributeValue("src");
                    String value = inputElements.get(i).getAttributeValue("value");
                    memberItems.add(new MemberItem(name, EndPoint.BASE_URL + url.substring(0, url.lastIndexOf("&size")), value));
                }
                memberGridAdapter.notifyDataSetChanged();
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                hideProgressDialog();
            }
        }));

        return rootView;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return gridView != null && gridView.canScrollVertically(direction);
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
