package com.hhp227.knu_minigroup.viewmodel;

import android.util.Log;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class ArticleViewModel extends ViewModel {
    private SavedStateHandle mSavedStateHandle;

    public ArticleViewModel(SavedStateHandle savedStateHandle) {
        this.mSavedStateHandle = savedStateHandle;
        Log.e("TEST", "ArticleViewModel init");
        Log.e("TEST", "???" + savedStateHandle.get("grp_id"));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.e("TEST", "ArticleViewModel onCleared");
    }
}
