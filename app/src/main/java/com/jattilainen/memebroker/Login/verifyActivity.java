package com.jattilainen.memebroker.Login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.ui.ProgressDialogHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jattilainen.memebroker.Constants;
import com.jattilainen.memebroker.R;

import static com.jattilainen.memebroker.Constants.START_MONEY;

public class verifyActivity extends AppCompatActivity {
    Button resend;
    Button reload;
    Button relogin;
    ProgressDialog reloginPD;
    ProgressDialog reloadPD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
        setContentView(R.layout.activity_verify);
        resend = findViewById(R.id.resend);
        reload = findViewById(R.id.update);
        relogin = findViewById(R.id.logoutAndDelete);
        final String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(verifyActivity.this, R.string.email_sent_short, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(verifyActivity.this, R.string.smthWentWrong, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        reload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reloadPD = ProgressDialog.show(verifyActivity.this, "", getString(R.string.signUpProgress), true);
                if (FirebaseAuth.getInstance().getCurrentUser() != null)
                {
                    Log.e("leaving", FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
                }
                FirebaseAuth.getInstance().getCurrentUser().reload();
                if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                {
                    Log.e("leaving", "onClick: finish");
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(username);
                    userRef.child("mail").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    userRef.child("verified").setValue(1);
                    userRef.child("money").setValue(START_MONEY);
                    userRef.child("respect").setValue(Constants.START_RESPECT);
                    Intent mainActivity = new Intent(verifyActivity.this, com.jattilainen.memebroker.MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                } else {
                    reloadPD.cancel();
                    Toast.makeText(verifyActivity.this, R.string.email_not_verified, Toast.LENGTH_LONG).show();
                }
            }
        });
        relogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloginPD = ProgressDialog.show(verifyActivity.this, "", getString(R.string.reloginProgress), true);
                final String mail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                final String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                final DatabaseReference userref = FirebaseDatabase.getInstance().getReference().child("users").child(name).child("password");
                userref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String password = (String)dataSnapshot.getValue();
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(mail, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                deleteAccount(name);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(verifyActivity.this, R.string.smthWentWrong, Toast.LENGTH_LONG).show();
                        reloginPD.cancel();
                    }
                });
            }
        });
    }
    private void deleteAccount(String name) {
        FirebaseDatabase.getInstance().getReference().child("users").child(name).removeValue();
        FirebaseDatabase.getInstance().getReference().child("userlist").child(name).removeValue();
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(verifyActivity.this, R.string.smthWentWrong, Toast.LENGTH_LONG).show();
            reloginPD.cancel();
            return;
        }
        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.e("deleteAccount", "OK");
                    Intent verifyActivity = new Intent(verifyActivity.this, com.jattilainen.memebroker.Login.loginOptionActivity.class);
                    startActivity(verifyActivity);
                    finish();
                } else {
                    reloginPD.cancel();
                    Toast.makeText(verifyActivity.this, R.string.smthWentWrong, Toast.LENGTH_LONG).show();
                }
            }
        });
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
                verifyActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
}
