package com.hhp227.knu_minigroup.data;

import com.hhp227.knu_minigroup.data.remote.ScheduleRemoteDataSource;
import com.hhp227.knu_minigroup.helper.Callback;

public class ScheduleRepository {
    private final ScheduleRemoteDataSource mScheduleRemoteDataSource = new ScheduleRemoteDataSource();

    public void getDCShuttleSchedule(Callback callback) {
        mScheduleRemoteDataSource.getDCShuttleSchedule(callback);
    }

    public void getSCShuttleSchedule(Callback callback) {
        mScheduleRemoteDataSource.getSCShuttleSchedule(callback);
    }
}
