package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.FragmentShuttleScheduleBinding;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DCShuttleScheduleFragment extends Fragment {
    private static final String TAG = "학교버스시간표";

    private List<Map<String, String>> mShuttleList;

    private SimpleAdapter mAdapter;

    private FragmentShuttleScheduleBinding mBinding;

    public static DCShuttleScheduleFragment newInstance() {
        return new DCShuttleScheduleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentShuttleScheduleBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mShuttleList = new ArrayList<>();
        mAdapter = new SimpleAdapter(getContext(), mShuttleList, R.layout.shuttle_item, new String[] {"col1", "col2"}, new int[] {R.id.division, R.id.time_label});

        mBinding.lvShuttle.setAdapter(mAdapter);
        mBinding.srlShuttle.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mShuttleList.clear();
                        fetchDataTask();
                        mBinding.srlShuttle.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        fetchDataTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void fetchDataTask() {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.URL_SHUTTLE.replace("{SHUTTLE}", "map03"), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);

                Log.e("TEST", "response" + response);
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
                            //Log.e(TAG, e.getMessage());
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null) {
                    VolleyLog.e(error.getMessage());
                }
            }
        }));
    }
}
