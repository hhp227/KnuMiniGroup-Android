package com.hhp227.knu_minigroup.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWebViewBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view);
        WebSettings webSettings = binding.wvNotice.getSettings();
        String url = getIntent().getStringExtra("url");

        setAppBar();
        binding.wvNotice.loadUrl(url);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);

        // 모바일에서 자바스크립트를 실행 시키기 위한 용도
        webSettings.setJavaScriptEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void setAppBar() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("title"));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
