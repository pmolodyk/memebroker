package com.jattilainen.memebroker.Help;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.jattilainen.memebroker.R;

public class HelpUploadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_upload);
        setSupportActionBar((Toolbar)findViewById(R.id.help_up_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
