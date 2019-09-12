package com.jattilainen.memebroker.Assets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jattilainen.memebroker.R;

public class AssetsViewHolderUI extends RecyclerView.ViewHolder {
    public TextView totalExpensesTextView;
    public TextView possibleReceiptsTextView;
    public TextView possibleIncomeTextView;
    public ImageView itemImageView;
    public LinearLayout linearLayout;
    public AssetsViewHolderUI(View itemView) {
        super(itemView);
        totalExpensesTextView = itemView.findViewById(R.id.assetsTotalExpensesTextView);
        possibleReceiptsTextView = itemView.findViewById(R.id.possibleReceiptsTextView);
        possibleIncomeTextView = itemView.findViewById(R.id.possibleIncomeTextView);
        itemImageView = itemView.findViewById(R.id.assetsItemImageView);
        linearLayout = itemView.findViewById(R.id.assetsItemLinearLayout);
    }
}
