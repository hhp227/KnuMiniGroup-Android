package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.calendar.Day;
import com.hhp227.knu_minigroup.calendar.ExtendedCalendarView;
import com.hhp227.knu_minigroup.ui.scrollable.BaseFragment;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tab2Fragment extends BaseFragment {
    private static final String TAG = "일정";
    private Calendar mCalendar;
    private ExtendedCalendarView mExtendedCalendarView;
    private HashMap<String, String> mMap;
    private List<HashMap<String, String>> mList;
    private ListView mListView;
    private SimpleAdapter mAdapter;

    public Tab2Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab2, container, false);
        View headerView = getLayoutInflater().inflate(R.layout.header_calendar, null, false);
        mExtendedCalendarView = headerView.findViewById(R.id.calendar);
        mListView = rootView.findViewById(R.id.lv_cal);
        mCalendar = Calendar.getInstance();

        mExtendedCalendarView.setOnDayClickListener(new ExtendedCalendarView.OnDayClickListener() {
            @Override
            public void onDayClicked(AdapterView<?> adapter, View view, int position, long id, Day day) {

            }
        });
        mExtendedCalendarView.prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExtendedCalendarView.previousMonth();
                if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMinimum(Calendar.MONTH)) {
                    mCalendar.set((mCalendar.get(Calendar.YEAR) - 1), mCalendar.getActualMaximum(Calendar.MONTH),1);
                } else {
                    mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1);
                }
                fetchDataTask();
            }
        });
        mExtendedCalendarView.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExtendedCalendarView.nextMonth();
                if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMaximum(Calendar.MONTH)) {
                    mCalendar.set((mCalendar.get(Calendar.YEAR) + 1), mCalendar.getActualMinimum(Calendar.MONTH),1);
                } else {
                    mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) + 1);
                }
                fetchDataTask();
            }
        });
        mListView.addHeaderView(headerView);
        fetchDataTask();
        return rootView;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mListView != null && mListView.canScrollVertically(direction);
    }

    private void fetchDataTask() {
        String year = String.valueOf(mCalendar.get(Calendar.YEAR));
        String month = String.format("%02d", mCalendar.get(Calendar.MONTH) + 1);

        String endPoint = EndPoint.URL_SCHEDULE.replace("{YEAR-MONTH}", year.concat(month));
        app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    mList = new ArrayList<>();
                    mAdapter = new SimpleAdapter(getContext(), mList, R.layout.schedule_item, new String[] {"날짜", "내용"}, new int[] {R.id.date, R.id.content});

                    mListView.setAdapter(mAdapter);
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new StringReader(response));
                    int eventType = parser.getEventType();

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG :
                                switch (parser.getName()) {
                                    case "entry" :
                                        mMap = new HashMap<>();
                                        break;
                                    case "date" :
                                        mMap.put("날짜", getTimeStamp(parser.nextText()));
                                        break;
                                    case "title" :
                                        try {
                                            mMap.put("내용", parser.nextText());
                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                        break;
                                }
                                break;
                            case XmlPullParser.END_TAG :
                                if (parser.getName().equals("entry"))
                                    mList.add(mMap);
                        }
                        eventType = parser.next();
                    }
                    mAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
            }
        }));
    }

    private String getTimeStamp(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = "";

        try {
            Date date = format.parse(dateStr);
            format = new SimpleDateFormat("dd");
            String date1 = format.format(date);
            timestamp = date1;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }
}
