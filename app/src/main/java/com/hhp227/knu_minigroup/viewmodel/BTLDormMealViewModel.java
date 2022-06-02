package com.hhp227.knu_minigroup.viewmodel;

import android.text.Html;
import android.text.SpannableString;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BTLDormMealViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public BTLDormMealViewModel() {
        mState.postValue(new State(true, new ArrayList<>(), null));
        fetchDataTask();
    }

    private void fetchDataTask() {
        String endPoint = EndPoint.URL_KNU_DORM_MEAL.replace("{ID}", "3");

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ArrayList<String> arrayList = new ArrayList<>();

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();

                    parser.setInput(new StringReader(response));
                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                String startTag = parser.getName();
                                if (startTag.equals("data"))
                                    arrayList.add(new SpannableString(Html.fromHtml(parser.nextText())).toString());
                                break;
                        }
                        eventType = parser.next();
                    }
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                    mState.postValue(new State(false, new ArrayList<>(), e.getMessage()));
                }
                mState.postValue(new State(false, arrayList.isEmpty() ? Arrays.asList("등록된 식단이 없습니다.", "등록된 식단이 없습니다.", "등록된 식단이 없습니다.") : arrayList, null));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, new ArrayList<>(), error.getMessage()));
            }
        }));
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
}
