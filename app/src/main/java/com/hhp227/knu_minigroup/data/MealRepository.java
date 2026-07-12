package com.hhp227.knu_minigroup.data;

import com.hhp227.knu_minigroup.data.remote.MealRemoteDataSource;
import com.hhp227.knu_minigroup.helper.Callback;

public class MealRepository {
    public static final String KEY_BREAKFAST = "breakfast";
    public static final String KEY_LAUNCH = "lunch";
    public static final String KEY_DINNER = "dinner";

    private final MealRemoteDataSource mMealRemoteDataSource = new MealRemoteDataSource();

    public void getStudentMealList(int id, Callback callback) {
        mMealRemoteDataSource.getStudentMealList(id, callback);
    }

    public void getBTLDormMealList(Callback callback) {
        mMealRemoteDataSource.getBTLDormMealList(callback);
    }
}
