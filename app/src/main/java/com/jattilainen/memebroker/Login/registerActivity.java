package com.jattilainen.memebroker.Login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jattilainen.memebroker.R;

import java.util.Random;

public class registerActivity extends AppCompatActivity {
    Button register;
    EditText username;
    EditText email;
    Random rand;
    ProgressDialog pd;
    TextView policyTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("register","start");
        setContentView(R.layout.activity_register);
        policyTV = findViewById(R.id.register_policy);
        policyTV.setMovementMethod(LinkMovementMethod.getInstance());
        rand = new Random();
        register = findViewById(R.id.regButton);
        username = findViewById(R.id.username);
        email = findViewById(R.id.user_email);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(registerActivity.this, "", getString(R.string.signUpProgress), true);
                final String uname = username.getText().toString();
                final String mail = email.getText().toString();
                final String psswd = String.valueOf(rand.nextInt(100000000) + 100000000);
                if (!uname.matches("[A-Za-z0-9]+"))
                {
                    Toast.makeText(getBaseContext(), R.string.unameFormat, Toast.LENGTH_LONG).show();
                    pd.cancel();
                    return;
                }
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches())
                {
                    Toast.makeText(getBaseContext(), R.string.invalidMail, Toast.LENGTH_LONG).show();
                    pd.cancel();
                    return;
                }
                else if (!psswd.matches("[A-Za-z0-9]+"))
                {
                    Toast.makeText(getBaseContext(), R.string.passFormat, Toast.LENGTH_LONG).show();
                    pd.cancel();
                    return;
                }
                else if (psswd.length() < 8)
                {
                    Toast.makeText(getBaseContext(), R.string.passLength, Toast.LENGTH_LONG).show();
                    pd.cancel();
                    return;
                }
                final DatabaseReference userref = FirebaseDatabase.getInstance().getReference().child("userlist").child(uname);
                userref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.e("register","resume");
                        if (dataSnapshot.exists())
                        {
                            Toast.makeText(getBaseContext(), R.string.unameTaken, Toast.LENGTH_LONG).show();
                            pd.cancel();
                        }
                        else
                        {
                            createAccount(uname, mail, psswd, userref);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

    }
    public void createAccount(String name, String email, String pass, DatabaseReference ulist)
    {
        final DatabaseReference userlist = ulist;
        final String mail = email;
        final String psswd = pass;
        final String uname = name;
        Log.e("register","startcr");
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, psswd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.e("register","srescr");
                final DatabaseReference userref = FirebaseDatabase.getInstance().getReference().child("users").child(uname);
                userref.child("password").setValue(psswd).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("register","scscr");
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(uname).build();
                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                userlist.setValue(1);
                                userref.child("verified").setValue(0);
                                userref.child("money").setValue(0);
                                userref.child("respect").setValue(100);
                                Intent verify_intent = new Intent(registerActivity.this, verifyActivity.class);
                                verify_intent.putExtra("username", uname);
                                startActivity(verify_intent);
                                pd.cancel();
                                finish();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("register","failcr");
                        Toast.makeText(registerActivity.this, R.string.createUserFailed, Toast.LENGTH_LONG).show();
                        pd.cancel();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(registerActivity.this, R.string.emailOccupied, Toast.LENGTH_LONG).show();
                pd.cancel();
            }
        });
    }

    public void signIn(View view) {

        Intent signinActivity = new Intent(registerActivity.this, com.jattilainen.memebroker.Login.signInActivity.class);
        startActivity(signinActivity);
        finish();
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
                registerActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
