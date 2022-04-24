package com.hhp227.knu_minigroup.fragment;

import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
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

import com.hhp227.knu_minigroup.databinding.FragmentDormmealBinding;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO
public class StudentMealFragment extends Fragment {
    private static final String KEY_BREAKFAST = "breakfast";
    private static final String KEY_LAUNCH = "lunch";
    private static final String KEY_DINNER = "dinner";

    private static final String TAG = "학생식당 식단표";

    private Pair<String, TextView>[] mMenuView;

    private int mId;

    private FragmentDormmealBinding mBinding;

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
        mBinding = FragmentDormmealBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMenuView = new Pair[] {
                new Pair<>(KEY_BREAKFAST, mBinding.breakfast),
                new Pair<>(KEY_LAUNCH, mBinding.lunch),
                new Pair<>(KEY_DINNER, mBinding.dinner)
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
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

    private void setTextView(ArrayList<Pair<String, String>> arrayList, Map<String, List<Pair<String, String>>> stringListMap) {
        for (Pair<String, TextView> pair: mMenuView) {
            pair.second.setText(!arrayList.isEmpty() ? Html.fromHtml(extractText(pair.first, stringListMap)) : "등록된 식단이 없습니다.");
        }
    }

    private String extractText(String key, Map<String, List<Pair<String, String>>> stringListMap) {
        StringBuilder stringBuilder = new StringBuilder();

        if (stringListMap.get(key) != null) {
            for (Pair<String, String> pair : stringListMap.get(key)) {
                stringBuilder.append(pair.second);
            }
        }
        return stringBuilder.toString();
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
