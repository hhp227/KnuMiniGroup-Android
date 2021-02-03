package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class StudentMealFragment extends Fragment {
    private static final String KEY_BREAKFAST = "breakfast";
    private static final String KEY_LAUNCH = "lunch";
    private static final String KEY_DINNER = "dinner";

    private static final String TAG = "학생식당 식단표";

    private Pair<String, TextView>[] mMenuView;

    private int mId;

    public static StudentMealFragment newInstance(int id) {
        StudentMealFragment fragment = new StudentMealFragment();
        Bundle args = new Bundle();

        args.putInt("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mId = getArguments().getInt("id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dormmeal, container, false);
        mMenuView = new Pair[] {
                new Pair<>(KEY_BREAKFAST, rootView.findViewById(R.id.breakfast)),
                new Pair<>(KEY_LAUNCH, rootView.findViewById(R.id.lunch)),
                new Pair<>(KEY_DINNER, rootView.findViewById(R.id.dinner))
        };
        String endPoint = EndPoint.URL_KNU_MEAL.replace("{ID}", String.valueOf(mId));

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
                    setTextView(arrayList, groupBy(arrayList));
                } catch (XmlPullParserException | IOException | IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
            }
        }));
        return rootView;
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
        StringBuilder stringBuilder = new StringBuilder();

        if (!text.isEmpty())
            stringBuilder.append("\n");
        stringBuilder.append("* 특식").append("\n").append(text);
        return stringBuilder.toString();
    }

    private void setTextView(ArrayList<Pair<String, String>> arrayList, Map<String, List<Pair<String, String>>> stringListMap) {
        for (Pair<String, TextView> pair: mMenuView) {
            pair.second.setText(!arrayList.isEmpty() ? test(pair.first, stringListMap) : "등록된 식단이 없습니다.");
        }

        Log.e("TEST", stringListMap.toString());
    }

    private String test(String key, Map<String, List<Pair<String, String>>> stringListMap) {
        Log.e("TEST", stringListMap.toString() + ", " + key);
        return "1";
    }

    public static Map<String, List<Pair<String, String>>> groupBy(ArrayList<Pair<String, String>> arrayList) {
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
}
