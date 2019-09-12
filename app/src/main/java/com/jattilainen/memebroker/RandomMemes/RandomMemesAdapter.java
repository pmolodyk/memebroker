package com.jattilainen.memebroker.RandomMemes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.SparseIntArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
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
import com.jattilainen.memebroker.MainActivity;
import com.jattilainen.memebroker.PersonProfile.PersonProfileActivity;
import com.jattilainen.memebroker.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import uk.co.senab.photoview.PhotoViewAttacher;


public class RandomMemesAdapter extends RecyclerView.Adapter<RandomMemesAdapter.RandomMemesViewHolder> {
    private List<RandomMemesItem> randomMemesItems;
    private MainActivity context;
    private int longClickPosition = -1;
    private long clickedPrice;
    private Map<Integer, Integer> randomsize = new HashMap<>();
    Random rand = new Random();
    RandomMemesAdapter(List<RandomMemesItem> topMemesItems, MainActivity context) {
        this.randomMemesItems = topMemesItems;
        this.context = context;
        for (int i = 0; i < 20; i++) {
            randomsize.put(i,  (rand.nextInt(4) + 3) * 200);
            Log.e("randomsize", String.valueOf(randomsize.get(i)));
        }
    }
    public interface OnButtonListener {
        void onAddButtonSelected(long currentPrice, String memeHash, String memeLanguage, double ratio);
        void onReportButtonSelected(String memeHash, String memeLanguage);
    }
    @NonNull
    @Override
    public RandomMemesAdapter.RandomMemesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.random_memes_item, parent, false);
        return new RandomMemesAdapter.RandomMemesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final @NonNull RandomMemesAdapter.RandomMemesViewHolder holder, final int position) {
        Log.e("bvh", "onBindViewHolder: " + holder.getAdapterPosition());
        final RandomMemesItem randomMemesItem = randomMemesItems.get(holder.getAdapterPosition());
        final AtomicLong currentPrice = new AtomicLong(-1);
        if (randomMemesItem.getUrl() != null) {
            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.Grey).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            Glide.with(context).load(randomMemesItem.getUrl()).apply(requestOptions).into(holder.itemImageView);
        }
        DatabaseReference memeRef = FirebaseDatabase.getInstance().getReference().child("images").child(randomMemesItem.getLanguage()).child("images").child(randomMemesItem.getHash());
        memeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("deletedElem", "onDataChange: ");
                if (!dataSnapshot.exists()) {
                    holder.nameTextView.setText(R.string.memeDeleted);
                    holder.priceTextView.setText("");
                    return;
                }
                Long currPrice = dataSnapshot.child("price").getValue(Long.class);
                Integer id = dataSnapshot.child("id").getValue(Integer.class);
                if (currPrice == null || id == null) {
                    holder.nameTextView.setText(R.string.memeDeleted);
                    holder.priceTextView.setText("");
                    return;
                }
                currentPrice.set(currPrice);
                final String author = dataSnapshot.child("author").getValue(String.class);
                holder.priceTextView.setText(String.valueOf(currentPrice.get()));
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
                buySellActivity.putExtra("memeHash", randomMemesItems.get(holder.getAdapterPosition()).getHash());
                buySellActivity.putExtra("memeLanguage", randomMemesItems.get(holder.getAdapterPosition()).getLanguage());
                buySellActivity.putExtra("url", randomMemesItems.get(holder.getAdapterPosition()).getUrl());
                context.startActivity(buySellActivity);
            }
        });
        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clickedPrice = currentPrice.get();
                longClickPosition = holder.getAdapterPosition();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return randomMemesItems.size();
    }

    public class RandomMemesViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView priceTextView;
        public TextView nameTextView;
        public ImageView itemImageView;
        public LinearLayout linearLayout;
        public RandomMemesViewHolder(View itemView) {
            super(itemView);
            priceTextView = itemView.findViewById(R.id.RandomMemesItemPriceTextView);
            itemImageView = itemView.findViewById(R.id.RandomMemesItemImageView);
            nameTextView = itemView.findViewById(R.id.RandomMemesItemNameTextView);
            linearLayout = itemView.findViewById(R.id.RandomMemesItemLinearLayout);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            MenuItem Edit = contextMenu.add(Menu.NONE, 1, 1, R.string.add_to_shortlist);
            Edit.setOnMenuItemClickListener(onMenuListener);
            MenuItem Report = contextMenu.add(Menu.NONE, 2, 2, R.string.report_abuse);
            Report.setOnMenuItemClickListener(onMenuListener);
        }
        private final MenuItem.OnMenuItemClickListener onMenuListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                RandomMemesItem randomMemesItem = randomMemesItems.get(longClickPosition);
                RandomMemesAdapter.OnButtonListener listener = (RandomMemesAdapter.OnButtonListener) context;
                switch (item.getItemId()) {
                    case 1:
                        if (longClickPosition == -1 || clickedPrice == -1) {
                            break;
                        }
                        listener.onAddButtonSelected(clickedPrice, randomMemesItem.getHash(), randomMemesItem.getLanguage(), randomMemesItem.getRatio());
                        longClickPosition = -1;
                        clickedPrice = -1;
                        break;
                    case 2:
                        if (longClickPosition == -1 || clickedPrice == -1) {
                            break;
                        }
                        listener.onReportButtonSelected(randomMemesItem.getHash(), randomMemesItem.getLanguage());
                        longClickPosition = -1;
                        clickedPrice = -1;
                        break;
                }
                return true;
            }
        };
    }

}
