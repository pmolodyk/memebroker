package com.jattilainen.memebroker.PersonProfile;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jattilainen.memebroker.Constants;
import com.jattilainen.memebroker.R;
import com.jattilainen.memebroker.RandomMemes.RandomMemesItem;
import com.jattilainen.memebroker.ShortList.ShortlistItem;
import com.jattilainen.memebroker.UploadedMemes.UploadedMemesAdapter;
import com.jattilainen.memebroker.UploadedMemes.UploadedMemesItem;

import java.util.ArrayList;
import java.util.List;

public class PersonProfileActivity extends AppCompatActivity implements PersonProfileAdapter.OnButtonListener, SwipeRefreshLayout.OnRefreshListener {
    RecyclerView recyclerView;
    private List<UploadedMemesItem> memes = new ArrayList<>();
    PersonProfileAdapter adapter;
    String name;
    String ownName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    int loadingToasts = 0;
    TextView noUploadsText;
    DatabaseReference shortlistRef = FirebaseDatabase.getInstance().getReference().child("users").child(ownName).child("shortlist").child("images");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        name = getIntent().getStringExtra("name");

        setContentView(R.layout.activity_person_profile);
        Toolbar toolbar = findViewById(R.id.person_profile_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(name);
        noUploadsText = findViewById(R.id.emptyPersonProfile);
        if (!getIntent().hasExtra("name")) {
            Toast.makeText(PersonProfileActivity.this, R.string.smthWentWrong, Toast.LENGTH_LONG).show();
            finish();
        }
        recyclerView = findViewById(R.id.personProfileRecyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(PersonProfileActivity.this));
        loadMemes();
    }
    void loadMemes() {
        memes = new ArrayList<>();
        adapter = new PersonProfileAdapter(memes, PersonProfileActivity.this, name);
        recyclerView.setAdapter(adapter);
        DatabaseReference personRef = FirebaseDatabase.getInstance().getReference().child("users").child(name);
        personRef.child("uploaded").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String memeHash = dataSnapshot.getKey();
                if(memeHash == null) {
                    return;
                }
                FirebaseDatabase.getInstance().getReference().child("images").child("all").child("images").child(memeHash).child("language").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        }
                        String lang = dataSnapshot.getValue(String.class);
                        if (lang == null) {
                            return;
                        }
                        loadMemeToList(memeHash, lang);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String memeHash = dataSnapshot.getKey();
                for (int i = 0; i < memes.size(); i++) {
                    UploadedMemesItem uploadedMemesItem = memes.get(i);
                    if (uploadedMemesItem.getHash().equals(memeHash)) {
                        memes.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    void loadMemeToList(final String memeHash, final String memeLanguage) {
        noUploadsText.setVisibility(View.INVISIBLE);
        Log.e("lmtl", memeHash + " " + memeLanguage);
        DatabaseReference memeRef = FirebaseDatabase.getInstance().getReference().child("images").child(memeLanguage).child("images").child(memeHash);
        memeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            double ratio;
            @Override
            public void onDataChange(@NonNull DataSnapshot memeSnapshot) {
                int height = Integer.parseInt(memeSnapshot.child("height").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                int width = Integer.parseInt(memeSnapshot.child("width").getValue(String.class));//ПЕТЯ СДЕЛАЕТ ИНТ НА СЕРВЕРЕ
                ratio = (double)height/width;
                StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + memeHash);
                chosen.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri url) {
                        memes.add(new UploadedMemesItem(memeHash, memeLanguage, ratio, url.toString()));
                        adapter.notifyItemInserted(memes.size());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAddButtonSelected(final long startPrice, final String memeHash, final String memeLanguage, final double ratio) {
        loadingToasts++;
        FirebaseDatabase.getInstance().getReference().child("images").child("all").child("images").child(memeHash).child("shortlist").child(name).setValue(1);
        StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + memeHash);
        chosen.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri url) {
                shortlistRef.child(memeHash).setValue(new ShortlistItem(startPrice, memeHash, memeLanguage, ratio, url.toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingToasts--;
                        Toast.makeText(PersonProfileActivity.this, R.string.addedToShortlist, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (loadingToasts > 0) {
            Toast.makeText(PersonProfileActivity.this, R.string.ConnectionError, Toast.LENGTH_LONG).show();
            return false;
        }
        onBackPressed();
        return true;
    }

    @Override
    public void onRefresh() {
        loadMemes();
    }

    @Override
    public void onBackPressed() {
        if (loadingToasts > 0) {
            Toast.makeText(PersonProfileActivity.this, R.string.ConnectionError, Toast.LENGTH_LONG).show();
            return;
        }
        super.onBackPressed();
    }
}
