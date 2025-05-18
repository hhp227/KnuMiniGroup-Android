package com.hhp227.knu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hhp227.knu_minigroup.data.ScheduleRepository;
import com.hhp227.knu_minigroup.helper.Callback;

import java.util.*;

public class SCShuttleScheduleViewModel extends ListViewModel<HashMap<String, String>> {

    private final MutableLiveData<List<String>> mTitleList = new MutableLiveData<>();

    private final ScheduleRepository mScheduleRepository;

    public SCShuttleScheduleViewModel() {
        mScheduleRepository = new ScheduleRepository();

        fetchDataTask();
    }

    public LiveData<List<String>> getTitleList() {
        return mTitleList;
    }

    private void setTitleList(List<String> titleList) {
        mTitleList.postValue(titleList);
    }

    public void refresh() {
        fetchDataTask();
    }

    private void fetchDataTask() {
        mScheduleRepository.getSCShuttleSchedule(new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                AbstractMap.SimpleEntry<ArrayList<HashMap<String, String>>, List<String>> result = (AbstractMap.SimpleEntry<ArrayList<HashMap<String, String>>, List<String>>) data;

                setLoading(false);
                setItemList(result.getKey());
                setTitleList(result.getValue());
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
