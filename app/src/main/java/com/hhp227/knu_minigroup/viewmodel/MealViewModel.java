package com.hhp227.knu_minigroup.viewmodel;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MealViewModel extends ViewModel {
    private static final int TYPE_STUDENT = 0;
    private static final int TYPE_DC_DORM = 1;
    private static final int TYPE_BTL_DORM = 2;
    private static final int TYPE_SC_DORM = 3;

    private final List<MealPage> mMealPages;

    public MealViewModel() {
        List<MealPage> mealPages = new ArrayList<>();

        mealPages.add(MealPage.student("GP감꽃푸드코트", 46));
        mealPages.add(MealPage.student("GP일청담", 57));
        mealPages.add(MealPage.student("공학관 교직원식당", 85));
        mealPages.add(MealPage.student("공학관 학생식당", 86));
        mealPages.add(MealPage.student("복지관 교직원식당", 36));
        mealPages.add(MealPage.student("복지관 학생식당", 37));
        mealPages.add(MealPage.student("복현회관 교직원식당", 39));
        mealPages.add(MealPage.student("복현회관 학생식당", 56));
        mealPages.add(MealPage.student("정보센터", 35));
        mealPages.add(MealPage.student("상주 학식", 49));
        mealPages.add(MealPage.dorm("문화관", TYPE_DC_DORM));
        mealPages.add(MealPage.dorm("BTL", TYPE_BTL_DORM));
        mealPages.add(MealPage.dorm("상주생활관", TYPE_SC_DORM));
        mMealPages = Collections.unmodifiableList(mealPages);
    }

    public List<MealPage> getMealPages() {
        return mMealPages;
    }

    public List<String> getTabNames() {
        List<String> tabNames = new ArrayList<>();

        for (MealPage mealPage : mMealPages) {
            tabNames.add(mealPage.getTitle());
        }
        return tabNames;
    }

    public int getOffscreenPageLimit() {
        return mMealPages.size();
    }

    public static final class MealPage {
        private final String mTitle;

        private final int mType;

        private final int mStudentMealId;

        private MealPage(String title, int type, int studentMealId) {
            mTitle = title;
            mType = type;
            mStudentMealId = studentMealId;
        }

        static MealPage student(String title, int studentMealId) {
            return new MealPage(title, TYPE_STUDENT, studentMealId);
        }

        static MealPage dorm(String title, int type) {
            return new MealPage(title, type, 0);
        }

        public String getTitle() {
            return mTitle;
        }

        public int getStudentMealId() {
            return mStudentMealId;
        }

        public boolean isStudentMeal() {
            return mType == TYPE_STUDENT;
        }

        public boolean isDCDormMeal() {
            return mType == TYPE_DC_DORM;
        }

        public boolean isBTLDormMeal() {
            return mType == TYPE_BTL_DORM;
        }

        public boolean isSCDormMeal() {
            return mType == TYPE_SC_DORM;
        }
    }
}
