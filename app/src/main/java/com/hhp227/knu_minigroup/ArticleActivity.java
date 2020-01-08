package com.hhp227.knu_minigroup;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hhp227.knu_minigroup.adapter.ReplyListAdapter;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ReplyItem;
import com.hhp227.knu_minigroup.fragment.Tab1Fragment;
import com.hhp227.knu_minigroup.helper.PreferenceManager;
import com.hhp227.knu_minigroup.ui.navigationdrawer.DrawerArrowDrawable;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hhp227.knu_minigroup.fragment.Tab1Fragment.UPDATE_ARTICLE;

public class ArticleActivity extends Activity {
    private static final int UPDATE_REPLY = 10;
    private static final String TAG = ArticleActivity.class.getSimpleName();
    private ActionBar actionBar;
    private EditText inputReply;
    private ImageView articleProfile;
    private LinearLayout articleImages;
    private List<ReplyItem> replyItemList;
    private List<String> imageList;
    private ListView listView;
    private PreferenceManager preferenceManager;
    private ProgressDialog progressDialog;
    private ReplyListAdapter replyListAdapter;
    private Source source;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView articleTitle, articleTimeStamp, articleContent, buttonSend;
    private View articleDetail;

    private boolean isBottom, isUpdate, isAuthorized;
    private int position;
    private String groupId, articleId, groupName, groupKey, articleKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        articleDetail = getLayoutInflater().inflate(R.layout.article_detail, null, false);
        buttonSend = findViewById(R.id.tv_btn_send);
        articleProfile = articleDetail.findViewById(R.id.iv_profile_image);
        articleTitle = articleDetail.findViewById(R.id.tv_title);
        articleTimeStamp = articleDetail.findViewById(R.id.tv_timestamp);
        articleContent = articleDetail.findViewById(R.id.tv_content);
        articleImages = articleDetail.findViewById(R.id.ll_image);
        inputReply = findViewById(R.id.et_reply);
        listView = findViewById(R.id.lv_article);
        swipeRefreshLayout = findViewById(R.id.srl_article);

