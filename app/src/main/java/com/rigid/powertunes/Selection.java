package com.rigid.powertunes;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.AlbumsAdapter;
import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.ArtistsAdapter;
import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.FoldersAdapter;
import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.GenresAdapter;
import com.rigid.powertunes.bottomsheetfragments.bottomsheetadapters.PlaylistAdapter;
import com.rigid.powertunes.main.fragments.fragmentadapters.SongsAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Selection implements RecyclerView.OnItemTouchListener {
    private final String TAG = this.getClass().getSimpleName();
    private final String SELECTION_ENTRIES = "selection_entries";
    private GestureDetector gestureDetector;
    private boolean hasSelection = false;
    private RecyclerView.Adapter adapterRv;
    private Set<Uri> selection;
    private SelectionCallback selectionCallback;

    //for tasks that rely on selection change
    public interface SelectionCallback{
        void onSelectionChanged();
    }

    public Selection(Context context,RecyclerView recyclerView, SelectionCallback selectionCallback){
        recyclerView.addOnItemTouchListener(this);
        adapterRv = recyclerView.getAdapter();
        selection=new HashSet<>();
        this.selectionCallback=selectionCallback;
        gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "ACTION UP");
                if(hasSelection){
                    //keep adding
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if(child!=null){
                        RecyclerView.ViewHolder viewHolder = recyclerView.findContainingViewHolder(child);
                        Uri uri = getUriFromViewHolder(viewHolder);
                        if(uri!=null)
                            if(isSelected(uri.toString())){
                                selection.remove(uri);
                                if(selection.isEmpty())
                                    hasSelection=false;
                            }
                            else
                                selection.add(uri);

                        notifyChange();
                        adapterRv.notifyItemChanged(recyclerView.getChildAdapterPosition(child));
                    }else{
                        clearSelection();
                    }
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "LONG PRESS ");

                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    hasSelection = true;
                    RecyclerView.ViewHolder viewHolder = recyclerView.findContainingViewHolder(child);
                    Uri uri = getUriFromViewHolder(viewHolder);
                    if(uri!=null)
                        if(!isSelected(uri.toString())) {
                            selection.add(uri);
                            adapterRv.notifyItemChanged(recyclerView.getChildAdapterPosition(child));
                            notifyChange();
                        }
                }
            }
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }


            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        //true if onTouchEvent is to be called, false if you want gesture detector to handle events
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private void notifyChange(){
        if(selectionCallback!=null)
            selectionCallback.onSelectionChanged();
    }
    private Uri getUriFromViewHolder(RecyclerView.ViewHolder viewHolder){
        if(viewHolder instanceof SongsAdapter.SongsViewHolder){
            return Uri.parse(((SongsAdapter.SongsViewHolder)viewHolder).getSongUri());
        }else if(viewHolder instanceof PlaylistAdapter.PlaylistViewHolder){
            return Uri.parse(((PlaylistAdapter.PlaylistViewHolder)viewHolder).getUri());
        }else if(viewHolder instanceof GenresAdapter.GenresViewHolder){
            return Uri.parse(((GenresAdapter.GenresViewHolder)viewHolder).getUri());
        }else if(viewHolder instanceof FoldersAdapter.FoldersViewHolder){
            return Uri.parse(((FoldersAdapter.FoldersViewHolder)viewHolder).getUri());
        }else if(viewHolder instanceof ArtistsAdapter.ArtistViewHolder){
            return Uri.parse(((ArtistsAdapter.ArtistViewHolder)viewHolder).getUri());
        }else if(viewHolder instanceof AlbumsAdapter.AlbumsViewHolder){
            return Uri.parse(((AlbumsAdapter.AlbumsViewHolder)viewHolder).getUri());
        }
        return null;
    }
    public boolean isSelected(String uri){
        return selection.contains(Uri.parse(uri));
    }
    public Set<Uri> getSelection(){
        return selection;
    }
    public boolean hasSelection(){
        return hasSelection;
    }
    public void clearSelection(){
        selection.clear();
        hasSelection=false;
        adapterRv.notifyItemRangeChanged(0,adapterRv.getItemCount());
        notifyChange();
    }
    public void setItemsSelected(Iterable<Uri> keys, boolean select){
        for(Uri str : keys){
            if(select)
                selection.add(str);
            else
                selection.remove(str);
        }
        adapterRv.notifyItemRangeChanged(0,adapterRv.getItemCount());
    }

    //if ever needed - for now config change while selection is disabled anyway
    public void onSaveInstanceState(@NonNull Bundle state){
        ArrayList<Uri> value = new ArrayList<>(selection.size());
        value.addAll(selection);
        state.putParcelableArrayList(SELECTION_ENTRIES, value);
    }
    public void onRestoreInstanceState(@Nullable Bundle state){
        if(state==null)
            return;

        ArrayList<Uri> selection = state.getParcelableArrayList(SELECTION_ENTRIES);
        if (selection != null && !selection.isEmpty()) {
            setItemsSelected(selection,true);
            notifyChange();
        }
    }
}
