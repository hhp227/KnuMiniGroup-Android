package com.hhp227.knu_minigroup.viewmodel;

import android.text.Html;
import android.text.SpannableString;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentMealViewModel extends ViewModel {
    public static final String KEY_BREAKFAST = "breakfast";
    public static final String KEY_LAUNCH = "lunch";
    public static final String KEY_DINNER = "dinner";

    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public StudentMealViewModel(SavedStateHandle savedStateHandle) {
        fetchDataTask(savedStateHandle.get("id"));
    }

    private void fetchDataTask(int id) {
        String endPoint = EndPoint.URL_KNU_MEAL.replace("{ID}", String.valueOf(id));

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ArrayList<Pair<String, String>> arrayList = new ArrayList<>();

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();

                    parser.setInput(new StringReader(response));
                    int eventType = parser.getEventType();

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            String startTag = parser.getName();

                            if (startTag.equals("entry")) {
                                switch (parser.getAttributeValue(0)) {
                                    case "breakfast":
                                        arrayList.add(new Pair<>(KEY_BREAKFAST, new SpannableString(Html.fromHtml(readEntry(parser))).toString()));
                                        break;
                                    case "breakfast_limited":
                                        arrayList.add(new Pair<>(KEY_BREAKFAST, limitedMeal(new SpannableString(Html.fromHtml(readEntry(parser))).toString())));
                                        break;
                                    case "lunch":
                                        arrayList.add(new Pair<>(KEY_LAUNCH, new SpannableString(Html.fromHtml(readEntry(parser))).toString()));
                                        break;
                                    case "lunch_limited":
                                        arrayList.add(new Pair<>(KEY_LAUNCH, limitedMeal(new SpannableString(Html.fromHtml(readEntry(parser))).toString())));
                                        break;
                                    case "dinner":
                                        arrayList.add(new Pair<>(KEY_DINNER, new SpannableString(Html.fromHtml(readEntry(parser))).toString()));
                                        break;
                                    case "dinner_limited":
                                        arrayList.add(new Pair<>(KEY_DINNER, limitedMeal(new SpannableString(Html.fromHtml(readEntry(parser))).toString())));
                                        break;
                                }
                            }
                        }
                        eventType = parser.next();
                    }
                    mState.postValue(new State(false, arrayList.isEmpty() ? Arrays.asList(new Pair<>(KEY_BREAKFAST, "등록된 식단이 없습니다."), new Pair<>(KEY_LAUNCH, "등록된 식단이 없습니다."), new Pair<>(KEY_DINNER, "등록된 식단이 없습니다.")) : arrayList, null));
                } catch (XmlPullParserException | IOException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    mState.postValue(new State(false, new ArrayList<>(), e.getMessage()));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, new ArrayList<>(), error.getMessage()));
            }
        }));
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();

            parser.nextTag();
        }
        return result;
    }

    private String readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        String data = null;

        parser.require(XmlPullParser.START_TAG, null, "entry");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("data")) {
                data = readText(parser);
            }
        }
        return data;
    }

    private String limitedMeal(String text) {
        return "* 특식" + "<br />" + text;
    }

    public static Map<String, List<Pair<String, String>>> groupBy(List<Pair<String, String>> arrayList) {
        Map<String, List<Pair<String, String>>> destination = new LinkedHashMap<>();

        for (Pair<String, String> it : arrayList) {
            String key = it.first;
            List<Pair<String, String>> value = destination.get(key);
            List<Pair<String, String>> list;

            if (value == null) {
                List<Pair<String, String>> answer = new ArrayList<>();
                list = answer;

                destination.put(key, answer);
            } else {
                list = value;
            }
            list.add(it);
        }
        return destination;
    }

    public static final class State {
        public boolean isLoading;

        public List<Pair<String, String>> list;

        public String message;

        public State(boolean isLoading, List<Pair<String, String>> list, String message) {
            this.isLoading = isLoading;
            this.list = list;
            this.message = message;
        }
    }
}
