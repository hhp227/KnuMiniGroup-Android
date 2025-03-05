package com.hhp227.knu_minigroup.activity;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;

import androidx.databinding.DataBindingUtil;
import com.hhp227.knu_minigroup.BuildConfig;
import com.hhp227.knu_minigroup.R;
import com.hhp227.knu_minigroup.databinding.ActivityVerinfoBinding;

public class VerInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityVerinfoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_verinfo);
        binding.textView2.append(BuildConfig.VERSION_NAME);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
