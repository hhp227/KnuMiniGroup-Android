package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.calendar.ExtendedCalendarView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tab2Fragment extends Fragment {
    private static final int TYPE_CALENDAR = 0;
    private static final int TYPE_ITEM = 1;
    private static final String TAG = "일정";
    private Calendar mCalendar;
    private RecyclerView.Adapter mAdapter;
    private List<Map<String, String>> mList;

    public Tab2Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.rv_cal);
        mList = new ArrayList<>();
        mCalendar = Calendar.getInstance();
        mAdapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                if (viewType == TYPE_CALENDAR) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.header_calendar, parent, false);
                    return new HeaderHolder(view);
                } else if (viewType == TYPE_ITEM) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.schedule_item, parent, false);
                    return new ItemHolder(view);
                }
                throw new RuntimeException();
            }

            @Override
            public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof HeaderHolder) {
                    ((HeaderHolder) holder).extendedCalendarView.prev.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HeaderHolder) holder).extendedCalendarView.previousMonth();
                            if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMinimum(Calendar.MONTH))
                                mCalendar.set((mCalendar.get(Calendar.YEAR) - 1), mCalendar.getActualMaximum(Calendar.MONTH),1);
                            else
                                mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1);
                            fetchDataTask();
                        }
                    });
                    ((HeaderHolder) holder).extendedCalendarView.next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HeaderHolder) holder).extendedCalendarView.nextMonth();
                            if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMaximum(Calendar.MONTH))
                                mCalendar.set((mCalendar.get(Calendar.YEAR) + 1), mCalendar.getActualMinimum(Calendar.MONTH),1);
                            else
                                mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) + 1);
                            fetchDataTask();
                        }
                    });
                } else if (holder instanceof ItemHolder) {
                    Map<String, String> calItem = mList.get(position);
                    ((ItemHolder) holder).date.setText(calItem.get("날짜"));
                    ((ItemHolder) holder).content.setText(calItem.get("내용"));
                }
            }

            @Override
            public int getItemCount() {
                return mList.size();
            }

            @Override
            public int getItemViewType(int position) {
                return position == 0 ? TYPE_CALENDAR : TYPE_ITEM;
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        fetchDataTask();
    }

    private void fetchDataTask() {
        String year = String.valueOf(mCalendar.get(Calendar.YEAR));
        String month = String.format("%02d", mCalendar.get(Calendar.MONTH) + 1);

        String endPoint = EndPoint.URL_SCHEDULE.replace("{YEAR-MONTH}", year.concat(month));
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Map<String, String> map = new HashMap<>();
                    mList.clear();
                    addHeaderView();
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(new StringReader(response));
                    int eventType = parser.getEventType();

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                switch (parser.getName()) {
                                    case "entry":
                                        map = new HashMap<>();
                                        break;
                                    case "date":
                                        map.put("날짜", getTimeStamp(parser.nextText()));
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
                                if (parser.getName().equals("entry"))
                                    mList.add(map);
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

    public void addHeaderView() {
        mList.add(new HashMap<String, String>());
    }

    public static class HeaderHolder extends RecyclerView.ViewHolder {
        public ExtendedCalendarView extendedCalendarView;

        public HeaderHolder(View itemView) {
            super(itemView);
            extendedCalendarView = itemView.findViewById(R.id.calendar);
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private TextView date, content;

        public ItemHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            content = itemView.findViewById(R.id.content);
        }
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
