package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.hhp227.knu_minigroup.adapter.WriteListAdapter;

import java.util.ArrayList;
import java.util.List;

public class WriteActivity extends Activity {
    private EditText inputTitle, inputContent;
    private List<String> contents;
    private ListView listView;
    private View headerView;
    private WriteListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        listView = findViewById(R.id.lv_write);
        headerView = getLayoutInflater().inflate(R.layout.write_text, null, false);
        inputTitle = headerView.findViewById(R.id.et_title);
        inputContent = headerView.findViewById(R.id.et_content);
        contents = new ArrayList<>();
        listAdapter = new WriteListAdapter(getApplicationContext(), R.layout.write_content, contents);

        listView.addHeaderView(headerView);
        listView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case android.R.id.home :
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                return true;
            case R.id.action_send :
                String title = inputTitle.getText().toString().trim();
                String content = inputContent.getText().toString().trim();
                if (!title.isEmpty()) {

                } else
                    Toast.makeText(getApplicationContext(), "제목을 입력하세요", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
