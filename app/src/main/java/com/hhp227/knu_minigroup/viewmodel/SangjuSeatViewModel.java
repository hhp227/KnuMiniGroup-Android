package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
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
import java.util.Collections;
import java.util.List;

public class SangjuSeatViewModel extends ViewModel {
    private static final String TAG = SangjuSeatViewModel.class.getSimpleName();

    private final MutableLiveData<State> mState = new MutableLiveData<>();

    public SangjuSeatViewModel() {
        fetchDataTask(false);
    }

    public LiveData<State> getState() {
        return mState;
    }

    public void refresh() {
        fetchDataTask(true);
    }

    private void fetchDataTask(boolean isRefresh) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, EndPoint.URL_KNULIBRARY_SEAT.replace("{ID}", "2"), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    List<SeatItem> seatItemList = new ArrayList<>();
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
                            mState.postValue(new State(false, Collections.emptyList(), e.getMessage()));
                            Log.e(TAG, e.getMessage());
                        } finally {
                            SeatItem listItem = new SeatItem(id, name, total, occupied, available, disable);

                            seatItemList.add(listItem);
                        }
                    }
                    mState.postValue(new State(false, seatItemList, null));
                } catch (JSONException e) {
                    mState.postValue(new State(false, Collections.emptyList(), e.getMessage()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, Collections.emptyList(), error.getMessage()));
            }
        });

        mState.postValue(new State(!isRefresh, Collections.emptyList(), null));
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public static final class State {
        public boolean isLoading;

        public List<SeatItem> seatItemList;

        public String message;

        public State(boolean isLoading, List<SeatItem> seatItemList, String message) {
            this.isLoading = isLoading;
            this.seatItemList = seatItemList;
            this.message = message;
        }
    }
}
