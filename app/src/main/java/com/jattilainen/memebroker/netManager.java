package com.jattilainen.memebroker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class netManager {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    DatabaseReference dbref = db.getReference();
    StorageReference storeref = storage.getReference();
    Uri location;
    String hash;
    long amountOfMemes = 0;
    Bitmap image;
    public netManager()
    {

    }
    public Boolean signedIn()
    {
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }
    public Boolean emailVerified()
    {
        FirebaseAuth.getInstance().getCurrentUser().reload();
        return FirebaseAuth.getInstance().getCurrentUser().isEmailVerified();
    }


}
