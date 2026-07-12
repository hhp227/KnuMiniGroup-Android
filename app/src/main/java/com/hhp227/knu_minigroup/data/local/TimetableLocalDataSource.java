package com.hhp227.knu_minigroup.data.local;

import android.database.Cursor;

import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.dto.TimetableItem;
import com.hhp227.knu_minigroup.helper.Callback;
import com.hhp227.knu_minigroup.helper.TimetableHelper;

import java.util.ArrayList;
import java.util.List;

public class TimetableLocalDataSource {
    private TimetableHelper mTimetableHelper;

    public void getTimetableList(Callback callback) {
        callback.onLoading();
        try {
            callback.onSuccess(fetchTimetableList());
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void addTimetable(int id, String subject, String classroom, Callback callback) {
        callback.onLoading();
        try {
            getTimetableHelper().add(id, subject, classroom);
            callback.onSuccess(fetchTimetableList());
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void updateTimetable(int id, String subject, String classroom, Callback callback) {
        callback.onLoading();
        try {
            getTimetableHelper().update(id, subject, classroom);
            callback.onSuccess(fetchTimetableList());
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void deleteTimetable(int id, Callback callback) {
        callback.onLoading();
        try {
            getTimetableHelper().delete(id);
            callback.onSuccess(fetchTimetableList());
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }

    public void close() {
        if (mTimetableHelper != null) {
            mTimetableHelper.close();

            mTimetableHelper = null;
        }
    }

    private TimetableHelper getTimetableHelper() {
        if (mTimetableHelper == null) {
            mTimetableHelper = new TimetableHelper(AppController.getInstance());
        }
        return mTimetableHelper;
    }

    private List<TimetableItem> fetchTimetableList() {
        List<TimetableItem> timetableList = new ArrayList<>();
        Cursor cursor = getTimetableHelper().getAll();

        while (cursor.moveToNext()) {
            timetableList.add(new TimetableItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
        }
        cursor.close();
        return timetableList;
    }
}
