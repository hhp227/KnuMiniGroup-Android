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
import com.hhp227.knu_minigroup.databinding.FragmentShuttleScheduleScBinding;
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

    private ArrayList<HashMap<String, String>> mShuttleList;

    private ProgressDialog mProgressDialog;

    private SimpleAdapter mAdapter;

    private Source mSource;

    private FragmentShuttleScheduleScBinding mBinding;

    public static SCShuttleScheduleFragment newInstance() {
        return new SCShuttleScheduleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentShuttleScheduleScBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mShuttleList = new ArrayList<>();
        mProgressDialog = new ProgressDialog(getActivity());
        mAdapter = new SimpleAdapter(getActivity(), mShuttleList, R.layout.shuttle_sc_item,
                new String[] {"col1", "col2", "col3", "col4", "col5", "col6"},
                new int[] {R.id.column1, R.id.column2, R.id.column3, R.id.column4, R.id.column5, R.id.column6});

        mBinding.lvShuttle.setAdapter(mAdapter);
        mBinding.srlShuttle.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
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
                        URL URL = new URL(EndPoint.URL_SHUTTLE.replace("{SHUTTLE}", "map03_02"));
                        InputStream html = URL.openStream();
                        mSource = new Source(new InputStreamReader(html, "utf-8")); // 소스를 UTF-8 인코딩으로 불러온다.

                        mSource.fullSequentialParse(); // 순차적으로 구문분석
                        Element table = mSource.getAllElements(HTMLElementName.TABLE).get(0);

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
                            mShuttleList.add(map);
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged(); // 모든 작업이 끝나면 리스트 갱신
                                hideProgressDialog();
                            }
                        }, 0);
                    } catch (Exception e) {
                        Log.e(TAG, "에러" + e);
                    }
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
