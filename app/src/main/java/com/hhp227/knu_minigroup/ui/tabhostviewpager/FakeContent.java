package com.hhp227.knu_minigroup.ui.tabhostviewpager;

import android.content.Context;
import android.view.View;
import android.widget.TabHost;

public class FakeContent implements TabHost.TabContentFactory {
    Context context;

    public FakeContent(Context context) {
        this.context = context;
    }

    @Override
    public View createTabContent(String tag) {
        View view = new View(context);
        view.setMinimumWidth(0);
        view.setMinimumHeight(0);
        return view;
    }
}
