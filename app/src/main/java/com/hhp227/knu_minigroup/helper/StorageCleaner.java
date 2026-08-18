package com.hhp227.knu_minigroup.helper;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 참조를 잃은 Firebase Storage 이미지를 지운다.
 *
 * 게시글 수정/삭제, 그룹 커버 교체/그룹 삭제는 RTDB에서 URL만 빼거나 덮어쓰기 때문에
 * 그대로 두면 Storage에 고아 파일이 쌓인다. 정리 규칙은 전부 여기에 모아둔다.
 *
 * <ul>
 *     <li>Storage 주소가 아닌 URL(LMS 시절 이미지 등)은 조용히 건너뛴다.</li>
 *     <li>삭제 실패는 로그만 남긴다 — 사용자의 수정/삭제 동작까지 실패시키지 않는다.</li>
 *     <li>호출은 반드시 <b>RTDB 반영이 성공한 뒤에</b> — 순서가 뒤집히면 갱신 실패 시 파일만 사라진다.</li>
 * </ul>
 */
public final class StorageCleaner {
    private static final String TAG = "StorageCleaner";

    private StorageCleaner() {
    }

    /**
     * 갱신 전후 목록을 비교해 빠진 이미지만 지운다. (게시글 수정)
     */
    public static void deleteRemoved(Collection<String> oldUrls, Collection<String> newUrls) {
        if (oldUrls == null || oldUrls.isEmpty()) {
            return;
        }
        Set<String> retained = newUrls != null ? new HashSet<>(newUrls) : Collections.<String>emptySet();
        List<String> removed = new ArrayList<>();

        for (String url : oldUrls) {
            if (!retained.contains(url)) {
                removed.add(url);
            }
        }
        delete(removed);
    }

    public static void delete(Collection<String> urls) {
        if (urls == null) {
            return;
        }
        for (String url : urls) {
            delete(url);
        }
    }

    public static void delete(String url) {
        StorageReference storageReference = toReference(url);

        if (storageReference == null) {
            return;
        }
        storageReference.delete().addOnFailureListener(e -> {
            if (e instanceof StorageException && ((StorageException) e).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                return; // 이미 지워진 파일 - 정상으로 본다
            }
            Log.w(TAG, "이미지 삭제 실패: " + url, e);
        });
    }

    /**
     * 다운로드 URL을 Storage 참조로 바꾼다. 우리 버킷 주소가 아니면 지울 것이 없으므로 null.
     */
    private static StorageReference toReference(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        try {
            return FirebaseStorage.getInstance().getReferenceFromUrl(url);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
