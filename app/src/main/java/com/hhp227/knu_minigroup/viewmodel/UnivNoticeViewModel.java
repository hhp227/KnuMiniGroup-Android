package com.hhp227.knu_minigroup.viewmodel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.BbsItem;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class UnivNoticeViewModel extends ListViewModel<BbsItem> {
    private static final int MAX_PAGE = 10;

    private Element mBBS_DIV;

    public UnivNoticeViewModel() {
        fetchNextPage();
    }

    public void fetchNextPage() {
        if (getOffset() < MAX_PAGE) {
            setRequestMore(true);
            fetchDataList(getOffset());
        }
    }

    public void refresh() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                setItemList(Collections.emptyList());
                setOffset(1);
                setRequestMore(true);
                setEndReached(false);
                fetchDataList(getOffset());
            }
        });
    }

    public void fetchDataList(int offset) {
        String tag_string_req = "req_knu_notice";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.URL_KNU_NOTICE.replace("{PAGE}", String.valueOf(offset)), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setLoading(false);
                setItemList(mergedList(getItemList().getValue(), parseHTML(response)));
                setOffset(getOffset() + 1);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setLoading(false);
                setMessage(error.getMessage());
            }
        });

        setLoading(true);
        setRequestMore(offset > 1);
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    private List<BbsItem> parseHTML(String response) {
        Source source = new Source(response);
        List<StartTag> tableTags = source.getAllStartTags(HTMLElementName.DIV);
        List<BbsItem> itemList = new ArrayList<>();

        for (int i = 0; i < tableTags.size(); i++) {
            if (tableTags.get(i).toString().equals("<div class=\"board_list\">")) {
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
                itemList.add(bbsItem);
            }
        } catch (Exception e) {
            setLoading(false);
            setMessage(e.getMessage());
        }
        return itemList;
    }

    private List<BbsItem> mergedList(List<BbsItem> existingList, List<BbsItem> newList) {
        return new ArrayList<BbsItem>() {
            {
                addAll(existingList);
                addAll(newList);
            }
        };
    }
}
