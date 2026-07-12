package com.hhp227.knu_minigroup.viewmodel;

import com.hhp227.knu_minigroup.data.TimetableRepository;
import com.hhp227.knu_minigroup.dto.TimetableItem;
import com.hhp227.knu_minigroup.helper.Callback;

import java.util.List;

public class MockTimeTableViewModel extends ListViewModel<TimetableItem> {
    private final TimetableRepository mTimetableRepository = new TimetableRepository();

    private final String[] mTimeLine = {"1교시\n09:00", "2교시\n10:00", "3교시\n11:00", "4교시\n12:00", "5교시\n13:00", "6교시\n14:00", "7교시\n15:00", "8교시\n16:00", "9교시\n17:00", "10교시\n18:00"};

    private final String[] mDayLine = {"시간", "월", "화", "수", "목", "금"};

    private final Callback mCallback = new Callback() {
        @Override
        public <T> void onSuccess(T data) {
            setLoading(false);
            setItemList((List<TimetableItem>) data);
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
    };

    public MockTimeTableViewModel() {
        fetchDataTask();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mTimetableRepository.close();
    }

    public String[] getTimeLine() {
        return mTimeLine;
    }

    public String[] getDayLine() {
        return mDayLine;
    }

    public TimetableItem getTimetableItem(int id) {
        List<TimetableItem> itemList = getItemList().getValue();

        if (itemList != null) {
            for (TimetableItem timetableItem : itemList) {
                if (timetableItem.id == id) {
                    return timetableItem;
                }
            }
        }
        return null;
    }

    public void addTimetable(int id, String subject, String classroom) {
        mTimetableRepository.addTimetable(id, subject, classroom, mCallback);
    }

    public void updateTimetable(int id, String subject, String classroom) {
        mTimetableRepository.updateTimetable(id, subject, classroom, mCallback);
    }

    public void deleteTimetable(int id) {
        mTimetableRepository.deleteTimetable(id, mCallback);
    }

    private void fetchDataTask() {
        mTimetableRepository.getTimetableList(mCallback);
    }
}
