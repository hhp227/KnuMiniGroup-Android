package com.hhp227.knu_minigroup.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.snackbar.Snackbar;
import com.hhp227.knu_minigroup.MainActivity;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.WebViewActivity;
import com.hhp227.knu_minigroup.adapter.BbsListAdapter;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.FragmentListBinding;
import com.hhp227.knu_minigroup.dto.BbsItem;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import java.util.ArrayList;
import java.util.List;

public class UnivNoticeFragment extends Fragment {
    private static final int MAX_PAGE = 10; // 최대볼수 있는 페이지 수
    private static final String TAG = "경북대공지사항";

    private boolean mHasRequestedMore; // 데이터 불러올때 중복안되게 하기위한 변수

    private int mOffSet;

    private AppCompatActivity mActivity;

    private ArrayList<BbsItem> mBbsItemList;

    private BbsListAdapter mAdapter;

    private Element mBBS_DIV;

    private RecyclerView.OnScrollListener mOnScrollListener;

    private FragmentListBinding mBinding;

    public UnivNoticeFragment() {
    }

    public static UnivNoticeFragment newInstance() {
        return new UnivNoticeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        mActivity = (AppCompatActivity) getActivity();
        mBbsItemList = new ArrayList<>();
        mAdapter = new BbsListAdapter(mBbsItemList);
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mHasRequestedMore && !recyclerView.canScrollVertically(1)) {
                    if (mOffSet != MAX_PAGE) {
                        mHasRequestedMore = true;

                        mOffSet++; // offSet 증가
                        fetchDataList();
                        Snackbar.make(recyclerView, "게시판 정보 불러오는 중...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    } else
                        mHasRequestedMore = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };

        // 처음 offSet은 1이다, 파싱이 되는 동안 업데이트 될것
        mOffSet = 1;

        mActivity.setTitle(getString(R.string.knu_board));
        mActivity.setSupportActionBar(mBinding.toolbar);
        setDrawerToggle();
        mBinding.srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mOffSet = 1; // offSet 초기화

                        mBbsItemList.clear();
                        mBinding.srl.setRefreshing(false);
                        fetchDataList();
                    }
                }, 1000);
            }
        });
        mBinding.recyclerView.addOnScrollListener(mOnScrollListener);
        mBinding.recyclerView.setLayoutManager(linearLayoutManager);
        mBinding.recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BbsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                BbsItem bbsItem = mBbsItemList.get(position);
                Intent intent = new Intent(v.getContext(), WebViewActivity.class);

                intent.putExtra(WebViewActivity.URL, EndPoint.URL_KNU + bbsItem.getUrl());
                startActivity(intent);
            }
        });
        showProgressBar();
        fetchDataList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mOnScrollListener != null)
            mBinding.recyclerView.removeOnScrollListener(mOnScrollListener);
        mOnScrollListener = null;
        mBinding = null;
    }

    private void setDrawerToggle() {
        DrawerLayout drawerLayout = ((MainActivity) mActivity).mBinding.drawerLayout;
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(mActivity, drawerLayout, mBinding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void fetchDataList() {
        showProgressBar();
        String tag_string_req = "req_knu_notice";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.URL_KNU_NOTICE.replace("{PAGE}", String.valueOf(mOffSet)), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseHTML(response);
                hideProgressBar();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null)
                    Log.e(TAG, error.getMessage());
                hideProgressBar();
            }
        });

        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    private void parseHTML(String response) {
        Source source = new Source(response);
        List<StartTag> tabletags = source.getAllStartTags(HTMLElementName.DIV);

        for (int i = 0; i < tabletags.size(); i++) {
            if (tabletags.get(i).toString().equals("<div class=\"board_list\">")) {
                mBBS_DIV = source.getAllElements(HTMLElementName.DIV).get(i);
                break;
            }
        }
        try {
            for (Element BBS_TR : mBBS_DIV.getAllElements(HTMLElementName.TBODY).get(0).getAllElements(HTMLElementName.TR)) {
                BbsItem bbsItem = new BbsItem();
                Element BC_TYPE = BBS_TR.getAllElements(HTMLElementName.TD).get(0); // 타입 을 불러온다.
                Element BC_info = BBS_TR.getAllElements(HTMLElementName.TD).get(1); // URL(herf) TITLE(title) 을 담은 정보를 불러온다.
                Element BC_a = BC_info.getAllElements(HTMLElementName.A).get(0); // BC_info 안의 a 태그를 가져온다.
                Element BC_writer = BBS_TR.getAllElements(HTMLElementName.TD).get(3); // 글쓴이를 불러온다.
                Element BC_date = BBS_TR.getAllElements(HTMLElementName.TD).get(4); // 날짜를 불러온다.

                bbsItem.setType(BC_TYPE.getContent().toString()); // 타입값을 담은 엘레먼트의 컨텐츠를 문자열로 변환시켜 가져온다.
                bbsItem.setTitle(BC_a.getTextExtractor().toString()); // a 태그의 title 은 BCS_title 로 선언
                bbsItem.setUrl(BC_a.getAttributeValue("href")); // a 태그의 herf 는 BCS_url 로 선언
                bbsItem.setWriter(BC_writer.getContent().toString()); // 작성자값을 담은 엘레먼트의 컨텐츠를 문자열로 변환시켜 가져온다.
                bbsItem.setDate(BC_date.getContent().toString()); // 작성일자값을 담은 엘레먼트의 컨텐츠를 문자열로 변환시켜 가져온다.
                mBbsItemList.add(bbsItem);
            }
            mAdapter.notifyDataSetChanged();
            mHasRequestedMore = false;
        } catch (Exception e) {
            Log.e(TAG, "에러" + e);
        }
    }

    private void showProgressBar() {
        if (mBinding.progressCircular.getVisibility() == View.GONE)
            mBinding.progressCircular.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.progressCircular.getVisibility() == View.VISIBLE)
            mBinding.progressCircular.setVisibility(View.GONE);
    }
}
