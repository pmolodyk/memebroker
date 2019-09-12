package com.jattilainen.memebroker;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class reportAbuseActivity extends Activity {
    String category;
    String hash;
    String language;
    Button submit;
    Button cancel;
    String uname = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_report_abuse);
        category = "language";
        submit = findViewById(R.id.button_submit_report);
        cancel = findViewById(R.id.button_cancel_report);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        hash = getIntent().getStringExtra("hash");
        language = getIntent().getStringExtra("language");
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference clarRef = FirebaseDatabase.getInstance().getReference().child("clars").child(language).child(hash);
                final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("clars").child(language).child(hash).child("users").child(uname);
                clarRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                        {
                            clarRef.child("clars").child(category).setValue(1);
                            clarRef.child("time").setValue(System.currentTimeMillis());
                            userRef.setValue(category);
                            increaseMemeReports(hash);
                        }
                        else
                        {
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists())
                                    {
                                        Toast.makeText(getBaseContext(), R.string.already_reported, Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        if (dataSnapshot.child("clars").hasChild(category))
                                        {
                                            Integer val = dataSnapshot.child("clars").child(category).getValue(Integer.class);
                                            clarRef.child("clars").child(category).setValue(val + 1);
                                        }
                                        else
                                        {
                                            clarRef.child("clars").child(category).setValue(1);
                                            clarRef.setValue(1);
                                        }
                                        userRef.setValue(category);
                                        increaseMemeReports(hash);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                finish();
            }
        });
    }

    public void onRadioButtonClicked(View v)
    {
        boolean checked = ((RadioButton) v).isChecked();
        switch(v.getId())
        {
            case R.id.radio_adult:
                if (checked)
                {
                    category = "adult";
                }
                break;
            case R.id.radio_illegal:
                if (checked)
                {
                    category = "illegal";
                }
                break;
            case R.id.radio_medical:
                if (checked)
                {
                    category = "medical";
                }
                break;
            case R.id.radio_racism:
                if (checked)
                {
                    category = "racism";
                }
                break;
            case R.id.radio_spam:
                if (checked)
                {
                    category = "spam";
                }
                break;
            case R.id.radio_violence:
                if (checked)
                {
                    category = "violence";
                }
                break;
            case R.id.radio_language:
                if (checked)
                {
                    category = "language";
                }
                break;
        }
    }
    public void increaseMemeReports(final String hash) {
        FirebaseDatabase.getInstance().getReference().child("images").child("all").child("images").child(hash).child("language").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot langSnapshot) {
                FirebaseDatabase.getInstance().getReference().child("images").child(langSnapshot.getValue(String.class))
                        .child("images").child(hash).child("reports").runTransaction(new Transaction.Handler() {
                    @NonNull

                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        Integer reports = mutableData.getValue(Integer.class);
                        if (reports == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(reports + 1);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
