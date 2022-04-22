package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;
import android.view.WindowManager;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
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
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupMainViewModel extends ViewModel {
    public final List<String> mGroupItemKeys = new ArrayList<>();

    public final List<Object> mGroupItemValues = new ArrayList<>();

    private static final String TAG = GroupMainViewModel.class.getSimpleName();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    public MutableLiveData<State> mState = new MutableLiveData<>(null);

    public void refresh() {
        mGroupItemKeys.clear();
        mGroupItemValues.clear();
    }

    private void fetchDataTask() {
        mState.postValue(new State(true, null));
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Source source = new Source(response);

                try {
                    List<Element> listElementA = source.getAllElements(HTMLElementName.A);

                    for (Element elementA : listElementA) {
                        try {
                            String id = groupIdExtract(elementA.getAttributeValue("onclick"));
                            boolean isAdmin = adminCheck(elementA.getAttributeValue("onclick"));
                            String image = EndPoint.BASE_URL + elementA.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                            String name = elementA.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                            GroupItem groupItem = new GroupItem();

                            groupItem.setId(id);
                            groupItem.setAdmin(isAdmin);
                            groupItem.setImage(image);
                            groupItem.setName(name);
                            mGroupItemKeys.add(id);
                            mGroupItemValues.add(groupItem);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                    insertAdvertisement();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    initFirebaseData();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                mState.postValue(new State(false, error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("panel_id", "2");
                params.put("start", "1");
                params.put("display", "10");
                params.put("encoding", "utf-8");
                return params;
            }
        });
    }

    private void insertAdvertisement() {
        if (!mGroupItemValues.isEmpty()) {
            //mAdapter.addHeaderView("가입중인 그룹", 0);
            if (mGroupItemValues.size() % 2 == 0)
                mGroupItemValues.add("광고");
        } else {
            mGroupItemValues.add("없음");
            //mAdapter.addHeaderView("인기 모임");
            mGroupItemValues.add("뷰페이져");
        }
    }

    private String groupIdExtract(String href) {
        return href.split("'")[3].trim();
    }

    private boolean adminCheck(String onClick) {
        return onClick.split("'")[1].trim().equals("0");
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");

        fetchDataTaskFromFirebase(databaseReference.child(mPreferenceManager.getUser().getUid()).orderByValue().equalTo(true), false);
    }

    private void fetchDataTaskFromFirebase(Query query, final boolean isRecursion) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isRecursion) {
                    try {
                        String key = dataSnapshot.getKey();
                        GroupItem value = dataSnapshot.getValue(GroupItem.class);
                        assert value != null;
                        int index = mGroupItemKeys.indexOf(value.getId());

                        if (index > -1) {
                            //mGroupItemValues.set(index, value); //isAdmin값때문에 주석처리
                            mGroupItemKeys.set(index, key);
                        }
                        //mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        /*if (getActivity() != null) {
                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }*/
                    }
                } else {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

                            fetchDataTaskFromFirebase(databaseReference.child(snapshot.getKey()), true);
                        }
                    } else {
                        /*if (getActivity() != null) {
                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }*/
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    public static final class State {
        public boolean isLoading;

        public String message;

        public State(boolean isLoading, String message) {
            this.isLoading = isLoading;
            this.message = message;
        }
    }
}
