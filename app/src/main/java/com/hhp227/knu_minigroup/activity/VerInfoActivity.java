package com.hhp227.knu_minigroup.activity;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;

import com.hhp227.knu_minigroup.BuildConfig;
import com.hhp227.knu_minigroup.databinding.ActivityVerinfoBinding;

public class VerInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityVerinfoBinding binding = ActivityVerinfoBinding.inflate(getLayoutInflater());
        binding.textView2.append(BuildConfig.VERSION_NAME);

        setContentView(binding.getRoot());
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
