package com.jattilainen.memebroker.Assets;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.ULocale;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jattilainen.memebroker.MainActivity;
import com.jattilainen.memebroker.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AssetsFragment extends Fragment {
    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    DatabaseReference assetsRef = FirebaseDatabase.getInstance().getReference().child("users").child(name).child("images");
    View view;
    boolean created = false;
    private List<AssetsItem> memes = new ArrayList<>();
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<AssetsItem, AssetsViewHolderUI> UIadapter;
    Context context;
    TextView emptyTextView;
    AssetsAdapter adapter;
    public AssetsFragment() {
        // Required empty public constructor
    }

    public void setContext(MainActivity context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!created) {
            Log.e("tor", "hi");
            view = inflater.inflate(R.layout.fragment_assets, container, false);
            created = true;
            emptyTextView = view.findViewById(R.id.emptyAssetsTextView);
            adapter = new AssetsAdapter(memes, getActivity());
            created = true;
            recyclerView = view.findViewById(R.id.assetsRecyclerView);
            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            assetsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    AssetsItem help = (dataSnapshot.getValue(AssetsItem.class));
                    final String hash = help.getHash();
                    final double ratio = help.getRatio();
                    final String language = help.getLanguage();
                    final Long amount = help.getAmount();
                    final Long totalExpenses = help.getTotalExpenses();
                    StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + hash);
                    chosen.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri url) {
                            memes.add(new AssetsItem(hash, language, totalExpenses, amount, ratio, url.toString()));
                            adapter.notifyItemInserted(memes.size());
                        }
                    });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    for (int i = 0; i < memes.size(); i++) {
                        AssetsItem serverItem = dataSnapshot.getValue(AssetsItem.class);
                        String memeHash = serverItem.getHash();
                        AssetsItem assetsItem = memes.get(i);
                        if (assetsItem.getHash().equals(memeHash)) {
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
            recyclerView.setAdapter(adapter);

        }
        assetsRef.addValueEventListener(new ValueEventListener() {
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
        return view;
    }
    private void setCard(final AssetsViewHolderUI holder, Long possibleReceipts, Long totalExpense, Long dMoney) {
        holder.possibleReceiptsTextView.setText(context.getString(R.string.possibleReceipts, possibleReceipts));
        holder.totalExpensesTextView.setText(context.getString(R.string.totalExpense, totalExpense));
        holder.possibleIncomeTextView.setText(context.getString(R.string.possibleProfit, dMoney));
        long ProfitColor = (long)Math.sqrt(Math.abs(dMoney));
        if (dMoney > 0) {
            Log.e("element", String.valueOf((int)Math.min(ProfitColor, 255)));
            holder.linearLayout.setBackgroundColor(Color.rgb(255 - (int)Math.min(ProfitColor, 255), 255, 255 - (int)Math.min(ProfitColor, 255)));//to be changed
        } else if (dMoney < 0) {
            holder.linearLayout.setBackgroundColor(Color.rgb(255, 255 - (int)Math.min(ProfitColor, 255), 255 - (int)Math.min(ProfitColor, 255)));//to be changed
        } else {
            holder.linearLayout.setBackgroundColor(Color.rgb(255, 255, 255));
        }
    }
    private long costToSell(long startPrice, Long want) {
        if (want > startPrice) {
            want = startPrice;
        }
        return (2 * startPrice - (want - 1)) * want / 2;
    }
}
