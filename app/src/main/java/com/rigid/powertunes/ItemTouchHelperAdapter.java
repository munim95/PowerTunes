package com.rigid.powertunes;

import androidx.recyclerview.widget.RecyclerView;

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

    void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState);
}
