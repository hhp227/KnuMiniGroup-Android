package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.FragmentShuttleScheduleBinding;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class InterCityFragment extends Fragment {
    private static final String TAG = "시외버스시간표";

    private ArrayList<HashMap<String, String>> mShuttleList;

    private ProgressDialog mProgressDialog;

    private SimpleAdapter mAdapter;

    private Source mSource;

    private FragmentShuttleScheduleBinding mBinding;

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
        mProgressDialog = new ProgressDialog(getActivity());
        mAdapter = new SimpleAdapter(getActivity(), mShuttleList, R.layout.shuttle_item,
                new String[] {"구분", "시간"},
                new int[] {R.id.division, R.id.time_label});

        mBinding.lvShuttle.setAdapter(mAdapter);
        mBinding.srlShuttle.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        mBinding.srlShuttle.setRefreshing(false); // 당겨서 새로고침 숨김
                    }
                }, 1000);
            }
        });
        mProgressDialog.setMessage("불러오는중...");
        showProgressDialog();
        try {
            new Thread() {
                public void run() {
                    try {
                        URL URL = new URL(EndPoint.URL_INTER_CITY_SHUTTLE);
                        InputStream html = URL.openStream();
                        // html소스 코드 인코딩 방식
                        mSource = new Source(new InputStreamReader(html, StandardCharsets.UTF_8));

                        mSource.fullSequentialParse(); // 순차적으로 구문분석
                    } catch (Exception e) {
                        Log.e(TAG, "에러" + e);
                    }
                    Element table = mSource.getAllElements(HTMLElementName.TABLE).get(13);

                    Log.d(TAG, "TABLE 갯수" + mSource.getAllElements(HTMLElementName.TABLE).size());
                    for (int i = 1; i < table.getAllElements(HTMLElementName.TD).size(); i++) {
                        Element TR = table.getAllElements(HTMLElementName.TD).get(i);
                        HashMap<String, String> map = new HashMap<>();
                        Element Time = TR.getAllElements(HTMLElementName.B).get(0);

                        map.put("구분", "대구 북부정류장");
                        map.put("시간", (Time).getContent().toString());
                        mShuttleList.add(map);
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 모든 작업이 끝나면 리스트 갱신
                            mAdapter.notifyDataSetChanged();
                            hideProgressDialog();
                        }
                    }, 0);
                }
            }.start();
        } catch (Exception e) {
            Log.e(TAG, "에러" + e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }
}
