package com.jattilainen.memebroker.MemeLanguageChoose;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jattilainen.memebroker.R;

import java.util.Collections;
import java.util.List;

public class MemeLangChooseAdapter extends RecyclerView.Adapter<MemeLangChooseAdapter.ViewHolder>{
     private List<MemeLanguage> memeLanguages;
     private Context context;

    public interface OnButtonListener {
        void onLangSelected(String memeLanguage, String langTranslation);
    }
    public MemeLangChooseAdapter(List<MemeLanguage> memeLanguages, Context context) {
        this.memeLanguages = memeLanguages;
        this.context = context;
    }

    @NonNull
    @Override
    public MemeLangChooseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.meme_lang_choose_item, parent, false);
        return new MemeLangChooseAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemeLangChooseAdapter.ViewHolder holder, int position) {
        final MemeLanguage memeLanguage = memeLanguages.get(holder.getAdapterPosition());
        holder.langTextView.setText(memeLanguage.getTranslatedLang());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MemeLangChooseAdapter.OnButtonListener listener = (MemeLangChooseAdapter.OnButtonListener) context;
                listener.onLangSelected(memeLanguage.getOriginLang(), memeLanguage.getTranslatedLang());

            }
        });
    }

    @Override
    public int getItemCount() {
        return memeLanguages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView langTextView;
        ViewHolder(View itemView) {
            super(itemView);
            langTextView = itemView.findViewById(R.id.memeChooseLangItemLangTextView);
            cardView = itemView.findViewById(R.id.memeChooseLangItemCardView);
        }
    }
}
