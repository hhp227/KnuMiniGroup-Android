package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SCShuttleScheduleFragment extends Fragment {
    private static final String TAG = "학교버스시간표";
    private SwipeRefreshLayout SWPRefresh;
    private Handler handler;
    private Source source;
    private ArrayList<HashMap<String, String>> data;
    private ProgressDialog progressDialog;
    private ListView ShuttleList;
    private SimpleAdapter ShuttleAdapter;

    public static SCShuttleScheduleFragment newInstance() {
        SCShuttleScheduleFragment fragment = new SCShuttleScheduleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_shuttle_schedule_sc, container, false);
        ShuttleList = rootView.findViewById(R.id.lv_shuttle);
        SWPRefresh = rootView.findViewById(R.id.srl_shuttle);
        data = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());

        ShuttleAdapter = new SimpleAdapter(getActivity(), data, R.layout.shuttle_sc_item,
                new String[] {"col1", "col2", "col3", "col4", "col5", "col6"},
                new int[] {R.id.column1, R.id.column2, R.id.column3, R.id.column4, R.id.column5, R.id.column6});

        ShuttleList.setAdapter(ShuttleAdapter);
        SWPRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        SWPRefresh.setRefreshing(false); // 당겨서 새로고침 숨김
                    }
                }, 1000);
            }
        });
        progressDialog.setMessage("불러오는중...");
        progressDialog.show();

        try {
            new Thread() {
                public void run() {
                    try {
                        URL URL = new URL(EndPoint.URL_SHUTTLE);
                        InputStream html = URL.openStream();
                        source = new Source(new InputStreamReader(html, "utf-8")); // 소스를 UTF-8 인코딩으로 불러온다.
                        source.fullSequentialParse(); // 순차적으로 구문분석
                    } catch (Exception e) {
                        Log.e(TAG, "에러" + e);
                    }
                    Element table = source.getAllElements(HTMLElementName.TABLE).get(0);

                    for (int i = 1; i < table.getAllElements(HTMLElementName.TR).size(); i++) {
                        Element TR = table.getAllElements(HTMLElementName.TR).get(i);
                        HashMap<String, String> map = new HashMap<>();

                        Element Col1 = TR.getAllElements(HTMLElementName.TD).get(0);
                        Element Col2 = TR.getAllElements(HTMLElementName.TD).get(1);
                        Element Col3 = TR.getAllElements(HTMLElementName.TD).get(3);
                        Element Col4 = TR.getAllElements(HTMLElementName.TD).get(4);
                        Element Col5 = TR.getAllElements(HTMLElementName.TD).get(5);
                        Element Col6 = TR.getAllElements(HTMLElementName.TD).get(7);

                        map.put("col1", (Col1).getContent().toString());
                        map.put("col2", (Col2).getContent().toString());
                        map.put("col3", (Col3).getContent().toString());
                        map.put("col4", (Col4).getContent().toString());
                        map.put("col5", (Col5).getContent().toString());
                        map.put("col6", (Col6).getContent().toString());

                        data.add(map);
                    }

                    handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ShuttleAdapter.notifyDataSetChanged(); // 모든 작업이 끝나면 리스트 갱신
                            progressDialog.dismiss(); // 모든 작업이 끝나면 다이어로그 종료
                        }
                    }, 0);
                }
            }.start();
        } catch (Exception e) {
            Log.e(TAG, "에러" + e);
        }

        return rootView;
    }
}
