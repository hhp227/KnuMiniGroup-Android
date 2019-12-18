package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.WebViewActivity;
import com.hhp227.knu_minigroup.adapter.BbsListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.BbsItem;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import java.util.ArrayList;
import java.util.List;

public class UnivNoticeFragment extends Fragment {
    private static final String TAG = "경북대공지사항";
    private ArrayList<BbsItem> ListData;
    private BbsListAdapter BBSAdapter;
    private Element BBS_DIV;
    private ListView BBSList;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Source source;

    private boolean hasRequestedMore = false; // 데이터 불러올때 중복안되게 하기위한 변수
    private boolean lastItemVisibleFlag = false;
    private int offSet;
    private int maxPage = 5; // 최대볼수 있는 페이지 수

    public UnivNoticeFragment() {
    }

    public static UnivNoticeFragment newInstance() {
        UnivNoticeFragment fragment = new UnivNoticeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        BBSList = rootView.findViewById(R.id.list_view);
        swipeRefreshLayout = rootView.findViewById(R.id.sr_layout);

        // 처음 offSet은 1이다, 파싱이 되는 동안 업데이트 될것
        offSet = 1;
        ListData = new ArrayList<>();

        BBSAdapter = new BbsListAdapter(getActivity(), ListData);
        progressDialog = new ProgressDialog(getActivity());

        BBSList.setAdapter(BBSAdapter);
        BBSList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BbsItem bbsItem = ListData.get(position);
                String URL_BBS = bbsItem.getUrl();
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.URL, EndPoint.URL_KNU + URL_BBS);
                startActivity(intent);
            }
        });

        // 리스트뷰 스크롤 리스너
        BBSList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && lastItemVisibleFlag && hasRequestedMore == false) {
                    // offSet이 maxPage이면 더 안보여줌
                    // 페이지 보기 제한 최대 maxPage까지 더보기 할수 있다.
                    if (offSet != maxPage) {
                        offSet++; // offSet 증가
                        hasRequestedMore = true; // HasRequestedMore가 true로 바뀌어 데이터를 불러온다
                        onLoadMoreItems();
                    } else {
                        hasRequestedMore = false;
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateListView();
                        swipeRefreshLayout.setRefreshing(false); // 당겨서 새로고침 숨김
                    }

                    private void updateListView() {
                        offSet = 1; // offSet 초기화
                        ListData = new ArrayList<>();
                        BBSAdapter = new BbsListAdapter(getActivity(), ListData);
                        BBSList.setAdapter(BBSAdapter);
                        fetchData();
                    }
                }, 1000);
            }
        });

        progressDialog.setCancelable(false);
        progressDialog.setMessage("게시판 정보 불러오는중...");

        fetchData();

        return rootView;
    }

    private void fetchData() {
        showProgressDialog();
        String tag_string_req = "req_knu_notice";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.URL_KNU_NOTICE.replace("{PAGE}", String.valueOf(offSet)), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                parseHTML(response);
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
                hideProgressDialog();
            }
        });
        app.AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    private void parseHTML(String response) {
        source = new Source(response);
        List<StartTag> tabletags = source.getAllStartTags(HTMLElementName.DIV);
        for (int i = 0; i < tabletags.size(); i++) {
            if (tabletags.get(i).toString().equals("<div class=\"board_list\">")) {
                BBS_DIV = source.getAllElements(HTMLElementName.DIV).get(i);
                break;
            }
        }

        try {
            for (Element BBS_TR : BBS_DIV.getAllElements(HTMLElementName.TBODY).get(0).getAllElements(HTMLElementName.TR)) {
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

                ListData.add(bbsItem);
            }
            BBSAdapter.notifyDataSetChanged();
            hasRequestedMore = false;
        } catch (Exception e) {
            Log.e(TAG, "에러" + e);
        }
    }

    /**
     * 리스트 하단으로 가면 더 불러오기
     */
    private void onLoadMoreItems() {
        fetchData();
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
