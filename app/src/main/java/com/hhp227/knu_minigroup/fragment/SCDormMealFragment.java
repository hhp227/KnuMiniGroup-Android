package com.hhp227.knu_minigroup.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.FragmentDormmealBinding;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;

public class SCDormMealFragment extends Fragment {
    private static final String TAG = "상주기숙사 식단표";

    private ArrayList<String> mMealList;

    private ProgressDialog mProgressDialog;

    private TextView[] mMenuView;

    private FragmentDormmealBinding mBinding;

    public static SCDormMealFragment newInstance() {
        return new SCDormMealFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentDormmealBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMenuView = new TextView[] {
                mBinding.breakfast,
                mBinding.lunch,
                mBinding.dinner
        };
        mMealList = new ArrayList<>();
        mProgressDialog = new ProgressDialog(getActivity());

        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("불러오는중...");
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

        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void parseHTML(String response) {
        Source source = new Source(response);
        Element table = source.getAllElements(HTMLElementName.TABLE).get(1);

        for (Element p : table.getAllElements(HTMLElementName.P))
            mMealList.add(p.getTextExtractor().toString().trim());
        for (int i = 0; i < mMenuView.length; i++)
            mMenuView[i].setText(mMealList.get(i));
    }

    private void showProgressDialog() {
        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
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
