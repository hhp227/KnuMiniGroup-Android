package com.hhp227.knu_minigroup.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public class SCDormMealViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public SCDormMealViewModel() {
        mState.postValue(new State(true, new ArrayList<>(), null));
        fetchDataTask();
    }

    private void fetchDataTask() {
        String tag_string_req = "req_getmeal";
        StringRequest stringRequest = new StringEucKrRequest(Request.Method.GET, EndPoint.URL_KNU_SC_DORM_MEAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                Element table = source.getAllElements(HTMLElementName.TABLE).get(1);
                List<String> mealList = new ArrayList<>();

                for (Element p : table.getAllElements(HTMLElementName.P))
                    mealList.add(p.getTextExtractor().toString().trim());
                mState.postValue(new State(false, mealList, null));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, new ArrayList<>(), error.getMessage()));
            }
        });

        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    public static final class State {
        public boolean isLoading;

        public List<String> list;

        public String message;

        public State(boolean isLoading, List<String> list, String message) {
            this.isLoading = isLoading;
            this.list = list;
            this.message = message;
        }
    }

    public static class StringEucKrRequest extends StringRequest {
        StringEucKrRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            try {
                String string = new String(response.data, "euc-kr");
                return Response.success(string, HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        }
    }
}
