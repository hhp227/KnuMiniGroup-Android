package com.hhp227.knu_minigroup.data;

import android.text.Html;
import android.text.SpannableString;
import android.util.Pair;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.helper.Callback;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

public class MealRepository {
    public static final String KEY_BREAKFAST = "breakfast";
    public static final String KEY_LAUNCH = "lunch";
    public static final String KEY_DINNER = "dinner";

    public void getStudentMealList(int id, Callback callback) {
        String endPoint = EndPoint.URL_KNU_MEAL.replace("{ID}", String.valueOf(id));

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ArrayList<Pair<String, String>> mealList = new ArrayList<>();

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
                                        mealList.add(new Pair<>(KEY_BREAKFAST, toHtmlString(readEntry(parser))));
                                        break;
                                    case "breakfast_limited":
                                        mealList.add(new Pair<>(KEY_BREAKFAST, limitedMeal(toHtmlString(readEntry(parser)))));
                                        break;
                                    case "lunch":
                                        mealList.add(new Pair<>(KEY_LAUNCH, toHtmlString(readEntry(parser))));
                                        break;
                                    case "lunch_limited":
                                        mealList.add(new Pair<>(KEY_LAUNCH, limitedMeal(toHtmlString(readEntry(parser)))));
                                        break;
                                    case "dinner":
                                        mealList.add(new Pair<>(KEY_DINNER, toHtmlString(readEntry(parser))));
                                        break;
                                    case "dinner_limited":
                                        mealList.add(new Pair<>(KEY_DINNER, limitedMeal(toHtmlString(readEntry(parser)))));
                                        break;
                                }
                            }
                        }
                        eventType = parser.next();
                    }
                    callback.onSuccess(mealList.isEmpty() ? Arrays.asList(
                            new Pair<>(KEY_BREAKFAST, "등록된 식단이 없습니다."),
                            new Pair<>(KEY_LAUNCH, "등록된 식단이 없습니다."),
                            new Pair<>(KEY_DINNER, "등록된 식단이 없습니다.")
                    ) : mealList);
                } catch (XmlPullParserException | IOException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    callback.onFailure(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
            }
        }));
    }

    public void getBTLDormMealList(Callback callback) {
        String endPoint = EndPoint.URL_KNU_DORM_MEAL.replace("{ID}", "3");

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, endPoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ArrayList<String> mealList = new ArrayList<>();

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
                                    mealList.add(toHtmlString(parser.nextText()));
                                break;
                        }
                        eventType = parser.next();
                    }
                    callback.onSuccess(mealList.isEmpty() ? Arrays.asList(
                            "등록된 식단이 없습니다.",
                            "등록된 식단이 없습니다.",
                            "등록된 식단이 없습니다."
                    ) : mealList);
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                    callback.onFailure(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure(error);
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

    private String toHtmlString(String text) {
        return new SpannableString(Html.fromHtml(text)).toString();
    }

    private String limitedMeal(String text) {
        return "* 특식" + "<br />" + text;
    }
}
