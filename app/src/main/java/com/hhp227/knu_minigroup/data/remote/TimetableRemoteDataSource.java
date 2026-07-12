package com.hhp227.knu_minigroup.data.remote;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.helper.Callback;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableRemoteDataSource {
    /*학기 시간표 페이지를 파싱하여 행 단위의 텍스트 리스트로 반환, 첫번째 행은 요일 헤더*/
    public void getSemesterTimetableList(String cookie, Callback callback) {
        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.TIMETABLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    List<List<String>> table = new ArrayList<>();
                    Element timeTable = new Source(response).getFirstElementByClass("bbslist");
                    List<Element> trList = timeTable.getAllElements(HTMLElementName.TR);

                    for (int i = 0; i < trList.size(); i++) {
                        if (i == 1) {
                            continue;
                        }
                        List<String> row = new ArrayList<>();

                        for (Element element : trList.get(i).getChildElements()) {
                            row.add(element.getTextExtractor().toString());
                        }
                        table.add(row);
                    }
                    callback.onSuccess(table);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }
        });
    }
}
