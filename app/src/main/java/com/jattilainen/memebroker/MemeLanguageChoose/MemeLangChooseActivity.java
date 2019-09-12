package com.jattilainen.memebroker.MemeLanguageChoose;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.jattilainen.memebroker.R;

import java.util.ArrayList;
import java.util.List;

public class MemeLangChooseActivity extends AppCompatActivity implements  MemeLangChooseAdapter.OnButtonListener{
    private List<MemeLanguage> languages = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_lang_choose);
        setSupportActionBar((Toolbar)findViewById(R.id.meme_lang_choose_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String[] originalLanguages = getResources().getStringArray(R.array.original_languages);
        String[] translatedLanguages = getResources().getStringArray(R.array.languages_array);
        for(int i = 0; i < originalLanguages.length; i++) {
            languages.add(new MemeLanguage(translatedLanguages[i], originalLanguages[i]));
        }
        MemeLangChooseAdapter adapter = new MemeLangChooseAdapter(languages, MemeLangChooseActivity.this);

        RecyclerView recyclerView = findViewById(R.id.memeLangChooseRecycleView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(MemeLangChooseActivity.this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onLangSelected(String memeLanguage, String langTranslation) {
        Log.e("langchoose", "pseredine");
        String[] translatedLanguages = getResources().getStringArray(R.array.languages_array);
        for(int i = 0; i < translatedLanguages.length; i++) {
            if (langTranslation.equals(translatedLanguages[i])) {
                setResult(RESULT_OK, getIntent().putExtra("langId", i));
                finish();
                break;
            }
        }
    }
}
