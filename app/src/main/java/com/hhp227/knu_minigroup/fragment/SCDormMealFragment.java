package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.app.EndPoint;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class SCDormMealFragment extends Fragment {
    private static final String TAG = "상주기숙사 식단표";
    private Source source;
    private TextView[] menuView;
    private ArrayList<String> data;
    private ProgressDialog progressDialog;

    public static SCDormMealFragment newInstance() {
        SCDormMealFragment fragment = new SCDormMealFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dormmeal, container, false);

        menuView = new TextView[] {
                rootView.findViewById(R.id.breakfast),
                rootView.findViewById(R.id.lunch),
                rootView.findViewById(R.id.dinner)
        };

        data = new ArrayList<>();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("불러오는중...");
        showProgressDialog();

        String tag_string_req = "req_getmeal";
        StringRequest stringRequest = new StringEucKrRequest(Request.Method.GET, EndPoint.URL_KNU_SC_DORM_MEAL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressDialog();
                parseHTML(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "응답 에러 : " + error.getMessage());
                hideProgressDialog();
            }
        });
        app.AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);

        return rootView;
    }

    private void parseHTML(String response) {
        source = new Source(response);
        Element table = source.getAllElements(HTMLElementName.TABLE).get(1);
        for (Element p : table.getAllElements(HTMLElementName.P))
            data.add(p.getTextExtractor().toString().trim());
        for (int i = 0; i < menuView.length; i++)
            menuView[i].setText(data.get(i));
    }

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public class StringEucKrRequest extends StringRequest {
        public StringEucKrRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            try {
                String string = new String(response.data, "euc-kr");
                return Response.success(string, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        }
    }
}