package com.jattilainen.memebroker.ShortList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jattilainen.memebroker.R;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ShortlistAdapterUI extends FirebaseRecyclerAdapter<ShortlistItem, ShortlistAdapterUI.ShortlistViewHolderUI> {
    private Context context;
    private String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    private DatabaseReference shortlistRef = FirebaseDatabase.getInstance().getReference().child("users").child(name).child("shortlist").child("images");
    private DatabaseReference memesRef = FirebaseDatabase.getInstance().getReference().child("images");
    public ShortlistAdapterUI(Class<ShortlistItem> modelClass, int modelLayout, Class<ShortlistViewHolderUI> viewHolderClass, DatabaseReference ref, Context context) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.context = context;
    }

    @Override
    protected void populateViewHolder(final ShortlistViewHolderUI holder, final ShortlistItem shortlistItem, int position) {
        final AtomicLong currentPrice = new AtomicLong(0);
        holder.oldPriceTextView.setText(context.getString(R.string.oldPrice, String.valueOf(shortlistItem.getStartPrice())));
        holder.newPriceTextView.setText(context.getString(R.string.newPrice, String.valueOf(currentPrice.get())));
        StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + shortlistItem.getHash());
        if (shortlistItem.getUrl() != null) {
            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.Grey).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            Glide.with(context).load(shortlistItem.getUrl()).apply(requestOptions).into(holder.itemImageView);
        }
        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shortlistItem.setStartPrice(currentPrice.get());
                holder.linearLayout.setBackgroundColor(Color.rgb(255, 255, 255));
                holder.oldPriceTextView.setText(context.getString(R.string.oldPrice, String.valueOf(shortlistItem.getStartPrice())));
                holder.newPriceTextView.setText(context.getString(R.string.newPrice, String.valueOf(currentPrice.get())));
                shortlistRef.child(shortlistItem.getHash()).child("startPrice").setValue(currentPrice.get());
                return true;
            }
        });
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buySellActivity = new Intent(context, com.jattilainen.memebroker.buySellActivity.class);
                buySellActivity.putExtra("memeHash", shortlistItem.getHash());
                buySellActivity.putExtra("memeLanguage", shortlistItem.getLanguage());
                buySellActivity.putExtra("url", shortlistItem.getUrl());

                context.startActivity(buySellActivity);
            }
        });
        memesRef.child(shortlistItem.getLanguage()).child("images").child(shortlistItem.getHash()).child("price").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("updateShortPricAccepted", shortlistItem.getHash() + " " + String.valueOf(dataSnapshot.getValue(long.class)) + String.valueOf(holder.getAdapterPosition()));
                if (holder.getAdapterPosition() == -1 || !(getItem(holder.getAdapterPosition()).getHash() == shortlistItem.getHash())) {
                    Log.e("shortlistperemoga", "onDataChange: ");
                    memesRef.child(shortlistItem.getLanguage()).child("images").child(shortlistItem.getHash()).child("price").removeEventListener(this);
                    return;
                }
                if (!dataSnapshot.exists()) {
                    holder.oldPriceTextView.setText(R.string.memeDeleted);
                    holder.newPriceTextView.setText("");
                    return;
                }
                Long currPrice = dataSnapshot.getValue(long.class);
                if (currPrice == null) {
                    holder.oldPriceTextView.setText(R.string.memeDeleted);
                    holder.newPriceTextView.setText("");
                    return;
                }
                currentPrice.set(currPrice);
                long dPrice = currentPrice.get() - shortlistItem.getStartPrice();
                if (dPrice > 0) {
                    holder.linearLayout.setBackgroundColor(Color.rgb(255 - (int)Math.min(dPrice, 255), 255, 255 - (int)Math.min(dPrice, 255)));//to be changed
                } else if (dPrice < 0) {
                    holder.linearLayout.setBackgroundColor(Color.rgb(255, 255 - (int)Math.min(-dPrice, 255), 255 - (int)Math.min(-dPrice, 255)));//to be changed
                } else {
                    holder.linearLayout.setBackgroundColor(Color.rgb(255, 255, 255));
                }
                holder.oldPriceTextView.setText(context.getString(R.string.oldPrice, String.valueOf(shortlistItem.getStartPrice())));
                holder.newPriceTextView.setText(context.getString(R.string.newPrice, String.valueOf(currentPrice.get())));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class ShortlistViewHolderUI extends RecyclerView.ViewHolder {
        public TextView oldPriceTextView;
        public TextView newPriceTextView;
        public ImageView itemImageView;
        public LinearLayout linearLayout;
        public ShortlistViewHolderUI(View itemView) {
            super(itemView);

            oldPriceTextView = itemView.findViewById(R.id.shortlistItemOldPrice);
            newPriceTextView = itemView.findViewById(R.id.shortlistItemNewPrice);
            itemImageView = itemView.findViewById(R.id.shortlistItemImageView);
            linearLayout = itemView.findViewById(R.id.shortlistItemLinearLayout);
        }
    }
    public void deleteItem(int position) {
        shortlistRef.child(getItem(position).getHash()).removeValue();
    }
}
