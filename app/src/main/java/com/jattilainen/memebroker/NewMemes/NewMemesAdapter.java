package com.jattilainen.memebroker.NewMemes;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jattilainen.memebroker.R;

import java.util.List;


public class NewMemesAdapter extends RecyclerView.Adapter<NewMemesAdapter.NewMemesViewHolder> {
    private List<NewMemesItem> newMemesItems;
    private Context context;

    public NewMemesAdapter(List<NewMemesItem> newMemesItems, Context context) {
        this.newMemesItems = newMemesItems;
        this.context = context;
    }
    public interface OnButtonListener {
        void onAddButtonSelected(long currentPrice, String memeHash, String memeLanguage, double ratio);
    }
    @NonNull
    @Override
    public NewMemesAdapter.NewMemesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_memes_item, parent, false);
        return new NewMemesAdapter.NewMemesViewHolder(v);
    }
    @Override
    public void onBindViewHolder(final @NonNull NewMemesAdapter.NewMemesViewHolder holder, int position) {
        Log.e("topmemes", "onBindViewHolder: ");
        final NewMemesItem newMemesItem = newMemesItems.get(holder.getAdapterPosition());
        if (newMemesItem.getUrl() != null) {
            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.Grey).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            Glide.with(context).load(newMemesItem.getUrl()).apply(requestOptions).into(holder.itemImageView);
        }
        DatabaseReference memeRef = FirebaseDatabase.getInstance().getReference().child("images").child(newMemesItem.getLanguage()).child("images").child(newMemesItem.getHash());
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
                buySellActivity.putExtra("memeHash", newMemesItems.get(holder.getAdapterPosition()).getHash());
                buySellActivity.putExtra("memeLanguage", newMemesItems.get(holder.getAdapterPosition()).getLanguage());
                buySellActivity.putExtra("url", newMemesItems.get(holder.getAdapterPosition()).getUrl());
                context.startActivity(buySellActivity);
            }
        });
        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                FirebaseDatabase.getInstance().getReference().child("images").child(newMemesItem.getLanguage()).child("images").child(newMemesItem.getHash()).child("price").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Integer currentPrice = dataSnapshot.getValue(Integer.class);
                        if (currentPrice != null) {
                            NewMemesAdapter.OnButtonListener listener = (NewMemesAdapter.OnButtonListener) context;
                            listener.onAddButtonSelected(currentPrice, newMemesItem.getHash(), newMemesItem.getLanguage(), newMemesItem.getRatio());
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
        return newMemesItems.size();
    }

    public class NewMemesViewHolder extends RecyclerView.ViewHolder {
        public TextView priceTextView;
        public TextView nameTextView;
        public ImageView itemImageView;
        public LinearLayout linearLayout;
        public NewMemesViewHolder(View itemView) {
            super(itemView);
            priceTextView = itemView.findViewById(R.id.NewMemesItemPriceTextView);
            itemImageView = itemView.findViewById(R.id.NewMemesItemImageView);
            nameTextView = itemView.findViewById(R.id.NewMemesItemNameTextView);
            linearLayout = itemView.findViewById(R.id.NewMemesItemLinearLayout);
        }
    }
}
