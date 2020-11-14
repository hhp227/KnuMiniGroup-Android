package com.hhp227.knu_minigroup.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemesterTimeTableFragment extends Fragment {
    private static final String TAG = "시간표";

    private LinearLayout[] lay;

    private ProgressBar mProgressBar;

    private TextView[] mDatas;

    public SemesterTimeTableFragment() {
    }

    public static SemesterTimeTableFragment newInstance() {
        return new SemesterTimeTableFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timetable, container, false);
        mProgressBar = rootView.findViewById(R.id.pb_group);
        mDatas = new TextView[22 * 6];
        lay = new LinearLayout[22];
        lay[0] = rootView.findViewById(R.id.lay_0);
        lay[1] = rootView.findViewById(R.id.lay_1);
        lay[2] = rootView.findViewById(R.id.lay_2);
        lay[3] = rootView.findViewById(R.id.lay_3);
        lay[4] = rootView.findViewById(R.id.lay_4);
        lay[5] = rootView.findViewById(R.id.lay_5);
        lay[6] = rootView.findViewById(R.id.lay_6);
        lay[7] = rootView.findViewById(R.id.lay_7);
        lay[8] = rootView.findViewById(R.id.lay_8);
        lay[9] = rootView.findViewById(R.id.lay_9);
        lay[10] = rootView.findViewById(R.id.lay_10);
        lay[11] = rootView.findViewById(R.id.lay_11);
        lay[12] = rootView.findViewById(R.id.lay_12);
        lay[13] = rootView.findViewById(R.id.lay_13);
        lay[14] = rootView.findViewById(R.id.lay_14);
        lay[15] = rootView.findViewById(R.id.lay_15);
        lay[16] = rootView.findViewById(R.id.lay_16);
        lay[17] = rootView.findViewById(R.id.lay_17);
        lay[18] = rootView.findViewById(R.id.lay_18);
        lay[19] = rootView.findViewById(R.id.lay_19);
        lay[20] = rootView.findViewById(R.id.lay_20);
        lay[21] = rootView.findViewById(R.id.lay_21);

        showProgressBar();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.TIMETABLE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Element timeTable = new Source(response).getFirstElementByClass("bbslist");
                List<Element> list = timeTable.getAllElements(HTMLElementName.TR);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params1.weight = 1; // 레이아웃의 weight를 동적으로 설정 (칸의 비율)
                params1.width = getLcdSizeWidth() / 6;
                params1.height = getLcdSizeHeight() / 14;

                params1.setMargins(1, 1, 1, 1);
                params1.gravity = 1; // 표가 뒤틀리는 것을 방지

                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
                params2.weight = 1; // 레이아웃의 weight를 동적으로 설정 (칸의 비율)
                params2.width = getLcdSizeWidth() / 6;
                params2.height = getLcdSizeHeight() / 20;

                params2.setMargins(1, 1, 1, 1);

                for (int i = 0, id = 0; i < lay.length; i++) { // 21개
                    if (i == 1)
                        continue;
                    List<Element> schedule = list.get(i).getChildElements();

                    for (int j = 0; j < 6; j++) { // 6개
                        mDatas[id] = new TextView(getActivity());

                        mDatas[id].setId(id);
                        mDatas[id].setTextSize(10);
                        mDatas[id].setGravity(Gravity.CENTER);
                        mDatas[id].setBackgroundColor(Color.parseColor(i == 0 ? "#FAF4C0" : "#EAEAEA"));
                        mDatas[id].setText(schedule.get(j).getTextExtractor().toString());
                        lay[i].addView(mDatas[id], i == 0 ? params2 : params1); //시간표 데이터 출력
                        id++;
                    }
                }
                hideProgressBar();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                hideProgressBar();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN));
                return headers;
            }
        });
        return rootView;
    }

    public int getLcdSizeWidth() {
        // TODO Auto-generated method stub
        return  ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    public int getLcdSizeHeight() {
        // TODO Auto-generated method stub
        return ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }

    private void showProgressBar() {
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.GONE);
    }
}
