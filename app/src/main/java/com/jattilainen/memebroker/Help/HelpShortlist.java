package com.jattilainen.memebroker.Help;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.jattilainen.memebroker.R;

public class HelpShortlist extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_shortlist);
        setSupportActionBar((Toolbar)findViewById(R.id.help_shortlist_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
