package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.knu_minigroup.adapter.SeatListAdapter;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.FragmentSeatBinding;
import com.hhp227.knu_minigroup.dto.SeatItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// TODO
public class DaeguSeatFragment extends Fragment {
    private static final String TAG = "대구 열람실좌석";

    private boolean mIsRefresh;

    private List<SeatItem> mSeatItemList;

    private ProgressDialog mProgressDialog;

    private SeatListAdapter mAdapter;

    private FragmentSeatBinding mBinding;

    public DaeguSeatFragment() {
    }

    public static DaeguSeatFragment newInstance() {
        return new DaeguSeatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentSeatBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSeatItemList = new ArrayList<>();
        mAdapter = new SeatListAdapter(mSeatItemList);
        mProgressDialog = new ProgressDialog(getActivity());

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.recyclerView.setAdapter(mAdapter);
        mBinding.srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsRefresh = true;
                        fetchDataTask();
                        mBinding.srl.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("불러오는중...");
        showProgressDialog();
        fetchDataTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void fetchDataTask() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, EndPoint.URL_KNULIBRARY_SEAT.replace("{ID}", "1"), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = response.getJSONObject("data");
                    JSONArray jsonArray = jsonObject.getJSONArray("list");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        int id = data.getInt("id");
                        String name = data.getString("name");
                        int total = data.getInt("activeTotal");
                        int occupied = data.getInt("occupied");
                        int available = data.getInt("available");
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

                        SeatItem listItem = new SeatItem(id, name, total, occupied, available, disable);

                        if (!mIsRefresh)
                            mSeatItemList.add(listItem);
                        else
                            mSeatItemList.set(i, listItem);
                    }
                    mAdapter.notifyDataSetChanged();
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
        });
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
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
