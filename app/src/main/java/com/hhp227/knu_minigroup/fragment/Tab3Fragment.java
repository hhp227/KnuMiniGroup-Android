package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AbsListView;
import android.widget.AdapterView;
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
    private boolean hasRequestedMore;
    private int offSet;

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
        offSet = 1;
        progressDialog.setMessage("불러오는중...");
        progressDialog.setCancelable(false);
        gridView.setAdapter(memberGridAdapter);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean lastItemVisibleFlag = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && lastItemVisibleFlag && hasRequestedMore == false) {
                    offSet += LIMIT;
                    hasRequestedMore = true;
                    fetchMemberList();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItemVisibleFlag = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MemberItem memberItem = memberItems.get(position);
                String name = memberItem.name;
                String image = memberItem.imageId;
                String value = memberItem.value;

                Bundle args = new Bundle();
                args.putString("name", name);
                args.putString("image", image);
                args.putString("value", value);

                UserFragment newFragment = UserFragment.newInstance();
                newFragment.setArguments(args);
                newFragment.show(getChildFragmentManager(), "dialog");
            }
        });
        showProgressDialog();
        fetchMemberList();

        return rootView;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return gridView != null && gridView.canScrollVertically(direction);
    }

    private void fetchMemberList() {
        String params = "?CLUB_GRP_ID=" + groupId + "&startM=" + offSet + "&displayM=" + LIMIT;
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.MEMBER_LIST + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Source source = new Source(response);
                    Element memberList = source.getElementById("member_list");
                    // 페이징 처리
                    String page = memberList.getFirstElementByClass("paging").getFirstElement("title", "현재 선택 목록", false).getTextExtractor().toString();
                    List<Element> inputElements = memberList.getAllElements("name", "memberIdCheck", false);
                    List<Element> imgElements = memberList.getAllElements("title", "프로필", false);
                    List<Element> spanElements = memberList.getAllElements(HTMLElementName.SPAN);

                    for (int i = 0; i < inputElements.size(); i++) {
                        String name = spanElements.get(i).getContent().toString();
                        String imageUrl = imgElements.get(i).getAttributeValue("src");
                        String value = inputElements.get(i).getAttributeValue("value");
                        memberItems.add(new MemberItem(name, imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&ext")), value));
                    }
                    memberGridAdapter.notifyDataSetChanged();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                hasRequestedMore = false;
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                hideProgressDialog();
            }
        }));
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
