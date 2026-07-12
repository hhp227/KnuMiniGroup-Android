package com.hhp227.knu_minigroup.viewmodel;

import com.hhp227.knu_minigroup.app.AppController;
import com.hhp227.knu_minigroup.app.EndPoint;
import com.hhp227.knu_minigroup.data.TimetableRepository;
import com.hhp227.knu_minigroup.helper.Callback;

import java.util.List;

public class SemesterTimeTableViewModel extends ListViewModel<List<String>> {
    private final TimetableRepository mTimetableRepository = new TimetableRepository();

    public SemesterTimeTableViewModel() {
        fetchDataTask();
    }

    private void fetchDataTask() {
        mTimetableRepository.getSemesterTimetableList(AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN), new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setLoading(false);
                setItemList((List<List<String>>) data);
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
