package com.hhp227.knu_minigroup.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.hhp227.knu_minigroup.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {
    public static String URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWebViewBinding binding = ActivityWebViewBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        WebSettings webSettings = binding.wvNotice.getSettings();
        URL = getIntent().getStringExtra(URL);

        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("title"));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        binding.wvNotice.loadUrl(URL);
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
}
