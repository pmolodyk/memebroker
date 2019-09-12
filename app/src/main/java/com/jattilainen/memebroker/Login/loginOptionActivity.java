package com.jattilainen.memebroker.Login;

//NO UNAUTHORISED DB USAGE

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jattilainen.memebroker.BuildConfig;
import com.jattilainen.memebroker.Constants;
import com.jattilainen.memebroker.MainActivity;
import com.jattilainen.memebroker.R;
import com.jattilainen.memebroker.netManager;

import java.util.Locale;

public class loginOptionActivity extends AppCompatActivity {
    netManager netm = new netManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateLocale();
        requestWindowFeature(Window.FEATURE_NO_TITLE );
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login_option);
        Log.e("here", "onResume: ");
        checkForTechProblems();
    }
    private void checkForTechProblems() {
        FirebaseDatabase.getInstance().getReference().child(Constants.TECHNIQUE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    return;
                }
                Integer minAppVersion = dataSnapshot.child(Constants.MIN_APP_NAME).getValue(Integer.class);
                if (minAppVersion == null || (minAppVersion > BuildConfig.VERSION_CODE)) {
                    setContentView(R.layout.wrong_version_fragment);
                } else {
                    Integer isMaintenance = dataSnapshot.child(Constants.MAINTENANCE).getValue(Integer.class);
                    if (isMaintenance != null && isMaintenance == 0) {
                        setContentView(R.layout.activity_login_option);
                        normalAppStart();
                    } else {
                        setContentView(R.layout.maintenance_fragment);
                        TextView maintenanceText = findViewById(R.id.maintenance_text);
                        maintenanceText.setText(getString(R.string.maintenance_with_time, isMaintenance));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLocale()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String locale_id = prefs.getString("lang", null);
        if (locale_id != null)
        {
            Log.e("Locale fetched!", locale_id);
            Locale locale = new Locale(locale_id);
            Locale.setDefault(locale);
            android.content.res.Configuration config = new android.content.res.Configuration();
            config.locale = locale;
            this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        }

    }
    private void normalAppStart() {
        if (netm.signedIn()) {
            final String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            final DatabaseReference userref = FirebaseDatabase.getInstance().getReference().child("users").child(name);
            userref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.child("verified").exists() && dataSnapshot.child("verified").getValue(Integer.class) == 1) {
                            Intent signedinActivity = new Intent(loginOptionActivity.this, MainActivity.class);
                            startActivity(signedinActivity);
                            finish();
                        } else {
                            Intent verifyActivity = new Intent(loginOptionActivity.this, com.jattilainen.memebroker.Login.verifyActivity.class);
                            startActivity(verifyActivity);
                            finish();
                        }
                    } else {
                        FirebaseAuth.getInstance().signOut();
                        Intent registerActivity = new Intent(loginOptionActivity.this, com.jattilainen.memebroker.Login.registerActivity.class);
                        startActivity(registerActivity);
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    FirebaseAuth.getInstance().signOut();
                    Intent registerActivity = new Intent(loginOptionActivity.this, com.jattilainen.memebroker.Login.registerActivity.class);
                    startActivity(registerActivity);
                    finish();
                }
            });
        } else {
            Intent registerActivity = new Intent(loginOptionActivity.this, com.jattilainen.memebroker.Login.registerActivity.class);
            startActivity(registerActivity);
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage(R.string.sure_quit);

        // create and show the alert dialog
        builder.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // do something like...
                loginOptionActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
