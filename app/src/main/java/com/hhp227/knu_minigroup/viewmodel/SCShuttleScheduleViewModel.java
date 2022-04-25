package com.hhp227.knu_minigroup.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hhp227.knu_minigroup.app.EndPoint;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SCShuttleScheduleViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public final ArrayList<HashMap<String, String>> mShuttleList = new ArrayList<>();

    private Source mSource;

    public SCShuttleScheduleViewModel() {
        mState.postValue(new State(true, new ArrayList<>(), null));
        fetchDataTask();
    }

    public void refresh() {
        mShuttleList.clear();
        fetchDataTask();
    }

    private void fetchDataTask() {
        try {
            new Thread() {
                public void run() {
                    try {
                        URL URL = new URL(EndPoint.URL_SHUTTLE.replace("{SHUTTLE}", "map03_02"));
                        InputStream html = URL.openStream();
                        mSource = new Source(new InputStreamReader(html, StandardCharsets.UTF_8)); // 소스를 UTF-8 인코딩으로 불러온다.

                        mSource.fullSequentialParse(); // 순차적으로 구문분석
                        Element table = mSource.getAllElements(HTMLElementName.TABLE).get(0);
                        List<Element> thList = table.getFirstElement(HTMLElementName.TR).getAllElements(HTMLElementName.TH);
                        List<String> result = new ArrayList<>();

                        for (int i = 1; i < table.getAllElements(HTMLElementName.TR).size(); i++) {
                            Element TR = table.getAllElements(HTMLElementName.TR).get(i);
                            HashMap<String, String> map = new HashMap<>();
                            Element Col1 = TR.getAllElements(HTMLElementName.TD).get(0);
                            Element Col2 = TR.getAllElements(HTMLElementName.TD).get(1);
                            Element Col3 = TR.getAllElements(HTMLElementName.TD).get(2);
                            Element Col4 = TR.getAllElements(HTMLElementName.TD).get(3);
                            Element Col5 = TR.getAllElements(HTMLElementName.TD).get(4);
                            Element Col6 = TR.getAllElements(HTMLElementName.TD).get(5);

                            map.put("col1", String.valueOf(i));
                            map.put("col2", (Col1).getContent().toString());
                            map.put("col3", (Col2).getContent().toString());
                            map.put("col4", (Col3).getContent().toString());
                            map.put("col5", (Col4).getContent().toString());
                            map.put("col6", (Col5).getContent().toString());
                            map.put("col7", (Col6).getContent().toString());
                            mShuttleList.add(map);
                        }
                        for (int i = 0; i < thList.size(); i++) {
                            result.add(thList.get(i).getTextExtractor().toString());
                        }
                        mState.postValue(new SCShuttleScheduleViewModel.State(false, result, null));
                    } catch (Exception e) {
                        mState.postValue(new SCShuttleScheduleViewModel.State(false, new ArrayList<>(), e.getMessage()));
                    }
                }
            }.start();
        } catch (Exception e) {
            mState.postValue(new State(false, new ArrayList<>(), e.getMessage()));
        }
    }

    public static final class State {
        public boolean isLoading;

        public List<String> list;

        public String message;

        public State(boolean isLoading, List<String> list, String message) {
            this.isLoading = isLoading;
            this.list = list;
            this.message = message;
        }
    }
}
