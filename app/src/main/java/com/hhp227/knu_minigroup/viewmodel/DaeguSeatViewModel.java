package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.SeatItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DaeguSeatViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public final List<SeatItem> mSeatItemList = new ArrayList<>();

    private static final String TAG = DaeguSeatViewModel.class.getSimpleName();

    public DaeguSeatViewModel() {
        fetchDataTask(false);
    }

    public void refresh() {
        fetchDataTask(true);
    }

    private void fetchDataTask(boolean isRefresh) {
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
                            mState.postValue(new State(false, false, e.getMessage()));
                            Log.e(TAG, e.getMessage());
                        }

                        SeatItem listItem = new SeatItem(id, name, total, occupied, available, disable);

                        if (!isRefresh)
                            mSeatItemList.add(listItem);
                        else
                            mSeatItemList.set(i, listItem);
                    }
                    mState.postValue(new State(false, true, null));
                } catch (JSONException e) {
                    mState.postValue(new State(false, false, e.getMessage()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, false, error.getMessage()));
            }
        });

        mState.postValue(new State(!isRefresh, false, null));
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public static final class State {
        public boolean isLoading;

        public boolean isSuccess;

        public String message;

        public State(boolean isLoading, boolean isSuccess, String message) {
            this.isLoading = isLoading;
            this.isSuccess = isSuccess;
            this.message = message;
        }
    }
}
