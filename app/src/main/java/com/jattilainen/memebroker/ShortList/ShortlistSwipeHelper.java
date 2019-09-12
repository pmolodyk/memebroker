package com.jattilainen.memebroker.ShortList;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Vadim on 03.06.2018.
 */

public class ShortlistSwipeHelper extends ItemTouchHelper.SimpleCallback{

    ShortlistAdapterUI adapter;
    public ShortlistSwipeHelper(ShortlistAdapterUI adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.deleteItem(viewHolder.getAdapterPosition());
    }
}
