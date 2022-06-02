package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.adapter.MemberListAdapter;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.FragmentMemberBinding;
import com.hhp227.knu_minigroup.dto.MemberItem;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO
public class MemberManagementFragment extends Fragment {
    private static String mGroupId;

    private List<MemberItem> mMemberItemList;

    private MemberListAdapter mAdapter;

    private FragmentMemberBinding mBinding;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentMemberBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMemberItemList = new ArrayList<>();
        mAdapter = new MemberListAdapter(mMemberItemList);

        mBinding.lvMember.setAdapter(mAdapter);
        mBinding.srlMember.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "새로고침", Toast.LENGTH_LONG).show();
                        mBinding.srlMember.setRefreshing(false);
                    }
                }, 1500);
            }
        });
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_MEMBER_LIST, new Response.Listener<String>() {
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

                headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGroupId);
                return params;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
