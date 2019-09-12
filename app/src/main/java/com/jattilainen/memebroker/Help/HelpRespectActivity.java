package com.jattilainen.memebroker.Help;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.jattilainen.memebroker.R;

public class HelpRespectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respect_help);
        setSupportActionBar((Toolbar)findViewById(R.id.help_respect_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
