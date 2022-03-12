package com.hhp227.knu_minigroup.fragment;

import static com.hhp227.knu_minigroup.adapter.GroupGridAdapter.TYPE_AD;
import static com.hhp227.knu_minigroup.adapter.GroupGridAdapter.TYPE_GROUP;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.activity.CreateGroupActivity;
import com.hhp227.knu_minigroup.activity.FindGroupActivity;
import com.hhp227.knu_minigroup.activity.GroupActivity;
import com.hhp227.knu_minigroup.activity.LoginActivity;
import com.hhp227.knu_minigroup.activity.MainActivity;
import com.hhp227.knu_minigroup.activity.RequestActivity;
import com.hhp227.knu_minigroup.adapter.GroupGridAdapter;
import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.databinding.FragmentGroupBinding;
import com.hhp227.knu_minigroup.dto.GroupItem;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupFragment extends Fragment {
    public static final int UPDATE_GROUP = 30;
    private static final int PORTAIT_SPAN_COUNT = 2;
    private static final int LANDSCAPE_SPAN_COUNT = 4;
    private static final String TAG = GroupFragment.class.getSimpleName();

    private int mSpanCount;

    private CookieManager mCookieManager;

    private CountDownTimer mCountDownTimer;

    private GridLayoutManager mGridLayoutManager;

    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup;

    private GroupGridAdapter mAdapter;

    private List<String> mGroupItemKeys;

    private List<Object> mGroupItemValues;

    private PreferenceManager mPreferenceManager;

    private RecyclerView.ItemDecoration mItemDecoration;

    private FragmentGroupBinding mBinding;

    private ActivityResultLauncher<Intent> mActivityResultLauncher;

    public GroupFragment() {
    }

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentGroupBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSpanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? PORTAIT_SPAN_COUNT :
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? LANDSCAPE_SPAN_COUNT :
                        0;
        mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_TEXT
                        || mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_BANNER
                        || mAdapter.getItemViewType(position) == GroupGridAdapter.TYPE_VIEW_PAGER ? mSpanCount : 1;
            }
        };
        mGridLayoutManager = new GridLayoutManager(getContext(), mSpanCount);
        mItemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getAdapter() != null && parent.getAdapter().getItemViewType(parent.getChildAdapterPosition(view)) == TYPE_GROUP || parent.getAdapter().getItemViewType(parent.getChildAdapterPosition(view)) == TYPE_AD) {
                    outRect.top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    outRect.bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                    if (parent.getChildAdapterPosition(view) % mSpanCount == 0) {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
                    } else if (parent.getChildAdapterPosition(view) % mSpanCount == 1) {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                    } else {
                        outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
                    }
                }
            }
        };
        mGroupItemKeys = new ArrayList<>();
        mGroupItemValues = new ArrayList<>();
        mAdapter = new GroupGridAdapter(mGroupItemKeys, mGroupItemValues);
        mCookieManager = AppController.getInstance().getCookieManager();
        mPreferenceManager = AppController.getInstance().getPreferenceManager();
        mCountDownTimer = new CountDownTimer(80000, 8000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mAdapter.moveSliderPager();
            }

            @Override
            public void onFinish() {
                start();
            }
        };
        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    mGroupItemKeys.clear();
                    mGroupItemValues.clear();
                    fetchDataTask();
                    ((MainActivity) requireActivity()).updateProfileImage();
                }
            }
        });

        ((MainActivity) requireActivity()).setAppBar(mBinding.toolbar, getString(R.string.main));
        mAdapter.setHasStableIds(true);
        mAdapter.setOnItemClickListener(new GroupGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                if (mGroupItemValues.get(position) instanceof GroupItem) {
                    GroupItem groupItem = (GroupItem) mGroupItemValues.get(position);
                    Intent intent = new Intent(getContext(), GroupActivity.class);

                    intent.putExtra("admin", groupItem.isAdmin());
                    intent.putExtra("grp_id", groupItem.getId());
                    intent.putExtra("grp_nm", groupItem.getName());
                    intent.putExtra("grp_img", groupItem.getImage()); // 경북대 소모임에는 없음
                    intent.putExtra("pos", position);
                    intent.putExtra("key", mAdapter.getKey(position));
                    mActivityResultLauncher.launch(intent);
                }
            }
        });
        mAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.b_find:
                        mActivityResultLauncher.launch(new Intent(getContext(), FindGroupActivity.class));
                        return;
                    case R.id.b_create:
                        mActivityResultLauncher.launch(new Intent(getContext(), CreateGroupActivity.class));
                }
            }
        });
        mGridLayoutManager.setSpanSizeLookup(mSpanSizeLookup);
        mBinding.rvGroup.setLayoutManager(mGridLayoutManager);
        mBinding.rvGroup.setAdapter(mAdapter);
        mBinding.rvGroup.addItemDecoration(mItemDecoration);
        mBinding.srlGroup.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mGroupItemKeys.clear();
                        mGroupItemValues.clear();
                        mBinding.srlGroup.setRefreshing(false);
                        fetchDataTask();
                    }
                }, 1700);
            }
        });
        mBinding.srlGroup.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mBinding.bnvGroupButton.getMenu().getItem(0).setCheckable(false);
        mBinding.bnvGroupButton.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setCheckable(false);
                switch (item.getItemId()) {
                    case R.id.navigation_find:
                        mActivityResultLauncher.launch(new Intent(getContext(), FindGroupActivity.class));
                        return true;
                    case R.id.navigation_request:
                        startActivity(new Intent(getContext(), RequestActivity.class));
                        return true;
                    case R.id.navigation_create:
                        mActivityResultLauncher.launch(new Intent(getContext(), CreateGroupActivity.class));
                        return true;
                }
                return false;
            }
        });
        if (AppController.getInstance().getPreferenceManager().getUser() == null)
            logout();
        showProgressBar();
        fetchDataTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.rvGroup.removeItemDecoration(mItemDecoration);
        mBinding = null;
        mActivityResultLauncher = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCountDownTimer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        CountDownTimer countDownTimer = mCountDownTimer;
        if (countDownTimer != null)
            countDownTimer.cancel();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                mSpanCount = PORTAIT_SPAN_COUNT;
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                mSpanCount = LANDSCAPE_SPAN_COUNT;
                break;
        }
        mGridLayoutManager.setSpanSizeLookup(mSpanSizeLookup);
        mGridLayoutManager.setSpanCount(mSpanCount);
        mBinding.rvGroup.invalidateItemDecorations();
    }

    private void fetchDataTask() {
        requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
                    mAdapter.notifyDataSetChanged();
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
                hideProgressBar();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void logout() {
        mPreferenceManager.clear();
        mCookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
                Log.d(TAG, "onReceiveValue " + value);
            }
        });
        startActivity(new Intent(getContext(), LoginActivity.class));
        requireActivity().finish();
    }

    private void insertAdvertisement() {
        if (!mGroupItemValues.isEmpty()) {
            mAdapter.addHeaderView("가입중인 그룹", 0);
            if (mGroupItemValues.size() % 2 == 0)
                mGroupItemValues.add("광고");
        } else {
            mGroupItemValues.add("없음");
            mAdapter.addHeaderView("인기 모임");
            mGroupItemValues.add("뷰페이져");
        }
        hideProgressBar();
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
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                } else {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

                            fetchDataTaskFromFirebase(databaseReference.child(snapshot.getKey()), true);
                        }
                    } else
                        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }

    private String groupIdExtract(String href) {
        return href.split("'")[3].trim();
    }

    private boolean adminCheck(String onClick) {
        return onClick.split("'")[1].trim().equals("0");
    }

    private void showProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.GONE)
            mBinding.pbGroup.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbGroup.getVisibility() == View.VISIBLE)
            mBinding.pbGroup.setVisibility(View.GONE);
    }
}
