package com.hhp227.knu_minigroup.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.YouTubeItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// TODO FindGroupViewModel 참고해서 addAll로 로직 교체
public class YoutubeSearchViewModel extends ViewModel {
    public static final String API_KEY = "AIzaSyBxQb9CaA01lU5AkXnPGf3s8QjoiV-3Vys";

    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public final List<YouTubeItem> mYouTubeItemList = new ArrayList<>();

    private static final int LIMIT = 50;

    private String mQuery = "";

    public YoutubeSearchViewModel() {
        mState.postValue(new State(true, false, null));
        fetchDataTask(mQuery);
    }

    public void setQuery(String query) {
        this.mQuery = query;

        mState.postValue(new State(true, false, null));
        refresh();
    }

    public void refresh() {
        mYouTubeItemList.clear();
        fetchDataTask(mQuery);
    }

    private void fetchDataTask(String query) {
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.URL_YOUTUBE_API + "?part=snippet&key=" + API_KEY + "&q=" + query + "&maxResults=" + LIMIT, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray items = response.getJSONArray("items");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject jsonObject = items.getJSONObject(i);
                        String id = jsonObject.getJSONObject("id").getString("videoId");
                        JSONObject snippet = jsonObject.getJSONObject("snippet");
                        String publishedAt = snippet.getString("publishedAt");
                        String title = snippet.getString("title");
                        String thumbnail = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                        String channelTitle = snippet.getString("channelTitle");

                        YouTubeItem youTubeItem = new YouTubeItem(id, publishedAt, title, thumbnail, channelTitle);
                        mYouTubeItemList.add(youTubeItem);
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
        }));
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
