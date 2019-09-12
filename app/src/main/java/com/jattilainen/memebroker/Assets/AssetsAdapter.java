package com.jattilainen.memebroker.Assets;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class AssetsAdapter  extends RecyclerView.Adapter<AssetsAdapter.AssetsViewHolder> {
    private List<AssetsItem> assetsItems;
    private Context context;
    private String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    private DatabaseReference assetsRef = FirebaseDatabase.getInstance().getReference().child("users").child(name).child("images");
    public AssetsAdapter(List<AssetsItem> assetsItems, Context context) {
        this.assetsItems = assetsItems;
        this.context = context;
    }

    public interface OnButtonListener {
        void onAddButtonSelected(long currentPrice, String memeHash, String memeLanguage, double ratio);
    }
    @NonNull
    @Override
    public AssetsAdapter.AssetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.assets_item, parent, false);
        return new AssetsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final AssetsAdapter.AssetsViewHolder holder, int position) {
        Log.e("bindviewholder", String.valueOf(holder.getAdapterPosition()) + String.valueOf(assetsItems.size()));
        final AssetsItem assetsItem = assetsItems.get(holder.getAdapterPosition());
        final AtomicLong currentPrice = new AtomicLong(-1);
        Log.e("qqqq", assetsItem.getHash());
        if (assetsItem.getUrl() != null) {
            RequestOptions requestOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.Grey).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            Glide.with(context).load(assetsItem.getUrl()).apply(requestOptions).into(holder.itemImageView);
        }
        assetsRef.child(assetsItem.getHash()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AssetsItem serverItem = dataSnapshot.getValue(AssetsItem.class);
                if(serverItem == null) {
                    Log.e("serverItem", String.valueOf(holder.getAdapterPosition()));
                } else {
                    Log.e("serverItem", String.valueOf(holder.getAdapterPosition()) + " " + String.valueOf(serverItem.getTotalExpenses()));
                }
                if (serverItem == null) {
                    return;
                }
                if (holder.getAdapterPosition() == -1) {
                    return;
                }
                for (int i = 0; i < assetsItems.size(); i++) {
                    Log.e("serverItems", String.valueOf(i) + " " + assetsItems.get(i).getTotalExpenses());
                }
                if (!assetsItems.get(holder.getAdapterPosition()).getHash().equals(assetsItem.getHash())) {
                    return;
                }
                assetsItem.setTotalExpenses(serverItem.getTotalExpenses());
                assetsItem.setAmount(serverItem.getAmount());
                if (currentPrice.get() != -1) {
                    Long possibleReceipts = costToSell(currentPrice.get(), assetsItem.getAmount());
                    Long totalExpense = assetsItem.getTotalExpenses();
                    Long dMoney = possibleReceipts - totalExpense;
                    holder.possibleReceiptsTextView.setText(context.getString(R.string.possibleReceipts, possibleReceipts));
                    holder.totalExpensesTextView.setText(context.getString(R.string.totalExpense, totalExpense));
                    holder.possibleIncomeTextView.setText(context.getString(R.string.possibleProfit, dMoney));
                    long ProfitColor = (long)Math.sqrt(dMoney);
                    if (dMoney > 0) {
                        Log.e("element", String.valueOf((int)Math.min(ProfitColor, 255)));
                        holder.linearLayout.setBackgroundColor(Color.rgb(255 - (int)Math.min(ProfitColor, 255), 255, 255 - (int)Math.min(ProfitColor, 255)));//to be changed
                    } else if (ProfitColor < 0) {
                        holder.linearLayout.setBackgroundColor(Color.rgb(255, 255 - (int)Math.min(-ProfitColor, 255), 255 - (int)Math.min(-ProfitColor, 255)));//to be changed
                    } else {
                        holder.linearLayout.setBackgroundColor(Color.rgb(255, 255, 255));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("images").child(assetsItem.getLanguage()).child("images").child(assetsItem.getHash()).child("price").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (holder.getAdapterPosition() == -1 || !(assetsItems.get(holder.getAdapterPosition()).getHash().equals(assetsItem.getHash()))) {
                    Log.e("shortlistperemoga", "onDataChange: ");
                    FirebaseDatabase.getInstance().getReference().child("images").child(assetsItem.getLanguage()).child("images").child(assetsItem.getHash()).child("price").removeEventListener(this);
                    return;
                }
                if (!dataSnapshot.exists()) {
                    holder.totalExpensesTextView.setText(R.string.memeDeleted);holder.possibleIncomeTextView.setText("");holder.possibleReceiptsTextView.setText("");
                    return;
                }
                Long currPrice = dataSnapshot.getValue(Long.class);
                if (currPrice == null) {
                    holder.totalExpensesTextView.setText(R.string.memeDeleted);holder.possibleIncomeTextView.setText("");holder.possibleReceiptsTextView.setText("");
                    return;
                }
                currentPrice.set(currPrice);
                Log.e("bindData", String.valueOf(currentPrice.get()));
                Long possibleReceipts = costToSell(currentPrice.get(), assetsItem.getAmount());
                Long totalExpense = assetsItem.getTotalExpenses();
                Log.e("bindData", String.valueOf(assetsItem.getTotalExpenses()));
                Long dMoney = possibleReceipts - totalExpense;
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buySellActivity = new Intent(context, com.jattilainen.memebroker.buySellActivity.class);
                buySellActivity.putExtra("memeHash", assetsItems.get(holder.getAdapterPosition()).getHash());
                buySellActivity.putExtra("memeLanguage", assetsItems.get(holder.getAdapterPosition()).getLanguage());
                buySellActivity.putExtra("url", assetsItems.get(holder.getAdapterPosition()).getUrl());
                //Log.e("buypre", assetsItems.get(holder.getAdapterPosition()).getUrl());
                context.startActivity(buySellActivity);
            }
        });
        holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (currentPrice.get() != -1) {
                    AssetsAdapter.OnButtonListener listener = (AssetsAdapter.OnButtonListener) context;
                    listener.onAddButtonSelected(currentPrice.get(), assetsItem.getHash(), assetsItem.getLanguage(), assetsItem.getRatio());
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return assetsItems.size();
    }

    public class AssetsViewHolder extends RecyclerView.ViewHolder {
        public TextView totalExpensesTextView;
        public TextView possibleReceiptsTextView;
        public TextView possibleIncomeTextView;
        public ImageView itemImageView;
        public LinearLayout linearLayout;
        public AssetsViewHolder(View itemView) {
            super(itemView);
            totalExpensesTextView = itemView.findViewById(R.id.assetsTotalExpensesTextView);
            possibleReceiptsTextView = itemView.findViewById(R.id.possibleReceiptsTextView);
            possibleIncomeTextView = itemView.findViewById(R.id.possibleIncomeTextView);
            itemImageView = itemView.findViewById(R.id.assetsItemImageView);
            linearLayout = itemView.findViewById(R.id.assetsItemLinearLayout);
        }
    }
    private long costToSell(long startPrice, Long want) {
        if (want > startPrice) {
            want = startPrice;
        }
        return (2 * startPrice - (want - 1)) * want / 2;
    }
    public void deleteItem(final int pos) {
        assetsItems.remove(pos);
        Log.e("delete", "ll");
        notifyItemRemoved(pos);
    }
}