        final Intent intent = getIntent();
        preferenceManager = app.AppController.getInstance().getPreferenceManager();
        groupId = intent.getStringExtra("grp_id");
        groupName = intent.getStringExtra("grp_nm");
        articleId = intent.getStringExtra("artl_num");
        groupKey = intent.getStringExtra("grp_key");
        articleKey = intent.getStringExtra("artl_key");
        position = intent.getIntExtra("position", 0);
        isAuthorized = intent.getBooleanExtra("auth", false);
        isBottom = intent.getBooleanExtra("isbottom", false);
        imageList = new ArrayList<>();
        replyItemList = new ArrayList<>();
        replyListAdapter = new ReplyListAdapter(this, replyItemList);
        progressDialog = new ProgressDialog(this);
        actionBar = getActionBar();
        actionBar.setTitle(groupName);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        });
        articleDetail.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.showContextMenu();
                return true;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputReply.getText().toString().trim().length() > 0) {
                    actionSend(inputReply.getText().toString());
                    // 전송하면 텍스트 초기화
                    inputReply.setText("");
                    if (v != null) {
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                } else
                    Toast.makeText(getApplicationContext(), "댓글을 입력하세요.", Toast.LENGTH_LONG).show();
            }
        });
        inputReply.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSend.setBackgroundResource(s.length() > 0 ? R.drawable.background_sendbtn_p : R.drawable.background_sendbtn_n);
                buttonSend.setTextColor(getResources().getColor(s.length() > 0 ? android.R.color.white : android.R.color.darker_gray));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inputReply.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            }
        });
        listView.addHeaderView(articleDetail);
        listView.setAdapter(replyListAdapter);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("요청중 ...");
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        swipeRefreshLayout.setRefreshing(false);
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        });
        registerForContextMenu(listView); // 콘텍스트메뉴
        showProgressDialog();
        fetchArticleData();
        // isBotoom이 참이면 화면 아래로 이동
        if (isBottom)
            setListViewBottom();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isAuthorized) {
            menu.add(Menu.NONE, 1, Menu.NONE, "수정하기");
            menu.add(Menu.NONE, 2, Menu.NONE, "삭제하기");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home :
                finish();
                return true;
            case 1 :
                Intent intent = new Intent(this, ModifyActivity.class);
                intent.putExtra("grp_id", groupId);
                intent.putExtra("artl_num", articleId);
                intent.putExtra("sbjt", articleTitle.getText().toString().substring(0, articleTitle.getText().toString().lastIndexOf("-")).trim());
                intent.putExtra("txt", articleContent.getText().toString());
                intent.putStringArrayListExtra("img", (ArrayList<String>) imageList);
                startActivityForResult(intent, UPDATE_ARTICLE);
                return true;
            case 2 :
                String tag_string_req = "req_delete";

                progressDialog.setMessage("요청중 ...");
                showProgressDialog();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_ARTICLE, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean("isError");
                            if (!error) {
                                Toast.makeText(getApplicationContext(), "삭제완료", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(ArticleActivity.this, GroupActivity.class);
                                intent.putExtra("admin", getIntent().getBooleanExtra("admin", false));
                                intent.putExtra("grp_id", groupId);
                                intent.putExtra("grp_nm", groupName);
                                intent.putExtra("key", groupKey);
                                // 모든 이전 activity 초기화
                                intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "삭제할수 없습니다.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "json 파싱 에러 : " + e.getMessage());
                        } finally {
                            hideProgressDialog();
                            deleteArticleFromFirebase();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "전송 에러: " + error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", preferenceManager.getCookie());
                        return headers;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("CLUB_GRP_ID", groupId);
                        params.put("ARTL_NUM", articleId);
                        return params;
                    }
                };
                app.AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_ARTICLE && resultCode == RESULT_OK) {
            isUpdate = true;
            onCreate(new Bundle());
        } else if (requestCode == UPDATE_REPLY && resultCode == RESULT_OK && data != null) {
            source = new Source(data.getStringExtra("update_reply"));
            replyItemList.clear();
            List<Element> commentList = source.getAllElementsByClass("comment-list");
            fetchReplyData(commentList);
        }
    }

    /**
     * 댓글을 길게 클릭하면 콘텍스트 메뉴가 뜸
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
        menu.setHeaderTitle("작업선택");
        boolean auth = replyItemList.isEmpty() || position == 0 ? false : replyItemList.get((position - 1)).isAuth();
        menu.add(Menu.NONE, 1, Menu.NONE, "내용 복사");
        if (position != 0 && auth) {
            menu.add(Menu.NONE, 2, Menu.NONE, "댓글 수정");
            menu.add(Menu.NONE, 3, Menu.NONE, "댓글 삭제");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ReplyItem replyItem = replyItemList.isEmpty() || info.position == 0 ? null : replyItemList.get(info.position - 1); // 헤더가 있기때문에 포지션에서 -1을 해준다.
        final String replyId = replyItem == null ? "0" : replyItem.getId();
        switch (item.getItemId()) {
            case 1 :
                android.content.ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(info.position == 0 ? articleContent.getText().toString() : replyItem.getReply());
                Toast.makeText(getApplicationContext(), "클립보드에 복사되었습니다!", Toast.LENGTH_SHORT).show();
                return true;
            case 2 :
                Intent intent = new Intent(getBaseContext(), ReplyModifyActivity.class);
                String reply = replyItem.getReply();
                intent.putExtra("grp_id", groupId);
                intent.putExtra("artl_num", articleId);
                intent.putExtra("cmt", reply);
                intent.putExtra("cmmt_num", replyId);
                startActivityForResult(intent, UPDATE_REPLY);
                return true;
            case 3 :
                String tag_string_req = "req_delete";

                progressDialog.setMessage("요청중...");
                showProgressDialog();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_REPLY, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        source = new Source(response);
                        try {
                            if (!response.contains("처리를 실패했습니다")) {
                                replyItemList.clear();
                                List<Element> commentList = source.getAllElementsByClass("comment-list");
                                fetchReplyData(commentList);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        } finally {
                            hideProgressDialog();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(TAG, error.getMessage());
                        hideProgressDialog();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Cookie", preferenceManager.getCookie());
                        return headers;
                    }

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("CLUB_GRP_ID", groupId);
                        params.put("CMMT_NUM", replyId);
                        params.put("ARTL_NUM", articleId);
                        return params;
                    }
                };
                app.AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
                return true;
        }
        return false;
    }

    private void fetchArticleData() {
        String params = "?CLUB_GRP_ID=" + groupId + "&startL=" + position + "&displayL=1";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                source = new Source(response.trim());
                try {
                    Element element = source.getFirstElementByClass("listbox2");
                    Element viewArt = element.getFirstElementByClass("view_art");
                    Element commentWrap = element.getFirstElementByClass("comment_wrap");
                    List<Element> commentList = element.getAllElementsByClass("comment-list");

                    String profileImg = isAuthorized ? EndPoint.USER_IMAGE.replace("{UID}", preferenceManager.getUser().getUid()) : null;
                    String title = viewArt.getFirstElementByClass("list_title").getTextExtractor().toString();
                    String timeStamp = viewArt.getFirstElement(HTMLElementName.TD).getTextExtractor().toString();
                    String content = contentExtractor(viewArt.getFirstElementByClass("list_cont"), true);

                    List<Element> images = viewArt.getAllElements(HTMLElementName.IMG);
                    String replyCnt = commentWrap.getContent().getFirstElement(HTMLElementName.P).getTextExtractor().toString();

                    Glide.with(getApplicationContext())
                            .load(profileImg)
                            .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                            .into(articleProfile);
                    articleTitle.setText(title);
                    articleTimeStamp.setText(timeStamp);
                    if (!TextUtils.isEmpty(content)) {
                        articleContent.setText(content);
                        articleContent.setVisibility(View.VISIBLE);
                    } else
                        articleContent.setVisibility(View.GONE);

                    if (images.size() > 0) {
                        for (Element image : images) {
                            final int position = imageList.size();
                            String imageUrl = !image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src");
                            ImageView articleImage = new ImageView(getApplicationContext());
                            articleImage.setAdjustViewBounds(true);
                            articleImage.setPadding(0, 0, 0, 30);
                            articleImage.setScaleType(ImageView.ScaleType.FIT_XY);
                            articleImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getApplicationContext(), PictureActivity.class);
                                    intent.putStringArrayListExtra("images", (ArrayList<String>) imageList);
                                    intent.putExtra("position", position);
                                    startActivity(intent);
                                }
                            });
                            Glide.with(getApplicationContext()).load(imageUrl).apply(RequestOptions.errorOf(R.drawable.ic_launcher_background)).into(articleImage);
                            articleImages.addView(articleImage);
                            imageList.add(imageUrl);
                        }
                        articleImages.setVisibility(View.VISIBLE);
                    } else
                        articleImages.setVisibility(View.GONE);

                    fetchReplyData(commentList);
                    if (isUpdate)
                        deliveryUpdate(title, contentExtractor(viewArt.getFirstElementByClass("list_cont"), false), imageList, replyCnt);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "값이 없습니다.", Toast.LENGTH_LONG).show();
                } finally {
                    hideProgressDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "에러 : " + error.getMessage());
                hideProgressDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", preferenceManager.getCookie());
                return headers;
            }
        };
        app.AppController.getInstance().addToRequestQueue(stringRequest);
    }

    private void fetchReplyData(List<Element> commentList) {
        for (Element comment : commentList) {
            Element commentName = comment.getFirstElementByClass("comment-name");
            Element commentAddr = comment.getFirstElementByClass("comment-addr");
            String replyId = commentAddr.getAttributeValue("id").replace("cmt_txt_", "");
            String name = commentName.getTextExtractor().toString().trim();
            String timeStamp = commentName.getFirstElement(HTMLElementName.SPAN).getContent().toString().trim();
            String replyContent = commentAddr.getContent().toString().trim();
            boolean authorization = commentName.getAllElements(HTMLElementName.INPUT).size() > 0;

            ReplyItem replyItem = new ReplyItem();
            replyItem.setId(replyId);
            replyItem.setName(name.substring(0, name.lastIndexOf("(")));
            replyItem.setReply(Html.fromHtml(replyContent).toString());
            replyItem.setDate(timeStamp.replaceAll("[(]|[)]", ""));
            replyItem.setAuth(authorization);
            replyItemList.add(replyItem);
        }
        replyListAdapter.notifyDataSetChanged();
    }

    private void actionSend(final String text) {
        String tag_string_req = "req_send";

        progressDialog.setMessage("전송중...");
        showProgressDialog();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.INSERT_REPLY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                source = new Source(response);
                replyItemList.clear();
                List<Element> commentList = source.getAllElementsByClass("comment-list");
                try {
                    fetchReplyData(commentList);
                    hideProgressDialog();
                    // 전송할때마다 리스트뷰 아래로
                    setListViewBottom();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    insertReplyToFirebase(commentList.get(commentList.size() - 1).getFirstElementByClass("comment-addr").getAttributeValue("id").replace("cmt_txt_", ""), text);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e(TAG, error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideProgressDialog();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", preferenceManager.getCookie());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("CLUB_GRP_ID", groupId);
                params.put("ARTL_NUM", articleId);
                params.put("CMT", text);
                return params;
            }
        };
        app.AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    /**
     * 리스트뷰 하단으로 간다.
     */
    private void setListViewBottom() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final int articleHeight = articleDetail.getMeasuredHeight();
                listView.setSelection(articleHeight);
            }
        }, 300);
    }

    private void deliveryUpdate(String title, String content, List<String> imageList, String replyCnt) {
        Intent intent = new Intent(getApplicationContext(), Tab1Fragment.class);
        intent.putExtra("position", position);
        intent.putExtra("sbjt", title);
        intent.putExtra("txt", content);
        intent.putExtra("img", imageList.size() > 0 ? imageList.get(0) : null);
        intent.putExtra("cmmt_cnt", replyCnt);

        setResult(RESULT_OK, intent);
    }

    private String contentExtractor(Element listCont, boolean isFlag) {
        StringBuilder sb = new StringBuilder();
        for (Element childElement : isFlag ? listCont.getChildElements() : listCont.getAllElements(HTMLElementName.P))
            sb.append(childElement.getTextExtractor().toString().concat("\n"));
        return sb.toString().trim();
    }

    private void insertReplyToFirebase(String replyId, String text) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        ReplyItem replyItem = new ReplyItem();
        replyItem.setId(replyId);
        replyItem.setUid(preferenceManager.getUser().getUid());
        replyItem.setName(preferenceManager.getUser().getName());
        replyItem.setTimestamp(System.currentTimeMillis());
        replyItem.setReply(text);

        databaseReference.child(articleKey).push().setValue(replyItem);
    }

    private void deleteArticleFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        databaseReference.child(groupKey).child(articleKey).removeValue();
    }

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
