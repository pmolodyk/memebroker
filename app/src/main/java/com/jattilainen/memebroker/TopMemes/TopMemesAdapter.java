package com.jattilainen.memebroker.TopMemes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

public class TopMemesAdapter extends RecyclerView.Adapter<TopMemesAdapter.TopMemesViewHolder> {
    private List<TopMemesItem> topMemesItems;
    private Context context;

    public TopMemesAdapter(List<TopMemesItem> topMemesItems, Context context) {
        this.topMemesItems = topMemesItems;
        this.context = context;
    }
    public interface OnButtonListener {
        void onAddButtonSelected(long currentPrice, String memeHash, String memeLanguage, double ratio);
    }
    @NonNull
    @Override
    public TopMemesAdapter.TopMemesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.top_memes_item, parent, false);
        return new TopMemesAdapter.TopMemesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final @NonNull TopMemesAdapter.TopMemesViewHolder holder, int position) {
        Log.e("topmemes", "onBindViewHolder: ");
        final TopMemesItem topMemesItem = topMemesItems.get(holder.getAdapterPosition());
        StorageReference chosen = FirebaseStorage.getInstance().getReference().child("images/" + topMemesItem.getHash());
        if (topMemesItem.getUrl() != null) {
            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.Grey).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            Glide.with(context).load(topMemesItem.getUrl()).apply(requestOptions).into(holder.itemImageView);
        }
        DatabaseReference memeRef = FirebaseDatabase.getInstance().getReference().child("images").child(topMemesItem.getLanguage()).child("images").child(topMemesItem.getHash());
        memeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    holder.nameTextView.setText(R.string.memeDeleted);holder.priceTextView.setText("");return;
                }
                Long currPrice = dataSnapshot.child("price").getValue(Long.class);
                Integer id = dataSnapshot.child("id").getValue(Integer.class);
                if (currPrice == null || id == null) {
                    holder.nameTextView.setText(R.string.memeDeleted);holder.priceTextView.setText("");return;
                }
                long price = currPrice;
                final String author = dataSnapshot.child("author").getValue(String.class);
                holder.priceTextView.setText(String.valueOf(price));
                SpannableString ss = new SpannableString(context.getString(R.string.meme_name_with_author, String.valueOf(id), author));
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        Intent personProfileActivity = new Intent(context, com.jattilainen.memebroker.PersonProfile.PersonProfileActivity.class);
                        personProfileActivity.putExtra("name", author);
                        context.startActivity(personProfileActivity);
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                };
                ss.setSpan(clickableSpan, ss.length() - author.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                TextView textView = holder.nameTextView;
                textView.setText(ss);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
               // textView.setHighlightColor(Color.TRANSPARENT);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent buySellActivity = new Intent(context, com.jattilainen.memebroker.buySellActivity.class);
                buySellActivity.putExtra("memeHash", topMemesItems.get(holder.getAdapterPosition()).getHash());
                buySellActivity.putExtra("memeLanguage", topMemesItems.get(holder.getAdapterPosition()).getLanguage());
                buySellActivity.putExtra("url", topMemesItems.get(holder.getAdapterPosition()).getUrl());
                context.startActivity(buySellActivity);
            }
        });
        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("images").child(topMemesItem.getLanguage()).child("images").child(topMemesItem.getHash()).child("price").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Integer currentPrice = dataSnapshot.getValue(Integer.class);
                        if (currentPrice != null) {
                            TopMemesAdapter.OnButtonListener listener = (TopMemesAdapter.OnButtonListener) context;
                            listener.onAddButtonSelected(currentPrice, topMemesItem.getHash(), topMemesItem.getLanguage(), topMemesItem.getRatio());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return topMemesItems.size();
    }

    public class TopMemesViewHolder extends RecyclerView.ViewHolder {
        public TextView priceTextView;
        public TextView nameTextView;
        public ImageView itemImageView;
        public LinearLayout linearLayout;
        public TopMemesViewHolder(View itemView) {
            super(itemView);
            priceTextView = itemView.findViewById(R.id.TopMemesItemPriceTextView);
            itemImageView = itemView.findViewById(R.id.TopMemesItemImageView);
            nameTextView = itemView.findViewById(R.id.TopMemesItemNameTextView);
            linearLayout = itemView.findViewById(R.id.TopMemesItemLinearLayout);
        }
    }
}
