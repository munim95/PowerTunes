package com.rigid.powertunes;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiLevelRvAdapter extends RecyclerView.Adapter {
    List<RvItem> recyclerViewItemList = new ArrayList<>();

    public MultiLevelRvAdapter(List<?> recyclerViewItems) {
        if (!(recyclerViewItems.get(0) instanceof RvItem)) {
            throw new IllegalArgumentException("Please Add Items Of Class extending RecyclerViewItem");
        }
        this.recyclerViewItemList = (List<RvItem>) recyclerViewItems;
    }

    void setRvItemList(List<RvItem> recyclerViewItemList) {
        this.recyclerViewItemList = recyclerViewItemList;
    }

    @Override
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public int getItemCount() {
        return recyclerViewItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return recyclerViewItemList.get(position).getLevel();
    }

    public List<RvItem> getRvItemList() {
        return recyclerViewItemList;
    }
}
