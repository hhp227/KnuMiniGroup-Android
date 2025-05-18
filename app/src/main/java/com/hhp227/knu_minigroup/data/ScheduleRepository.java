package com.hhp227.knu_minigroup.data;

import android.text.TextUtils;
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

import java.util.*;

public class ScheduleRepository {
    public void getDCShuttleSchedule(Callback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.URL_SHUTTLE.replace("{SHUTTLE}", "map03"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                List<Map<String, String>> shuttleList = new ArrayList<>();
                Source source = new Source(response);

                for (int a = 0; a < 3; a++) {
                    Element table = source.getAllElements(HTMLElementName.TABLE).get(a);
                    List<Element> trs = table.getAllElements(HTMLElementName.TR);

                    for (int i = 0, j = -1; i < trs.size(); i++, j++) {
                        try {
                            Map<String, String> map1 = new HashMap<>();
                            Map<String, String> map2 = new HashMap<>();
                            Element col1 = trs.get(i).getAllElements(HTMLElementName.TH).get(0);

                            map1.put("col1", col1.getTextExtractor().toString());
                            if (i != 0) {
                                Element col2 = trs.get(i).getAllElements(HTMLElementName.TD).get(0);
                                map1.put("col2", col2.getTextExtractor().toString());
                                shuttleList.add(a == 0 ? j : shuttleList.size(), map1);
                                col1 = trs.get(i).getAllElements(HTMLElementName.TH).get(1);
                                col2 = trs.get(i).getAllElements(HTMLElementName.TD).get(1);
                                map2.put("col1", col1.getTextExtractor().toString());
                                map2.put("col2", col2.getTextExtractor().toString());
                                if (!TextUtils.isEmpty(col1.getTextExtractor().toString()) || !TextUtils.isEmpty(col2.getTextExtractor().toString()))
                                    shuttleList.add(shuttleList.size(), map2);
                            } else
                                shuttleList.add(a == 0 ? i : shuttleList.size(), map1);
                        } catch (Exception e) {
                            callback.onFailure(e);
                        }
                    }
                }
                callback.onSuccess(shuttleList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        });

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public void getSCShuttleSchedule(Callback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.URL_SHUTTLE.replace("{SHUTTLE}", "map03_02"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ArrayList<HashMap<String, String>> shuttleList = new ArrayList<>();
                Source source = new Source(response);
                Element table = source.getAllElements(HTMLElementName.TABLE).get(0);
                List<Element> thList = table.getFirstElement(HTMLElementName.TR).getAllElements(HTMLElementName.TH);
                List<String> result = new ArrayList<>();

                for (int i = 1; i < table.getAllElements(HTMLElementName.TR).size(); i++) {
                    Element TR = table.getAllElements(HTMLElementName.TR).get(i);
                    HashMap<String, String> map = new HashMap<>();
                    Element Col1 = TR.getAllElements(HTMLElementName.TD).get(0);
                    Element Col2 = TR.getAllElements(HTMLElementName.TD).get(1);
                    Element Col3 = TR.getAllElements(HTMLElementName.TD).get(2);
                    Element Col4 = TR.getAllElements(HTMLElementName.TD).get(3);
                    Element Col5 = TR.getAllElements(HTMLElementName.TD).get(4);
                    Element Col6 = TR.getAllElements(HTMLElementName.TD).get(5);

                    map.put("col1", String.valueOf(i));
                    map.put("col2", (Col1).getContent().toString());
                    map.put("col3", (Col2).getContent().toString());
                    map.put("col4", (Col3).getContent().toString());
                    map.put("col5", (Col4).getContent().toString());
                    map.put("col6", (Col5).getContent().toString());
                    map.put("col7", (Col6).getContent().toString());
                    shuttleList.add(map);
                }
                for (Element element : thList) {
                    result.add(element.getTextExtractor().toString());
                }
                callback.onSuccess(new AbstractMap.SimpleEntry<>(shuttleList, result));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        });

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }
}
