package com.jattilainen.memebroker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jattilainen.memebroker.Assets.AssetsAdapter;
import com.jattilainen.memebroker.Assets.AssetsFragment;
import com.jattilainen.memebroker.Help.HelpFragment;
import com.jattilainen.memebroker.Login.loginOptionActivity;
import com.jattilainen.memebroker.MemeLanguageChoose.MemeLangChooseActivity;
import com.jattilainen.memebroker.NewMemes.NewMemesAdapter;
import com.jattilainen.memebroker.NewMemes.NewMemesFragment;
import com.jattilainen.memebroker.NewMemes.NewMemesItem;
import com.jattilainen.memebroker.RandomMemes.RandomMemesAdapter;
import com.jattilainen.memebroker.RandomMemes.RandomMemesFragment;
import com.jattilainen.memebroker.ShortList.ShortlistFragment;
import com.jattilainen.memebroker.TopMemes.TopMemesAdapter;
import com.jattilainen.memebroker.TopMemes.TopMemesFragment;
import com.jattilainen.memebroker.TopPeople.TopPeopleFragment;
import com.jattilainen.memebroker.UploadedMemes.UploadedMemesAdapter;
import com.jattilainen.memebroker.UploadedMemes.UploadedMemesFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements  NavigationView.OnNavigationItemSelectedListener, RandomMemesFragment.OnButtonListener, AssetsAdapter.OnButtonListener, RandomMemesAdapter.OnButtonListener, TopMemesFragment.OnButtonListener, UploadedMemesAdapter.OnButtonListener, TopMemesAdapter.OnButtonListener, NewMemesAdapter.OnButtonListener, NewMemesFragment.OnButtonListener {
    RandomMemesFragment main = (new RandomMemesFragment());
    PreferenceFragmentCompat settings = (PreferenceFragmentCompat) (new SettingsFragment());
    TopMemesFragment topMemes = (new TopMemesFragment());
    NewMemesFragment newMemes = (new NewMemesFragment());
    ShortlistFragment shortlist = (new ShortlistFragment());
    UploadedMemesFragment uploadedMemes = (new UploadedMemesFragment());
    AssetsFragment assets = (new AssetsFragment());
    TopPeopleFragment topPeopleFragment = new TopPeopleFragment();
    HelpFragment help = new HelpFragment();
    Toolbar toolbar;
    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    public boolean inMaintenance = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        shortlist.setContext(MainActivity.this);
        main.setContext(MainActivity.this);
        topMemes.setContext(MainActivity.this);
        newMemes.setContext(MainActivity.this);
        assets.setContext(MainActivity.this);
        topPeopleFragment.setContext(MainActivity.this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent upload_intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(upload_intent);
            }
        });
        CheckForMaintenance();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navHeader = navigationView.getHeaderView(0);
        setNavHeaderContent(navHeader);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_main, main);
        ft.commit();
        setRandomMemesLang(-1);
    }
    @Override
    public void onAddButtonSelected(long currentPrice, String memeHash, String memeLanguage, double ratio) {
        Log.e("God", "onAddButtonSelected:");
        shortlist.addMeme(currentPrice, memeHash, memeLanguage, ratio);
    }
    @Override
    public void onReportButtonSelected(String memeHash, String memeLanguage)
    {
        Intent report_intent = new Intent(MainActivity.this, reportAbuseActivity.class);
        report_intent.putExtra("hash", memeHash);
        report_intent.putExtra("language", memeLanguage);
        startActivity(report_intent);
    }


    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            int flags = intent.getFlags();
            flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;//THANKS SO
            intent.setFlags(flags);
            requestCode = Constants.SEARCH_CODE;
        }
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onBackPressed() {
        //Toast.makeText(MainActivity.this, getString(R.string.action_settings), Toast.LENGTH_LONG).show();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("");
            builder.setMessage(R.string.sure_quit);

            // create and show the alert dialog
            builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // do something like...
                    MainActivity.super.onBackPressed();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = null;
        PreferenceFragmentCompat preferences = null;
        if (id == R.id.nav_meme) {
            fragment = main;
            setRandomMemesLang(-1);
        } else if (id == R.id.nav_uploaded) {
            fragment = uploadedMemes;
            toolbar.setTitle(R.string.uploadedMemes);
        } else if (id == R.id.nav_settings) {
            preferences = settings;
            toolbar.setTitle(R.string.action_settings);
        } else if (id == R.id.nav_shortlist) {
            fragment = shortlist;
            toolbar.setTitle(R.string.shortlist);
        } else if (id == R.id.nav_assets) {
            fragment = assets;
            toolbar.setTitle(R.string.assets);
        } else if (id == R.id.nav_top_memes) {
            fragment = topMemes;
            setTopMemesLang(-1);
        } else if (id == R.id.nav_new_memes) {
            fragment = newMemes;
            setNewMemesLang(-1);
        } else if (id == R.id.nav_top_people) {
            fragment = topPeopleFragment;
            toolbar.setTitle(R.string.topPeople);
        } else if (id == R.id.nav_help) {
            toolbar.setTitle(R.string.action_help);
            fragment = help;
        }
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, fragment);
            ft.commit();
        }
        if (preferences != null)
        {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, preferences);
            ft.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("langchoose", "almostlyfinish" + String.valueOf(requestCode));
        if (RESULT_OK == resultCode && requestCode == Constants.SEARCH_CODE) {
            String query = data.getStringExtra("query");
            main.setImage(query);
        } else if (resultCode == RESULT_OK && requestCode == Constants.MEME_LANG_CHOOSE_CODE) {
            Log.e("langchoose", "pochtifinish");
            setRandomMemesLang(data.getIntExtra("langId", -1));
        } else if (resultCode == RESULT_OK && requestCode == Constants.TOP_LANG_CHOOSE_CODE) {
            setTopMemesLang(data.getIntExtra("langId", -1));
        } else if (resultCode == RESULT_OK && requestCode == Constants.NEW_LANG_CHOOSE_CODE) {
            setNewMemesLang(data.getIntExtra("langId", -1));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRandomLanguageSelectButtonSelected() {
        Intent langIntent = new Intent(MainActivity.this, MemeLangChooseActivity.class);
        startActivityForResult(langIntent, Constants.MEME_LANG_CHOOSE_CODE);
    }
    @Override
    public void onTopLanguageSelectButtonSelected() {
        Intent langIntent = new Intent(MainActivity.this, MemeLangChooseActivity.class);
        startActivityForResult(langIntent, Constants.TOP_LANG_CHOOSE_CODE);
    }
    void setNavHeaderContent(View header) {
        TextView nameTextView = header.findViewById(R.id.navHeaderNameTextView);
        final TextView moneyTextView = header.findViewById(R.id.navHeaderMoneyTextView);
        final TextView respectTextView = header.findViewById(R.id.navHeaderRespectTextView);
        ImageView circleImageView = header.findViewById(R.id.navHeaderImageView);
        circleImageView.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher_round));
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(name);
        userRef.child("money").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long money = dataSnapshot.getValue(Long.class);
                moneyTextView.setText(getString(R.string.Money, money));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userRef.child("respect").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer respect = dataSnapshot.getValue(Integer.class);
                respectTextView.setText(getString(R.string.Respect, respect));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        nameTextView.setText(name);
    }
    void setRandomMemesLang(int langId) {
        String[] translatedLanguages = getResources().getStringArray(R.array.languages_array);
        String[] originalLanguages = getResources().getStringArray(R.array.original_languages);
        if (langId == -1) {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            langId = prefs.getInt(Constants.RANDOM_MEME_LANG_PREFS, -1);
            if (langId == -1) {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putInt(Constants.RANDOM_MEME_LANG_PREFS, 0);
                editor.apply();
                langId = 0;
            }
        } else {
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putInt(Constants.RANDOM_MEME_LANG_PREFS, langId);
            editor.apply();
        }
        main.setFinalLanguage(originalLanguages[langId]);
        getSupportActionBar().setTitle(getString(R.string.RandomMemesTitle, translatedLanguages[langId]));
    }
    void setTopMemesLang(int langId) {
        String[] originalLanguages = getResources().getStringArray(R.array.original_languages);
        String[] translatedLanguages = getResources().getStringArray(R.array.languages_not_capital);
        if (langId == -1) {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            langId = prefs.getInt(Constants.TOP_MEME_LANG_PREFS, -1);
            if (langId == -1) {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putInt(Constants.TOP_MEME_LANG_PREFS, 0);
                editor.apply();
                langId = 0;
            }
        } else {
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putInt(Constants.TOP_MEME_LANG_PREFS, langId);
            editor.apply();
        }
        topMemes.setFinalLanguage(originalLanguages[langId]);
        toolbar.setTitle(getString(R.string.TopMemesTitle, translatedLanguages[langId]));
    }
    void setNewMemesLang(int langId) {
        String[] originalLanguages = getResources().getStringArray(R.array.original_languages);
        String[] translatedLanguages = getResources().getStringArray(R.array.languages_not_capital);
        if (langId == -1) {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            langId = prefs.getInt(Constants.NEW_MEME_LANG_PREFS, -1);
            if (langId == -1) {
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putInt(Constants.NEW_MEME_LANG_PREFS, 0);
                editor.apply();
                langId = 0;
            }
        } else {
            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putInt(Constants.NEW_MEME_LANG_PREFS, langId);
            editor.apply();
        }
        newMemes.setFinalLanguage(originalLanguages[langId]);
        toolbar.setTitle(getString(R.string.NewMemesTitle, translatedLanguages[langId]));
    }
    void CheckForMaintenance() {
        FirebaseDatabase.getInstance().getReference().child(Constants.TECHNIQUE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }
                Integer isMaintenance = dataSnapshot.child(Constants.MAINTENANCE).getValue(Integer.class);
                Integer appVersion = dataSnapshot.child(Constants.MIN_APP_NAME).getValue(Integer.class);
                if ((isMaintenance != null && isMaintenance != 0 && !inMaintenance) || (appVersion == null || appVersion > BuildConfig.VERSION_CODE)) {
                    inMaintenance = true;
                    Intent maintenanceIntent = new Intent(MainActivity.this, loginOptionActivity.class);
                    startActivity(maintenanceIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNewLanguageSelectButtonSelected() {
        Intent langIntent = new Intent(MainActivity.this, MemeLangChooseActivity.class);
        startActivityForResult(langIntent, Constants.NEW_LANG_CHOOSE_CODE);
    }
}