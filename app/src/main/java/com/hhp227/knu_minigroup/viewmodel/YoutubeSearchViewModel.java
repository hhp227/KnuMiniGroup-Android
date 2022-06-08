package com.hhp227.knu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class YoutubeSearchViewModel extends ViewModel {
    public static final String API_KEY = "AIzaSyBxQb9CaA01lU5AkXnPGf3s8QjoiV-3Vys";

    private final MutableLiveData<State> mState = new MutableLiveData<>();

    private final MutableLiveData<String> mQuery = new MutableLiveData<>();

    private static final int LIMIT = 50;

    public YoutubeSearchViewModel() {
        setQuery("");
    }

    public void setQuery(String query) {
        mQuery.postValue(query);
    }

    public LiveData<String> getQuery() {
        return mQuery;
    }

    public LiveData<State> getState() {
        return mState;
    }

    public void refresh() {
        setQuery(mQuery.getValue());
    }

    public void requestData(String query) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                fetchDataTask(query);
            }
        });
    }

    private void fetchDataTask(String query) {
        mState.postValue(new State(true, Collections.emptyList(), null));
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, EndPoint.URL_YOUTUBE_API + "?part=snippet&key=" + API_KEY + "&q=" + query + "&maxResults=" + LIMIT, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    List<YouTubeItem> youTubeItems = new ArrayList<>();
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
                        youTubeItems.add(youTubeItem);
                    }
                    mState.postValue(new State(false, youTubeItems, null));
                } catch (JSONException e) {
                    mState.postValue(new State(false, Collections.emptyList(), e.getMessage()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, Collections.emptyList(), error.getMessage()));
            }
        }));
    }

    public static final class State {
        public boolean isLoading;

        public List<YouTubeItem> youTubeItems;

        public String message;

        public State(boolean isLoading, List<YouTubeItem> youTubeItems, String message) {
            this.isLoading = isLoading;
            this.youTubeItems = youTubeItems;
            this.message = message;
        }
    }
}
