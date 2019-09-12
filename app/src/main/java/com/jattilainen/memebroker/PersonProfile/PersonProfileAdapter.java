package com.jattilainen.memebroker.PersonProfile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jattilainen.memebroker.R;
import com.jattilainen.memebroker.RandomMemes.RandomMemesAdapter;
import com.jattilainen.memebroker.UploadedMemes.UploadedMemesItem;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<UploadedMemesItem> uploadedMemesItems;
    Context context;
    private int longClickPosition = -1;
    private long clickedPrice;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private String[] originLanguages;
    private String[] translatedLanguages;
    private String name;
    public interface OnButtonListener {
        void onAddButtonSelected(long currentPrice, String memeHash, String memeLanguage, double ratio);
    }

    public PersonProfileAdapter(List<UploadedMemesItem> uploadedMemesItems, Context context, String name) {
        this.uploadedMemesItems = uploadedMemesItems;
        this.name = name;
        this.context = context;
        originLanguages = context.getResources().getStringArray(R.array.original_languages);
        translatedLanguages = context.getResources().getStringArray(R.array.single_form_languages);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_profile_header, parent, false);
            return new PersonProfileAdapter.HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.uploaded_memes_item, parent, false);
            return new PersonProfileAdapter.NormalViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder_all, int position) {
        if (holder_all instanceof NormalViewHolder) {
           final NormalViewHolder holder = (NormalViewHolder) holder_all;
            final UploadedMemesItem uploadedMemesItem = uploadedMemesItems.get(holder.getAdapterPosition() - 1);
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
                    buySellActivity.putExtra("memeHash", uploadedMemesItem.getHash());
                    buySellActivity.putExtra("memeLanguage", uploadedMemesItem.getLanguage());
                    buySellActivity.putExtra("url", uploadedMemesItem.getUrl());

                    context.startActivity(buySellActivity);
                }
            });
            holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (currentPrice.get() != -1) {
                        PersonProfileAdapter.OnButtonListener listener = (PersonProfileAdapter.OnButtonListener) context;
                        listener.onAddButtonSelected(currentPrice.get(), uploadedMemesItem.getHash(), uploadedMemesItem.getLanguage(), uploadedMemesItem.getRatio());
                    }
                    return true;
                }
            });
        } else {
            final HeaderViewHolder holder = (HeaderViewHolder) holder_all;
            holder.nameTextView.setText(name);
            final TextView moneyTextView = holder.moneyTextView;
            final TextView respectTextView = holder.respectTextView;
            ImageView circleImageView = holder.circleImageView;
            circleImageView.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_launcher_round));
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(name);
            userRef.child("money").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Long money = dataSnapshot.getValue(Long.class);
                    moneyTextView.setText(context.getString(R.string.Money, money));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            userRef.child("respect").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer respect = dataSnapshot.getValue(Integer.class);
                    respectTextView.setText(context.getString(R.string.Respect, respect));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return uploadedMemesItems.size() + 1;
    }

    public class NormalViewHolder extends RecyclerView.ViewHolder {
        public TextView priceTextView;
        public ImageView itemImageView;
        public LinearLayout linearLayout;
        public TextView nameTextView;
        public NormalViewHolder(View itemView) {
            super(itemView);
            priceTextView = itemView.findViewById(R.id.UploadedMemesItemPriceTextView);
            itemImageView = itemView.findViewById(R.id.UploadedMemesItemImageView);
            linearLayout = itemView.findViewById(R.id.UploadedMemesItemLinearLayout);
            nameTextView = itemView.findViewById(R.id.UploadedMemesItemNameTextView);
        }
    }
    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView moneyTextView;
        public TextView respectTextView;
        public ImageView circleImageView;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.personProfileHeaderNameTextView);
            moneyTextView = itemView.findViewById(R.id.personProfileHeaderMoneyTextView);
            respectTextView = itemView.findViewById(R.id.personProfileHeaderRespectTextView);
            circleImageView = itemView.findViewById(R.id.personProfileImageView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }
}
