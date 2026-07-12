package com.hhp227.knu_minigroup.data;

import android.database.Cursor;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.TimetableItem;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.helper.TimetableHelper;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableRepository {
    private TimetableHelper mTimetableHelper;

    public void getTimetableList(Callback callback) {
        callback.onLoading();
        try {
            callback.onSuccess(fetchTimetableList());
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void addTimetable(int id, String subject, String classroom, Callback callback) {
        callback.onLoading();
        try {
            getTimetableHelper().add(id, subject, classroom);
            callback.onSuccess(fetchTimetableList());
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void updateTimetable(int id, String subject, String classroom, Callback callback) {
        callback.onLoading();
        try {
            getTimetableHelper().update(id, subject, classroom);
            callback.onSuccess(fetchTimetableList());
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void deleteTimetable(int id, Callback callback) {
        callback.onLoading();
        try {
            getTimetableHelper().delete(id);
            callback.onSuccess(fetchTimetableList());
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

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

    public void close() {
        if (mTimetableHelper != null) {
            mTimetableHelper.close();

            mTimetableHelper = null;
        }
    }

    private TimetableHelper getTimetableHelper() {
        if (mTimetableHelper == null) {
            mTimetableHelper = new TimetableHelper(AppController.getInstance());
        }
        return mTimetableHelper;
    }

    private List<TimetableItem> fetchTimetableList() {
        List<TimetableItem> timetableList = new ArrayList<>();
        Cursor cursor = getTimetableHelper().getAll();

        while (cursor.moveToNext()) {
            timetableList.add(new TimetableItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
        }
        cursor.close();
        return timetableList;
    }
}
