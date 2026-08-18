package com.hhp227.knu_minigroup.data.remote;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.dto.ArticleItem;
import com.hhp227.knu_minigroup.dto.User;
import com.hhp227.knu_minigroup.dto.YouTubeItem;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.helper.StorageCleaner;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleRemoteDataSource {
    private final String mGroupId, mGroupKey;

    private String mLastKey = null; // 마지막으로 가져온 데이터의 키

    private boolean mStopRequestMore = false;

    public ArticleRemoteDataSource(String groupId, String key) {
        this.mGroupId = groupId;
        this.mGroupKey = key;
    }

    public boolean isStopRequestMore() {
        return mStopRequestMore;
    }

    public void setLastKey(String lastKey) {
        this.mLastKey = lastKey;
    }

    public void getArticleList(String cookie, int limit, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        Query query = databaseReference.child(mGroupKey).orderByKey().limitToLast(limit);

        if (mLastKey != null) {
            query = query.endBefore(mLastKey);
        }
        callback.onLoading();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newLastKey = null;
                List<Map.Entry<String, ArticleItem>> articleItemList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ArticleItem value = snapshot.getValue(ArticleItem.class);

                    if (articleItemList.isEmpty()) {
                        newLastKey = key; // 마지막 키 저장
                    }
                    if (value != null) {
                        articleItemList.add(0, new AbstractMap.SimpleEntry<>(key, value));
                    }
                }
                if (newLastKey == null) {
                    mStopRequestMore = true;
                }
                mLastKey = newLastKey; // 다음 페이지 요청을 위해 키 업데이트
                callback.onSuccess(articleItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
    }

    public void getArticleData(String articleKey, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        callback.onLoading();
        databaseReference.child(mGroupKey).child(articleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem value = dataSnapshot.getValue(ArticleItem.class);

                if (value != null) {
                    callback.onSuccess(value);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public void addArticle(User user, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        DatabaseReference article = databaseReference.child(mGroupKey).push();

        article.setValue(new HashMap<String, Object>() {
            {
                put("uid", user.getUid());
                put("name", user.getName());
                put("title", title);
                put("timestamp", System.currentTimeMillis());
                put("content", TextUtils.isEmpty(content) ? null : content);
                put("images", imageList);
                put("youtube", youTubeItem);
            }
        });
        callback.onSuccess(article.getKey());
    }

    public void setArticle(String articleKey, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        Query query = databaseReference.child(mGroupKey).child(articleKey);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);

                if (articleItem != null) {
                    // 덮어쓰기 전에 기존 목록을 떠둔다. 수정 화면에서 빠진 이미지는 이 차집합으로만 알 수 있다.
                    final List<String> oldImageList = articleItem.getImages() != null
                            ? new ArrayList<>(articleItem.getImages())
                            : null;

                    articleItem.setTitle(title);
                    articleItem.setContent(TextUtils.isEmpty(content) ? null : content);
                    articleItem.setImages(imageList.isEmpty() ? null : imageList);
                    articleItem.setYoutube(youTubeItem);
                    query.getRef().setValue(articleItem, (error, reference) -> {
                        // 수정이 실제로 반영된 뒤에 지워야 실패 시 파일만 날아가는 일이 없다.
                        if (error == null) {
                            StorageCleaner.deleteRemoved(oldImageList, imageList);
                        }
                    });
                    callback.onSuccess(articleItem);
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                callback.onFailure(databaseError.toException());
            }
        });
    }

    public void removeArticle(String articleKey, Callback callback) {
        DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");
        DatabaseReference articleReference = articlesReference.child(mGroupKey).child(articleKey);

        callback.onLoading();

        // 글을 지우고 나면 이미지 목록을 알 수 없으므로 먼저 읽어둔다.
        articleReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);

                removeArticleData(articleReference, replysReference, articleKey, callback);
                if (articleItem != null) {
                    StorageCleaner.delete(articleItem.getImages());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 이미지 목록을 못 읽어도 글 삭제 자체는 진행한다.
                removeArticleData(articleReference, replysReference, articleKey, callback);
            }
        });
    }

    private void removeArticleData(DatabaseReference articleReference, DatabaseReference replysReference, String articleKey, Callback callback) {
        articleReference.removeValue();
        replysReference.child(articleKey).removeValue();
        callback.onSuccess(null);
    }

    /**
     * LMS 서버가 닫혀 이미지 업로드 엔드포인트를 쓸 수 없으므로 Firebase Storage에 올리고 다운로드 URL을 돌려준다.
     * 상위 계층은 URL 문자열만 받으므로 기존 계약(onSuccess(String))은 그대로다.
     *
     * @param cookie 로그인 시 CookieManager에 넣어둔 Firebase uid (LoginViewModel 참고)
     */
    public void addArticleImage(String cookie, Bitmap bitmap, Callback callback) {
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference("article_images")
                    .child(resolveUid(cookie))
                    .child(System.currentTimeMillis() + ".jpg");

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            callback.onLoading();
            storageReference.putBytes(byteArrayOutputStream.toByteArray())
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return storageReference.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                    .addOnFailureListener(e -> {
                        Log.e("ArticleRemoteDataSource", "이미지 업로드 실패", e);
                        callback.onFailure(e);
                    });
        } else {
            callback.onFailure(new IllegalArgumentException("이미지가 비어있습니다."));
        }
    }

    /**
     * 저장 경로를 유저별로 나누기 위한 uid. 쿠키에 값이 없으면 현재 로그인 세션에서 채운다.
     */
    private String resolveUid(String cookie) {
        if (!TextUtils.isEmpty(cookie)) {
            return cookie;
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        return firebaseUser != null ? firebaseUser.getUid() : "anonymous";
    }

    private void fetchArticleDataFromFirebase(ArticleItem articleItem, String articleKey, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        databaseReference.child(mGroupKey).child(articleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem value = dataSnapshot.getValue(ArticleItem.class);

                if (value != null) {
                    articleItem.setUid(value.getUid());
                }
                callback.onSuccess(articleItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private String contentExtractor(Element listCont) {
        StringBuilder sb = new StringBuilder();

        for (Element childElement : listCont.getChildElements()) {
            sb.append(childElement.getTextExtractor().toString().concat("\n"));
        }
        return sb.toString().trim();
    }

    private List<String> imageExtract(Element listCont) {
        List<String> result = new ArrayList<>();

        for (Element p : listCont.getAllElements(HTMLElementName.P)) {
            try {
                if (p.getFirstElement(HTMLElementName.IMG) != null) {
                    Element image = p.getFirstElement(HTMLElementName.IMG);
                    String imageUrl = !image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src");

                    result.add(imageUrl);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private YouTubeItem youtubeExtract(Element listCont) {
        YouTubeItem youTubeItem = null;
        int position = 0;

        for (Element p : listCont.getAllElements(HTMLElementName.P)) {
            try {
                if (p.getFirstElement(HTMLElementName.IMG) != null) {
                    position++;
                } else if (p.getFirstElementByClass("youtube-player") != null) {
                    Element youtube = p.getFirstElementByClass("youtube-player");
                    String youtubeUrl = youtube.getAttributeValue("src");
                    String youtubeId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1, youtubeUrl.lastIndexOf("?"));
                    String thumbnail = "https://i.ytimg.com/vi/" + youtubeId + "/mqdefault.jpg";
                    youTubeItem = new YouTubeItem(youtubeId, null, null, thumbnail, null);
                    youTubeItem.position = position;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return youTubeItem;
    }
}