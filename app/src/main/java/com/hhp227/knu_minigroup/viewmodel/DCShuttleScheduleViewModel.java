package com.hhp227.knu_minigroup.viewmodel;

import com.hhp227.knu_minigroup.data.ScheduleRepository;
import com.hhp227.knu_minigroup.helper.Callback;

import java.util.List;
import java.util.Map;

public class DCShuttleScheduleViewModel extends ListViewModel<Map<String, String>> {
    private final ScheduleRepository mScheduleRepository;

    public DCShuttleScheduleViewModel() {
        mScheduleRepository = new ScheduleRepository();

        fetchDataTask();
    }

    public void refresh() {
        fetchDataTask();
    }

    private void fetchDataTask() {
        mScheduleRepository.getDCShuttleSchedule(new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                List<Map<String, String>> shuttleList = (List<Map<String, String>>) data;

                setLoading(false);
                setItemList(shuttleList);
                setEndReached(true);
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
}
