package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;

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

public class DaeguSeatViewModel extends ListViewModel<SeatItem> {
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
                            setLoading(false);
                            setMessage(e.getMessage());
                            Log.e(TAG, e.getMessage());
                        } finally {
                            SeatItem listItem = new SeatItem(id, name, total, occupied, available, disable);

                            seatItemList.add(listItem);
                        }
                    }
                    setLoading(false);
                    setItemList(seatItemList);
                } catch (JSONException e) {
                    setLoading(false);
                    setMessage(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setLoading(false);
                setMessage(error.getMessage());
            }
        });

        setLoading(!isRefresh);
        AppController.getInstance().addToRequestQueue(jsonObjectRequest);
    }
}
