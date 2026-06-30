package com.hhp227.knu_minigroup.viewmodel;

import com.hhp227.knu_minigroup.data.MealRepository;
import com.hhp227.knu_minigroup.helper.Callback;

import java.util.ArrayList;
import java.util.List;

public class BTLDormMealViewModel extends ListViewModel<String> {
    private final MealRepository mMealRepository = new MealRepository();

    public BTLDormMealViewModel() {
        fetchDataTask();
    }

    private void fetchDataTask() {
        mMealRepository.getBTLDormMealList(new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setLoading(false);
                setItemList((List<String>) data);
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setItemList(new ArrayList<String>());
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setLoading(true);
            }
        });
    }
}
