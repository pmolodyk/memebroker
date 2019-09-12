package com.jattilainen.memebroker.UploadedMemes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jattilainen.memebroker.R;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class UploadedMemesAdapter extends RecyclerView.Adapter<UploadedMemesAdapter.UploadedMemesViewHolder>{
    private List<UploadedMemesItem> uploadedMemesItems;
    private Context context;
    private String[] originLanguages;
    private String[] translatedLanguages;

    public interface OnButtonListener {
        void onAddButtonSelected(long currentPrice, String memeHash, String memeLanguage, double ratio);
    }

    public UploadedMemesAdapter(List<UploadedMemesItem> uploadedMemesItems, Context context) {
        this.uploadedMemesItems = uploadedMemesItems;
        this.context = context;
        originLanguages = context.getResources().getStringArray(R.array.original_languages);
        translatedLanguages = context.getResources().getStringArray(R.array.single_form_languages);
    }
    @NonNull
    @Override
    public UploadedMemesAdapter.UploadedMemesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.uploaded_memes_item, parent, false);
        return new UploadedMemesAdapter.UploadedMemesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final @NonNull UploadedMemesAdapter.UploadedMemesViewHolder holder, int position) {
        Log.e("uploadedmemes", "onBindViewHolder: ");
        final UploadedMemesItem uploadedMemesItem = uploadedMemesItems.get(holder.getAdapterPosition());
        StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + uploadedMemesItem.getHash());
        ViewTreeObserver vto = holder.itemImageView.getViewTreeObserver();
        final AtomicBoolean fitted = new AtomicBoolean(false);
        String help = "";
        final AtomicLong currentPrice = new AtomicLong(-1);
        for (int i = 0; i < originLanguages.length; i++) {
            if (originLanguages[i].equals(uploadedMemesItem.getLanguage())) {
                help = translatedLanguages[i];
            }
        }
        final String translatedLanguage = help;
        if (uploadedMemesItem.getUrl() != null) {
            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.Grey).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            Glide.with(context).load(uploadedMemesItem.getUrl()).apply(requestOptions).into(holder.itemImageView);
        }
        DatabaseReference memeRef = FirebaseDatabase.getInstance().getReference().child("images").child(uploadedMemesItem.getLanguage()).child("images").child(uploadedMemesItem.getHash());
        memeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    holder.priceTextView.setText(R.string.memeDeleted);holder.nameTextView.setText("");return;
                }
                Long price = dataSnapshot.child("price").getValue(Long.class);
                currentPrice.set(price);
                Integer id = dataSnapshot.child("id").getValue(Integer.class);
                if (price == null || id == null) {
                    holder.priceTextView.setText(R.string.memeDeleted);holder.nameTextView.setText("");currentPrice.set(-1);return;
                }
                holder.priceTextView.setText(String.valueOf(price));
                holder.nameTextView.setText(context.getString(R.string.meme_name_without_author, translatedLanguage, String.valueOf(id)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent buySellActivity = new Intent(context, com.jattilainen.memebroker.buySellActivity.class);
                buySellActivity.putExtra("memeHash", uploadedMemesItems.get(holder.getAdapterPosition()).getHash());
                buySellActivity.putExtra("memeLanguage", uploadedMemesItems.get(holder.getAdapterPosition()).getLanguage());
                buySellActivity.putExtra("url", uploadedMemesItems.get(holder.getAdapterPosition()).getUrl());
                context.startActivity(buySellActivity);
            }
        });
        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (currentPrice.get() != -1) {
                    UploadedMemesAdapter.OnButtonListener listener = (UploadedMemesAdapter.OnButtonListener) context;
                    listener.onAddButtonSelected(currentPrice.get(), uploadedMemesItem.getHash(), uploadedMemesItem.getLanguage(), uploadedMemesItem.getRatio());
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return uploadedMemesItems.size();
    }

    public class UploadedMemesViewHolder extends RecyclerView.ViewHolder {
        public TextView priceTextView;
        public ImageView itemImageView;
        public LinearLayout linearLayout;
        public TextView nameTextView;
        public UploadedMemesViewHolder(View itemView) {
            super(itemView);
            priceTextView = itemView.findViewById(R.id.UploadedMemesItemPriceTextView);
            itemImageView = itemView.findViewById(R.id.UploadedMemesItemImageView);
            linearLayout = itemView.findViewById(R.id.UploadedMemesItemLinearLayout);
            nameTextView = itemView.findViewById(R.id.UploadedMemesItemNameTextView);
        }
    }
}
