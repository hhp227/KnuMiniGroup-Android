package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.adapter.SeatListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.SeatItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SangjuSeatFragment extends Fragment {
    private static final String TAG = "상주 열람실좌석";
    private ListView listView;
    private List<SeatItem> seatItemList;
    private ProgressDialog progressDialog;
    private SeatListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean isRefresh;

    public SangjuSeatFragment() {
    }

    public static SangjuSeatFragment newInstance() {
        SangjuSeatFragment fragment = new SangjuSeatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        listView = rootView.findViewById(R.id.list_view);
        swipeRefreshLayout = rootView.findViewById(R.id.sr_layout);
        seatItemList = new ArrayList<>();
        adapter = new SeatListAdapter(getActivity(), seatItemList);
        progressDialog = new ProgressDialog(getActivity());

        listView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isRefresh = true;
                        fetchData();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        progressDialog.setCancelable(false);
        progressDialog.setMessage("불러오는중...");

        showProgressDialog();
        fetchData();

        return rootView;
    }

    private void fetchData() {
        app.AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.URL_KNULIBRARY_SEAT.replace("{ID}", "2"), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("data");
                    JSONArray jsonArray = jsonObject.getJSONArray("list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);

                        String[] disable = null;
                        try {
                            JSONObject disablePeriod = data.getJSONObject("disablePeriod");
                            disable = new String[3];
                            disable[0] = disablePeriod.getString("name");
                            disable[1] = disablePeriod.getString("beginTime");
                            disable[2] = disablePeriod.getString("endTime");
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }

                        SeatItem listItem = new SeatItem(
                                data.getInt("id"),
                                data.getString("name"),
                                data.getInt("activeTotal"),
                                data.getInt("occupied"),
                                data.getInt("available"),
                                disable);

                        if (isRefresh == false)
                            seatItemList.add(listItem);
                        else
                            seatItemList.set(i, listItem);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                hideProgressDialog();
            }
        }));
    }

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
