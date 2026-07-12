package com.hhp227.knu_minigroup.data;

import com.hhp227.knu_minigroup.data.local.TimetableLocalDataSource;
import com.hhp227.knu_minigroup.data.remote.TimetableRemoteDataSource;
import com.hhp227.knu_minigroup.helper.Callback;

public class TimetableRepository {
    private final TimetableLocalDataSource mTimetableLocalDataSource = new TimetableLocalDataSource();

    private final TimetableRemoteDataSource mTimetableRemoteDataSource = new TimetableRemoteDataSource();

    public void getTimetableList(Callback callback) {
        mTimetableLocalDataSource.getTimetableList(callback);
    }

    public void addTimetable(int id, String subject, String classroom, Callback callback) {
        mTimetableLocalDataSource.addTimetable(id, subject, classroom, callback);
    }

    public void updateTimetable(int id, String subject, String classroom, Callback callback) {
        mTimetableLocalDataSource.updateTimetable(id, subject, classroom, callback);
    }

    public void deleteTimetable(int id, Callback callback) {
        mTimetableLocalDataSource.deleteTimetable(id, callback);
    }

    /*학기 시간표 페이지를 파싱하여 행 단위의 텍스트 리스트로 반환, 첫번째 행은 요일 헤더*/
    public void getSemesterTimetableList(String cookie, Callback callback) {
        mTimetableRemoteDataSource.getSemesterTimetableList(cookie, callback);
    }

    public void close() {
        mTimetableLocalDataSource.close();
    }
}
