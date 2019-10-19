package com.hhp227.knu_minigroup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.hhp227.knu_minigroup.helper.PreferenceManager;

public class MainActivity extends Activity {
    private PreferenceManager preferenceManager;
    private String sessionId;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout = findViewById(R.id.bLogout);

        preferenceManager = new PreferenceManager(getApplicationContext());

        if(app.AppController.getInstance().getPreferenceManager().getUser() == null)
            logout();

        sessionId = app.AppController.getInstance().getPreferenceManager().getSessionId();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        preferenceManager.clear();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
