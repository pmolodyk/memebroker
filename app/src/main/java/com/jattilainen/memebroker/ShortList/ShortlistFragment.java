package com.jattilainen.memebroker.ShortList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.jattilainen.memebroker.MainActivity;
import com.jattilainen.memebroker.R;
import com.jattilainen.memebroker.RandomMemes.RandomMemesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ShortlistFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<ShortlistItem> memes = new ArrayList<>();
    boolean created = false;
    MainActivity context;
    View view;
    ShortlistAdapterUI UIadapter;
    TextView emptyTextView;

    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    DatabaseReference shortlistRef = FirebaseDatabase.getInstance().getReference().child("users").child(name).child("shortlist").child("images");
    DatabaseReference memesRef = FirebaseDatabase.getInstance().getReference().child("images");


    public void setContext(MainActivity context) {
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!created) {
            Log.e("tor", "hi");
            view = inflater.inflate(R.layout.fragment_shortlist, container, false);
            emptyTextView = view.findViewById(R.id.emptyShortlistTextView);
            created = true;
            UIadapter = new ShortlistAdapterUI(ShortlistItem.class, R.layout.shortlist_item, ShortlistAdapterUI.ShortlistViewHolderUI.class, shortlistRef, context);
            shortlistRef.addValueEventListener(new ValueEventListener() {
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
            recyclerView = view.findViewById(R.id.shortlistRecyclerView);
            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(UIadapter);
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    UIadapter.deleteItem(viewHolder.getAdapterPosition());
                }
            }).attachToRecyclerView(recyclerView);
        }
        return view;

    }

    public void addMeme(final long startPrice, final String memeHash, final String memeLanguage, final double ratio) {
        FirebaseDatabase.getInstance().getReference().child("images").child("all").child("images").child(memeHash).child("shortlist").child(name).setValue(1);
        StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + memeHash);
        chosen.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri url) {
                shortlistRef.child(memeHash).setValue(new ShortlistItem(startPrice, memeHash, memeLanguage, ratio, url.toString()));

            }
        });
        Toast.makeText(context, R.string.addedToShortlist, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.shortlist_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shortlist_delete_button:
                shortlistRef.removeValue();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}