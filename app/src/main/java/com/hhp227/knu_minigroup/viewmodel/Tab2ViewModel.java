package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.helper.DateUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab2ViewModel extends ViewModel {
    public final List<Map<String, String>> mList = new ArrayList<>();

    private final MutableLiveData<State> mState = new MutableLiveData<>();

    private final MutableLiveData<Calendar> mCalendar = new MutableLiveData<>(Calendar.getInstance());

    private static final String TAG = Tab2ViewModel.class.getSimpleName();

    public LiveData<State> getState() {
        return mState;
    }

    public void previousMonth() {
        Calendar calendar = mCalendar.getValue();

        if (calendar != null) {
            if (calendar.get(Calendar.MONTH) == calendar.getActualMinimum(Calendar.MONTH)) {
                calendar.set((calendar.get(Calendar.YEAR) - 1), calendar.getActualMaximum(Calendar.MONTH),1);
            } else {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
            }
            mCalendar.postValue(calendar);
        }
    }

    public void nextMonth() {
        Calendar calendar = mCalendar.getValue();

        if (calendar != null) {
            if (calendar.get(Calendar.MONTH) == calendar.getActualMaximum(Calendar.MONTH)) {
                calendar.set((calendar.get(Calendar.YEAR) + 1), calendar.getActualMinimum(Calendar.MONTH),1);
            } else {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
            }
            mCalendar.postValue(calendar);
        }
    }

    public LiveData<Calendar> getCalendar() {
        return mCalendar;
    }

    public void fetchDataTask(Calendar calendar) {
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        String endPoint = EndPoint.URL_SCHEDULE.replace("{YEAR-MONTH}", year.concat(month));

        mState.postValue(new State(true, Collections.emptyList(), null));
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    List<Map<String, String>> list = new ArrayList<>();
                    Map<String, String> map = new HashMap<>();
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new StringReader(response));
                    int eventType = parser.getEventType();

                    list.add(new HashMap<>());
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                switch (parser.getName()) {
                                    case "entry":
                                        map = new HashMap<>();
                                        break;
                                    case "date":
                                        map.put("날짜", DateUtil.getCalendarStamp(parser.nextText()));
                                        break;
                                    case "title":
                                        try {
                                            map.put("내용", parser.nextText());
                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                        break;
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                if (parser.getName().equals("entry")) list.add(map);
                        }
                        eventType = parser.next();
                    }
                    mState.postValue(new State(false, list, null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null) {
                    VolleyLog.e(TAG, error.getMessage());
                    mState.postValue(new State(false, Collections.emptyList(), error.getMessage()));
                }
            }
        }));
    }

    public void addAll(List<Map<String, String>> list) {
        mList.clear();
        mList.addAll(list);
    }

    public static final class State {
        public boolean isLoading;

        public List<Map<String, String>> list;

        public String message;

        public State(boolean isLoading, List<Map<String, String>> list, String message) {
            this.isLoading = isLoading;
            this.list = list;
            this.message = message;
        }
    }
}
