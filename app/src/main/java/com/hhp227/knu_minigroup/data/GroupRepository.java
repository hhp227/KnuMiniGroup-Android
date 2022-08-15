package com.hhp227.knu_minigroup.data;

import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.helper.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class GroupRepository {
    public GroupRepository() {
    }

    public void getGroupList() {

    }

    public void addGroup(String cookie, Bitmap bitmap, String title, String description, String type, Callback callback) {
        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.CREATE_GROUP, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!response.getBoolean("isError")) {
                        String groupId = response.getString("CLUB_GRP_ID").trim();
                        String groupName = response.getString("GRP_NM");

                        if (bitmap != null)
                            groupImageUpdate(groupId, groupName, description, bitmap);
                        else {
                            insertGroupToFirebase(groupId, groupName, description, null);
                        }
                    }
                } catch (JSONException e) {
                    callback.onFailure(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("GRP_NM", title);
                params.put("TXT", description);
                params.put("JOIN_DIV", type);
                if (params.size() > 0) {
                    StringBuilder encodedParams = new StringBuilder();

                    try {
                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            encodedParams.append(URLEncoder.encode(entry.getKey(), getParamsEncoding()));
                            encodedParams.append('=');
                            encodedParams.append(URLEncoder.encode(entry.getValue(), getParamsEncoding()));
                            encodedParams.append('&');
                        }
                        return encodedParams.toString().getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                    }
                }
                return null;
            }
        });
    }

    public void setGroup() {

    }

    public void removeGroup() {

    }

    private void groupImageUpdate(String groupId, String groupName, String description, Bitmap bitmap) {
    }

    private void insertGroupToFirebase(String groupId, String groupName, String description, Object o) {
    }
}
