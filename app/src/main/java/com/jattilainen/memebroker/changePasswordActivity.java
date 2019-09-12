package com.jattilainen.memebroker;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class changePasswordActivity extends AppCompatActivity {
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        final EditText oldPasswordField = findViewById(R.id.old_password);
        final EditText repPasswordField = findViewById(R.id.rep_password);
        final EditText newPasswordField = findViewById(R.id.new_password);
        Button ok = findViewById(R.id.changeButton);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
        }
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String email = user.getEmail();
        final String name = user.getDisplayName();
        FirebaseDatabase.getInstance().getReference().child("users").child(name).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String psw = dataSnapshot.getValue(String.class);
                if (psw == null) {
                    return;
                }
                if (!"already_changed".equals(psw)) {
                    oldPasswordField.setText(psw);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newPassword = newPasswordField.getText().toString();
                final String repPassword = repPasswordField.getText().toString();
                final String oldPassword = oldPasswordField.getText().toString();
                if ("".equals(newPassword) || "".equals(oldPassword)) {
                    return;
                }
                if (!repPassword.equals(newPassword)) {
                    Toast.makeText(getBaseContext(), R.string.passwordsDifferent, Toast.LENGTH_LONG).show();
                    return;
                }
                if (!newPassword.matches("[A-Za-z0-9]+"))
                {
                    Toast.makeText(getBaseContext(), R.string.passFormat, Toast.LENGTH_LONG).show();
                    return;
                }
                if (newPassword.length() < Constants.PASSWORD_MIN_LENGTH) {
                    Toast.makeText(getBaseContext(), getString(R.string.passLength, Constants.PASSWORD_MIN_LENGTH), Toast.LENGTH_LONG).show();
                    return;
                }
                AuthCredential credential = EmailAuthProvider.getCredential(email,oldPassword);
                Log.e("jesus", "tut");
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
                                        dbref.child("users").child(name).child("password").setValue("already_changed");
                                        Log.e("jesus", "SPASIBO");
                                        finish();
                                    }else {
                                        Log.e("jesus", "petux");
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(changePasswordActivity.this, R.string.oldPasswordIsWrong, Toast.LENGTH_LONG).show();
                            Log.e("jesus", "PETUX");
                        }
                    }
                });
            }
        });

    }
}
