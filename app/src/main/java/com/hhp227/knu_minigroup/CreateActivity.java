package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.knu_minigroup.app.EndPoint;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateActivity extends Activity {
    private static final String TAG = CreateActivity.class.getSimpleName();
    private EditText groupTitle, groupDescription;
    private TextView resetTitle;
    private RadioGroup joinType;
    private boolean joinTypeCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        groupTitle = findViewById(R.id.etTitle);
        groupDescription = findViewById(R.id.etDescription);
        resetTitle = findViewById(R.id.tvReset);
        joinType = findViewById(R.id.rgJoinType);

        groupTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetTitle.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.black : android.R.color.darker_gray));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        resetTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupTitle.setText("");
            }
        });

        joinType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                joinTypeCheck = checkedId == R.id.rbAuto ? false : true;
            }
        });

        joinType.check(R.id.rbAuto);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home :
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return true;
            case R.id.actionSend :
                final String title = groupTitle.getText().toString().trim();
                final String description = groupDescription.getText().toString().trim();
                final String join = !joinTypeCheck ? "0" : "1";
                if(!title.isEmpty() && !description.isEmpty()) {
                    app.AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.CREATE_GROUP, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if(!jsonObject.getBoolean("isError")) {
                                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                                    jsonObject.getInt("CLUB_GRP_ID");
                                }
                            } catch(JSONException e) {
                                Log.e(TAG, e.getMessage());
                            }
                            finish();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.e(error.getMessage());
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Cookie", app.AppController.getInstance().getPreferenceManager().getCookie());
                            return headers;
                        }

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("GRP_NM", title);
                            params.put("TXT", description);
                            params.put("JOIN_DIV", join);
                            return params;
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "잘못되었습니다.", Toast.LENGTH_LONG).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
