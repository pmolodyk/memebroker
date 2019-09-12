package com.jattilainen.memebroker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadActivity extends AppCompatActivity {
    private final int UPLOAD_CODE = 1;
    ImageView uploadedImage;
    Bitmap readyImage;
    InputStream is;
    TextView warning;
    Uri finalPath;
    Boolean chosen = false;
    Bitmap image;
    MaterialBetterSpinner langChooseSpinner;
    int chosenLangId = -1;
    String[] LANGUAGE_LIST;
    String[] ORIGINAL_LANGUAGE_LIST;
    ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        setSupportActionBar((Toolbar)findViewById(R.id.upload_activity_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        uploadedImage = findViewById(R.id.uploadedImage);
        warning = findViewById(R.id.warningScaleImage);
        langChooseSpinner = findViewById(R.id.upload_meme_lang_choose_spinner);
        LANGUAGE_LIST = getResources().getStringArray(R.array.languages_array);
        LANGUAGE_LIST[0] = getResources().getString(R.string.noText);
        ORIGINAL_LANGUAGE_LIST = getResources().getStringArray(R.array.original_languages);
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, LANGUAGE_LIST);

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        int uploadingLang = prefs.getInt(Constants.UPLOAD_MEME_LANG_PREFS, -1);
        chosenLangId = uploadingLang;
        if (chosenLangId != -1) {
            langChooseSpinner.setText(LANGUAGE_LIST[chosenLangId]);
        } else {
            langChooseSpinner.setHint(R.string.choose_language);
        }
        langChooseSpinner.setAdapter(arrayAdapter);
        langChooseSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                chosenLangId = i;
            }
        });
    }

    private String hashBitmap(Bitmap bmp){
        long prime = 31;
        long hash = 0; //or a higher prime at your choice
        for(int x = 0; x < bmp.getWidth(); x++){
            for (int y = 0; y < bmp.getHeight(); y++){
                hash += bmp.getPixel(x,y);
                hash *= prime;
            }
        }
        return String.valueOf(hash);
    }

    public void uploadImage(View v)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, UPLOAD_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == UPLOAD_CODE)
            {
                finalPath = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), finalPath);
                    image = bitmap;
                    int targetWidth, targetHeight;
                    if (bitmap.getWidth() > bitmap.getHeight())
                    {
                        targetWidth = Math.min(Constants.TARGET_WIDTH, bitmap.getWidth());
                        targetHeight = (int) (bitmap.getHeight() * targetWidth / (double) bitmap.getWidth());
                    }
                    else
                    {
                        targetHeight = Math.min(Constants.TARGET_HEIGHT, bitmap.getHeight());
                        targetWidth = (int) (bitmap.getWidth() * targetHeight / (double) bitmap.getHeight());
                    }
                    if (targetWidth > Constants.MAX_HORIZONTAL_RATIO * targetHeight || targetHeight > Constants.MAX_VERTICAL_RATIO * targetWidth) {
                        Toast.makeText(UploadActivity.this, R.string.wrong_meme_format, Toast.LENGTH_LONG).show();
                        return;
                    }
                    readyImage = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                    uploadedImage.setImageBitmap(readyImage);
                    //Glide.with(UploadActivity.this).load(finalPath).diskCacheStrategy(DiskCacheStrategy.ALL).into(uploadedImage);
                    chosen = true;
                    warning.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void uploadImage(Bitmap bmp)
    {
        final String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        //Log.e("upload", location.getPath());
        final String hash = hashBitmap(readyImage);
        if (chosenLangId == -1) {
            Toast.makeText(UploadActivity.this, R.string.upload_language_not_chosen, Toast.LENGTH_LONG).show();
        }
        final String loadLanguage = ORIGINAL_LANGUAGE_LIST[chosenLangId];
        FirebaseDatabase.getInstance().getReference().child("images").child("all").child("images").child(hash).child("language").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(getBaseContext(), R.string.meme_exists, Toast.LENGTH_LONG).show();
                }
                else {
                    final DatabaseReference quotaRef = FirebaseDatabase.getInstance().getReference().child("users").child(username).child("quota");
                    quotaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Integer used = 0;
                            if (dataSnapshot.exists()) {
                                used = dataSnapshot.getValue(Integer.class);
                            }
                            if (used == null || used >= Constants.WEEK_LOADS_QUOTA) {
                                Toast.makeText(getBaseContext(), R.string.quota_exceeded, Toast.LENGTH_LONG).show();
                                return;
                            }
                            quotaRef.setValue(used + 1);

                            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("images/" + hash);
                            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                            readyImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                            StorageMetadata author = new StorageMetadata.Builder().setCustomMetadata("author", username).setCustomMetadata("width", String.valueOf(readyImage.getWidth())).setCustomMetadata("height", String.valueOf(readyImage.getHeight())).setCustomMetadata("language", loadLanguage).build();
                            byte[] imagedata = outStream.toByteArray();
                            UploadTask ut = imageRef.putBytes(imagedata, author);
                            ut.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(getBaseContext(), R.string.upload_started, Toast.LENGTH_LONG).show();
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Log.d("firebase-uploader", "failed");
                                }
                            });
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


    }

    public void finishUploading() {
        if (chosen) {
            uploadImage(image);
            finish();
        } else {
            Toast.makeText(UploadActivity.this, R.string.image_not_chosen_error, Toast.LENGTH_LONG).show();
        }
    }

    public void rotateBitmap(View v)
    {
        if (!chosen) {
            Toast.makeText(UploadActivity.this, R.string.image_not_chosen_error, Toast.LENGTH_SHORT).show();
            return;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(readyImage, 0, 0, readyImage.getWidth(), readyImage.getHeight(), matrix, true);
        readyImage = rotatedBitmap;
        uploadedImage.setImageBitmap(readyImage);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.confirm_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e("finishupl", "onOptionsItemSelected: ");
        switch(item.getItemId())
        {
            case R.id.confirm_menu_confirm:
                if (chosenLangId == -1) {
                    Toast.makeText(UploadActivity.this, R.string.upload_language_not_chosen, Toast.LENGTH_LONG).show();
                } else {
                    SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                    editor.putInt(Constants.UPLOAD_MEME_LANG_PREFS, chosenLangId);
                    editor.apply();
                    finishUploading();
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
