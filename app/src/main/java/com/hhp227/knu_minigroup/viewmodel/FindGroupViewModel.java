package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.GroupItem;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class FindGroupViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>(new State(false, false, 1, false, null));

    public final List<String> mGroupItemKeys = new ArrayList<>(Arrays.asList(""));

    public final List<GroupItem> mGroupItemValues = new ArrayList<>(Arrays.asList((GroupItem) null));

    private static final int LIMIT = 15;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    public int mMinId;

    public FindGroupViewModel() {
        if (mState.getValue() != null) {
            fetchGroupList(mState.getValue().offset);
        }
    }

    public void fetchGroupList(int offset) {
        mState.postValue(new State(true, false, 0, false, null));
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);
                List<Element> list = source.getAllElements("id", "accordion", false);

                for (Element element : list) {
                    try {
                        Element menuList = element.getFirstElementByClass("menu_list");

                        if (element.getAttributeValue("class").equals("accordion")) {
                            int id = groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"));
                            String imageUrl = EndPoint.BASE_URL + element.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                            String name = element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                            StringBuilder info = new StringBuilder();
                            String description = menuList.getAllElementsByClass("info").get(0).getContent().toString();
                            String joinType = menuList.getAllElementsByClass("info").get(1).getTextExtractor().toString().trim();
                            mMinId = mMinId == 0 ? id : Math.min(mMinId, id);

                            for (Element span : element.getFirstElement(HTMLElementName.A).getAllElementsByClass("info")) {
                                String extractedText = span.getTextExtractor().toString();

                                info.append(extractedText.contains("회원수") ?
                                        extractedText.substring(0, extractedText.lastIndexOf("생성일")).trim() + "\n" :
                                        extractedText + "\n");
                            }

                            /*if (id > mMinId) {
                                mHasRequestedMore = true;
                                break;
                            } else
                                mHasRequestedMore = false;*/
                            GroupItem groupItem = new GroupItem();

                            groupItem.setId(String.valueOf(id));
                            groupItem.setImage(imageUrl);
                            groupItem.setName(name);
                            groupItem.setInfo(info.toString().trim());
                            groupItem.setDescription(description);
                            groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                            mGroupItemKeys.add(mGroupItemKeys.size() - 1, String.valueOf(id));
                            mGroupItemValues.add(mGroupItemValues.size() - 1, groupItem);
                        }
                    } catch (Exception e) {
                        Log.e("TEST", "e: " + e.getMessage());
                        mState.postValue(new State(false, false, 0, false, e.getMessage()));
                    } finally {
                        initFirebaseData();
                    }
                }
                /*mAdapter.setFooterProgressBarVisibility(View.INVISIBLE);
                mAdapter.notifyDataSetChanged();
                hideProgressBar();
                mBinding.text.setText("그룹이 없습니다.");
                mBinding.rlGroup.setVisibility(mGroupItemValues.size() > 1 ? View.GONE : View.VISIBLE);*/
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mState.postValue(new State(false, false, 0, false, error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("panel_id", "1");
                params.put("gubun", "select_share_total");
                params.put("start", String.valueOf(offset));
                params.put("display", String.valueOf(LIMIT));
                params.put("encoding", "utf-8");
                if (params.size() > 0) {
                    StringBuilder encodedParams = new StringBuilder();

                    try {
                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            encodedParams.append(URLEncoder.encode(entry.getKey(), getParamsEncoding()));
                            encodedParams.append('=');
                            encodedParams.append(URLEncoder.encode(entry.getValue(), getParamsEncoding()));
                            encodedParams.append('&');
                        }
                        return encodedParams.toString().getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                    }
                }
                return null;
            }
        });
    }

    public void fetchNextPage() {

    }

    public void refresh() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                mMinId = 0;

                mGroupItemKeys.clear();
                mGroupItemValues.clear();
                mState.postValue(new State(false, false, 1, true, null));
            }
        });
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        fetchGroupListFromFirebase(databaseReference.orderByKey());
    }

    private void fetchGroupListFromFirebase(Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    GroupItem value = snapshot.getValue(GroupItem.class);

                    if (value != null) {
                        int index = mGroupItemKeys.indexOf(value.getId());

                        if (index > -1) {
                            //mGroupItemValues.set(index, value); //getInfo 구현이 덜되어 주석처리
                            mGroupItemKeys.set(index, key);
                        }
                    }
                }
                if (mState.getValue() != null) {
                    mState.postValue(new State(false, true, mState.getValue().offset, false, null));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mState.postValue(new State(false, false, 0, false, databaseError.getMessage()));
            }
        });
    }

    private int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("[(]|[)]|[,]")[1].trim());
    }

    public static final class State {
        public boolean isLoading;

        public boolean isSuccess;

        public int offset;

        public boolean hasRequestedMore;

        public String message;

        public State(boolean isLoading, boolean isSuccess, int offset, boolean hasRequestedMore, String message) {
            this.isLoading = isLoading;
            this.isSuccess = isSuccess;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.message = message;
        }
    }
}