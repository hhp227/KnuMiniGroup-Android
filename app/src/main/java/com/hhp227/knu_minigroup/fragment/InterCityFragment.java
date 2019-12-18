package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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

public class InterCityFragment extends Fragment {
    private static final String TAG = "시외버스시간표";
    private SwipeRefreshLayout SWPRefresh;
    private Handler handler;
    private Source source;
    private ArrayList<HashMap<String, String>> data;
    private ProgressDialog progressDialog;
    private ListView ShuttleList;
    private SimpleAdapter ShuttleAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shuttle_schedule, container, false);
        ShuttleList = rootView.findViewById(R.id.lv_shuttle);
        SWPRefresh = rootView.findViewById(R.id.srl_shuttle);
        data = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());

        ShuttleAdapter = new SimpleAdapter(getActivity(), data, R.layout.shuttle_item,
                new String[] {"구분", "시간"},
                new int[] {R.id.division, R.id.time_label});

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
                        URL URL = new URL(EndPoint.URL_INTER_CITY_SHUTTLE);
                        InputStream html = URL.openStream();
                        // html소스 코드 인코딩 방식
                        source = new Source(new InputStreamReader(html, "utf-8"));
                        source.fullSequentialParse(); // 순차적으로 구문분석
                    } catch (Exception e) {
                        Log.e(TAG, "에러" + e);
                    }
                    Element table = source.getAllElements(HTMLElementName.TABLE).get(13);

                    Log.d(TAG, "TABLE 갯수" + source.getAllElements(HTMLElementName.TABLE).size());

                    for (int i = 1; i < table.getAllElements(HTMLElementName.TD).size(); i++) {
                        Element TR = table.getAllElements(HTMLElementName.TD).get(i);
                        HashMap<String, String> map = new HashMap<>();

                        Element Time = TR.getAllElements(HTMLElementName.B).get(0);

                        map.put("구분", "대구 북부정류장");
                        map.put("시간", (Time).getContent().toString());

                        data.add(map);
                    }

                    handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 모든 작업이 끝나면 리스트 갱신
                            ShuttleAdapter.notifyDataSetChanged();
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
