package com.hhp227.knu_minigroup.handler;

import android.view.View;

public interface OnActivityArticleEventListener {
    void onReplyFocusChange(boolean hasFocus);

    void onRefresh();

    boolean onLongClick(View var1);
}
