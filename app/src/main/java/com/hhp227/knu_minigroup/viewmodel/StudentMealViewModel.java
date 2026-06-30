package com.hhp227.knu_minigroup.viewmodel;

import android.util.Pair;

import androidx.lifecycle.SavedStateHandle;

import com.hhp227.knu_minigroup.data.MealRepository;
import com.hhp227.knu_minigroup.helper.Callback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentMealViewModel extends ListViewModel<Pair<String, String>> {
    public static final String KEY_BREAKFAST = MealRepository.KEY_BREAKFAST;
    public static final String KEY_LAUNCH = MealRepository.KEY_LAUNCH;
    public static final String KEY_DINNER = MealRepository.KEY_DINNER;

    private final MealRepository mMealRepository = new MealRepository();

    public StudentMealViewModel(SavedStateHandle savedStateHandle) {
        fetchDataTask(savedStateHandle.get("id"));
    }

    private void fetchDataTask(int id) {
        mMealRepository.getStudentMealList(id, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setLoading(false);
                setItemList((List<Pair<String, String>>) data);
            }

            @Override
            public void onFailure(Throwable throwable) {
                setLoading(false);
                setMessage(throwable.getMessage());
            }

            @Override
            public void onLoading() {
                setLoading(true);
            }
        });
    }

    public static Map<String, List<Pair<String, String>>> groupBy(List<Pair<String, String>> arrayList) {
        Map<String, List<Pair<String, String>>> destination = new LinkedHashMap<>();

        for (Pair<String, String> it : arrayList) {
            String key = it.first;
            List<Pair<String, String>> value = destination.get(key);
            List<Pair<String, String>> list;

            if (value == null) {
                List<Pair<String, String>> answer = new ArrayList<>();
                list = answer;

                destination.put(key, answer);
            } else {
                list = value;
            }
            list.add(it);
        }
        return destination;
    }
}
