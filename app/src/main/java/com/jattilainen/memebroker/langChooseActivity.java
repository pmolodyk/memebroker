package com.jattilainen.memebroker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RadioButton;

public class langChooseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lang_choose);
        this.setFinishOnTouchOutside(false);

    }

    public void onLanguageChoice(View v)
    {
        boolean checked = ((RadioButton) v).isChecked();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        switch(v.getId())
        {
            case R.id.english_lang:
                if (checked)
                {
                    editor.putString("lang", "en");
                    editor.apply();
                    setResult(RESULT_OK, getIntent().putExtra("lang", "en"));
                    finish();
                }
                break;
            case R.id.russian_lang:
                if (checked)
                {
                    editor.putString("lang", "ru");
                    editor.apply();
                    setResult(RESULT_OK, getIntent().putExtra("lang", "ru"));
                    finish();
                }
                break;
        }
        finish();
    }
}
