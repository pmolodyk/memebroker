package com.jattilainen.memebroker.Login;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.jattilainen.memebroker.R;

public class recoverPasswordActivity extends Activity {

    private EditText mailET;
    private Button ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);
        mailET = findViewById(R.id.recovery_email);
        ok = findViewById(R.id.recovery_button);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mailET.getText().toString();
                if (mail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches())
                {
                    Toast.makeText(getBaseContext(), R.string.invalidMail, Toast.LENGTH_LONG).show();
                    return;
                }
                FirebaseAuth.getInstance().sendPasswordResetEmail(mailET.getText().toString()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(), R.string.checkPass, Toast.LENGTH_LONG).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getBaseContext(), R.string.recoverySent, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        });
    }
}
