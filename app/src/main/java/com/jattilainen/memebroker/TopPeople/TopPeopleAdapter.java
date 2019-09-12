package com.jattilainen.memebroker.TopPeople;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.jattilainen.memebroker.Constants;
import com.jattilainen.memebroker.R;

import java.util.List;

public class TopPeopleAdapter extends RecyclerView.Adapter<TopPeopleAdapter.TopPeopleViewHolder> {
    private List<TopPeopleItem> topPeopleItems;
    private Context context;
    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    public TopPeopleAdapter(List<TopPeopleItem> topPeopleItems, Context context) {
        this.topPeopleItems = topPeopleItems;
        this.context = context;
    }

    @NonNull
    @Override
    public TopPeopleAdapter.TopPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.top_people_item, parent, false);
        return new TopPeopleAdapter.TopPeopleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TopPeopleAdapter.TopPeopleViewHolder holder, int position) {
        final TopPeopleItem topPeopleItem = topPeopleItems.get(holder.getAdapterPosition());
        holder.moneyTextView.setText(String.valueOf(topPeopleItem.getMoney()));
        holder.orderTextView.setText(context.getString(R.string.topPeopleOrder, topPeopleItem.getOrder()));
        if (topPeopleItem.getOrder() == Constants.TOP_PEOPLE_TOTAL_LOAD_SIZE) {
            holder.orderTextView.setText(context.getString(R.string.topPeopleOrderBig, topPeopleItem.getOrder() - 1));
        }
        holder.nameTextView.setText(topPeopleItem.getName());
        setBackgroundColor(holder.linearLayout, topPeopleItem.getOrder());
        if (topPeopleItem.getName().equals(name)) {
            holder.linearLayout.setBackgroundColor(context.getResources().getColor(R.color.Green));
        }
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent personProfileActivity = new Intent(context, com.jattilainen.memebroker.PersonProfile.PersonProfileActivity.class);
                personProfileActivity.putExtra("name", topPeopleItem.getName());
                context.startActivity(personProfileActivity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return topPeopleItems.size();
    }

    public class TopPeopleViewHolder extends RecyclerView.ViewHolder {
        public TextView moneyTextView;
        public TextView nameTextView;
        public TextView orderTextView;
        public LinearLayout linearLayout;
        public TopPeopleViewHolder(View itemView) {
            super(itemView);
            moneyTextView = itemView.findViewById(R.id.TopPeopleItemMoneyTextView);
            nameTextView = itemView.findViewById(R.id.TopPeopleItemNameTextView);
            orderTextView = itemView.findViewById(R.id.TopPeopleItemOrderTextView);
            linearLayout = itemView.findViewById(R.id.TopPeopleItemLinearLayout);
        }
    }
    private void setBackgroundColor(LinearLayout linearLayout, int order) {
//        if (order <= Constants.TOP_PEOPLE_FIRST_COLOR_BLOCK_SIZE) {
//            if (order % 2 == 0) {
//                linearLayout.setBackgroundColor(context.getResources().getColor(R.color.Orange));
//            } else {
//                linearLayout.setBackgroundColor(context.getResources().getColor(R.color.DarkOrange));
//            }
//        } else {
            if (order % 2 == 0) {
                linearLayout.setBackgroundColor(context.getResources().getColor(R.color.LightSilver));
            } else {
                linearLayout.setBackgroundColor(context.getResources().getColor(R.color.DarkSilver));
            }
        }
    //}
}
