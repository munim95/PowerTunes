package com.rigid.powertunes.bottomsheetbehaviours;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.R;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class ConfirmationBottomSheet extends BottomSheetBehavior {
    private Context ctx;
    private TextView titleText,confirmText;
    private View scrimView;
    private boolean isScrimVisible;
    private Button removeBtn;

    //for initializing fields
    private boolean isCalledOnce=false;

    public ConfirmationBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx=context;
        addBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if(slideOffset>0){
                    //expanding
                    setScrimViewBehaviour(scrimView,true, slideOffset);
                }else if(slideOffset==0){
                    setScrimViewBehaviour(scrimView,false, slideOffset);
                }
            }
        });
    }
    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        setScrimCanceledOnTouch(child,event);

        return super.onInterceptTouchEvent(parent, child, event);
    }
    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        return super.onTouchEvent(parent, child, event);

    }
    //collapse bottomsheet when scrim is touched
    private void setScrimCanceledOnTouch(View child, MotionEvent event){
        if(isScrimVisible){
            //cancel on touch above child
            if(event.getY()<child.getTop() && event.getAction()==MotionEvent.ACTION_UP) {
                setState(STATE_COLLAPSED);
                isScrimVisible=false;
            }
        }
    }

    private void setScrimViewBehaviour(View scrimView, boolean visibilty, float alpha){
        isScrimVisible=visibilty;
        if(visibilty){
            scrimView.setVisibility(View.VISIBLE);
            scrimView.setClickable(true);
            scrimView.setAlpha(alpha*0.5f);
        } else{
            scrimView.setVisibility(View.GONE);
            scrimView.setClickable(false);
        }
    }
    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        if(!isCalledOnce) {
            scrimView = parent.findViewById(R.id.bottomSheetScrim);
            parent.findViewById(R.id.bottomDialogCancel).setOnClickListener(clickListener());
            titleText=parent.findViewById(R.id.bottomConfirmDeleteTitle);
            confirmText=parent.findViewById(R.id.bottomConfirmText);
            removeBtn=parent.findViewById(R.id.bottomDialogRemove);
            isCalledOnce=true;
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }
    private View.OnClickListener clickListener(){
        return v -> setState(STATE_COLLAPSED);
    }

    public void showConfirmationDialog(boolean isFolder,boolean isPlaylist,boolean isPlaylistSong) {
        if(!isFolder) {
            if (GlobalSelectionTracker.getMySelection().getSelection().size() == 1) {
                if(isPlaylistSong){
                    removeBtn.setVisibility(View.VISIBLE);
                    titleText.setText("Remove Song?");
                    confirmText.setText("Caution: Removing will NOT delete the song from device. Press delete to permanently erase.");
                }else {
                    removeBtn.setVisibility(View.GONE);
                    titleText.setText("Delete Song?");
                    confirmText.setText("Permanently delete song?");
                }
            } else {
                if(isPlaylistSong){
                    removeBtn.setVisibility(View.VISIBLE);
                    titleText.setText("Remove Songs?");
                    confirmText.setText("Caution: Removing will NOT delete the songs from device. Press delete to permanently erase.");
                }else{
                    removeBtn.setVisibility(View.GONE);
                    titleText.setText("Delete Songs?");
                    confirmText.setText("Permanently delete songs?");
                }
            }
        }else{
            removeBtn.setVisibility(View.GONE);
            if(GlobalSelectionTracker.getMySelection().getSelection().size()==1) {
                if (!isPlaylist) {
                    titleText.setText("Delete Songs In Folder?");
                    confirmText.setText("This will permanently delete songs in this folder.");
                } else {
                    titleText.setText("Delete Playlist?");
                    confirmText.setText("Songs will not be deleted.");
                }
            }else{
                if (!isPlaylist) {
                    titleText.setText("Delete Songs In Folders?");
                    confirmText.setText("This will permanently delete songs in selected folders.");
                } else {
                    titleText.setText("Delete Playlists?");
                    confirmText.setText("Songs will not be deleted.");
                }
            }
        }
        setState(STATE_EXPANDED);
    }

}
