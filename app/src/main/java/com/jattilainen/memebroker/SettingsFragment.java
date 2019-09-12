package com.jattilainen.memebroker;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jattilainen.memebroker.Login.loginOptionActivity;


public class SettingsFragment extends PreferenceFragmentCompat {
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    @Override
    public void onCreate(Bundle savedInstancestate)
    {
        super.onCreate(savedInstancestate);
        addPreferencesFromResource(R.xml.fragment_settings);
        android.support.v7.preference.Preference logoutPref = findPreference("LOGOUT_PREF"),
        changePassPref = findPreference("PASS_PREF");
        logoutPref.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.support.v7.preference.Preference preference) {
                FirebaseAuth.getInstance().signOut();
                Intent login_intent = new Intent(getActivity(), loginOptionActivity.class);
                startActivity(login_intent);
                if (getActivity() != null)
                {
                    getActivity().finish();
                }
                else
                {
                    Log.e("Logout: ", "someone killed activity before!");
                }
                return false;
            }
        });
        changePassPref.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.support.v7.preference.Preference preference) {
                Intent pass_intent = new Intent(getActivity(), changePasswordActivity.class);
                startActivity(pass_intent);
                return false;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle b, String s)
    {

    }
}
