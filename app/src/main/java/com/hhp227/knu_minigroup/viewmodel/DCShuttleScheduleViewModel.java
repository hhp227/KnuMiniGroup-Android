package com.hhp227.knu_minigroup.viewmodel;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DCShuttleScheduleViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>(new State(true, false, null));

    public final List<Map<String, String>> mShuttleList = new ArrayList<>();

    public DCShuttleScheduleViewModel() {
        fetchDataTask();
    }

    public void refresh() {
        mShuttleList.clear();
        fetchDataTask();
    }

    private void fetchDataTask() {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.URL_SHUTTLE.replace("{SHUTTLE}", "map03"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
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
                                mShuttleList.add(a == 0 ? j : mShuttleList.size(), map1);
                                col1 = trs.get(i).getAllElements(HTMLElementName.TH).get(1);
                                col2 = trs.get(i).getAllElements(HTMLElementName.TD).get(1);
                                map2.put("col1", col1.getTextExtractor().toString());
                                map2.put("col2", col2.getTextExtractor().toString());
                                if (!TextUtils.isEmpty(col1.getTextExtractor().toString()) || !TextUtils.isEmpty(col2.getTextExtractor().toString()))
                                    mShuttleList.add(mShuttleList.size(), map2);
                            } else
                                mShuttleList.add(a == 0 ? i : mShuttleList.size(), map1);
                        } catch (Exception e) {
                            mState.postValue(new State(false, false, e.getMessage()));
                        }
                    }
                }
                mState.postValue(new State(false, true, null));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, false, error.getMessage()));
            }
        }));
    }

    public static final class State {
        public boolean isLoading;

        public boolean isSuccess;

        public String message;

        public State(boolean isLoading, boolean isSuccess, String message) {
            this.isLoading = isLoading;
            this.isSuccess = isSuccess;
            this.message = message;
        }
    }
}
