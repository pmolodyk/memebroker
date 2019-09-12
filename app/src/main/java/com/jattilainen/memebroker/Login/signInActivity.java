package com.jattilainen.memebroker.Login;

//UNAUTHORISED DB ACCESS!! SWITCH TO MAIL LOG IN FIXED

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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jattilainen.memebroker.MainActivity;
import com.jattilainen.memebroker.R;

public class signInActivity extends AppCompatActivity {
    Button okButton;
    EditText emailET;
    EditText passwordET;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        okButton = findViewById(R.id.signin_button);
        emailET = findViewById(R.id.signin_email);
        passwordET = findViewById(R.id.signin_password);
        Log.e("signin", "started");
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd = ProgressDialog.show(signInActivity.this, "", getString(R.string.signInProgress), true);
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    Toast.makeText(getBaseContext(), R.string.invalidMail, Toast.LENGTH_LONG).show();
                    pd.cancel();
                    return;
                }
                else if (!password.matches("[A-Za-z0-9]+"))
                {
                    Toast.makeText(getBaseContext(), R.string.passFormat, Toast.LENGTH_LONG).show();
                    pd.cancel();
                    return;
                }
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (authResult.getUser().isEmailVerified()) {
                            Intent signedinActivity = new Intent(signInActivity.this, MainActivity.class);
                            startActivity(signedinActivity);
                            pd.cancel();
                            finish();
                        } else {
                            Intent verifyActivity = new Intent(signInActivity.this, com.jattilainen.memebroker.Login.verifyActivity.class);
                            startActivity(verifyActivity);
                            pd.cancel();
                            finish();
                        }
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(), R.string.invalidUser, Toast.LENGTH_LONG).show();
                        pd.cancel();
                    }
                });
            }
        });

    }

    public void passRecover(View view)
    {
        Intent rec_intent = new Intent(signInActivity.this, recoverPasswordActivity.class);
        startActivity(rec_intent);
    }

    public void signUp(View view) {
        Intent registerActivity = new Intent(signInActivity.this, com.jattilainen.memebroker.Login.registerActivity.class);
        startActivity(registerActivity);
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
                signInActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
