package com.jattilainen.memebroker.UploadedMemes;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.jattilainen.memebroker.R;

import java.util.ArrayList;
import java.util.List;

public class UploadedMemesFragment extends Fragment {
    View view;
    UploadedMemesAdapter adapter;
    boolean created = false;
    private List<UploadedMemesItem> memes = new ArrayList<>();
    RecyclerView recyclerView;
    TextView emptyTextView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!created) {
            view = inflater.inflate(R.layout.fragment_uploaded_memes, container, false);
            emptyTextView = view.findViewById(R.id.emptyUploadedTextView);
            adapter = new UploadedMemesAdapter(memes, getActivity());
            created = true;
            recyclerView = view.findViewById(R.id.uploadedMemesRecyclerView);
            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(adapter);
            loadDataToList();
        }
        return view;
    }
    private void loadDataToList() {
        String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        DatabaseReference uploadedRef = FirebaseDatabase.getInstance().getReference().child("users").child(name).child("uploaded");
        uploadedRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String memeHash = dataSnapshot.getKey();
                Log.e("uploadeddata", memeHash);
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
        uploadedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    emptyTextView.setVisibility(View.INVISIBLE);
                } else {
                    emptyTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    void loadMemeToList(@NonNull final String memeHash, @NonNull final String memeLanguage) {
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
}
