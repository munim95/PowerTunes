package com.rigid.powertunes.bottomsheetbehaviours;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.R;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SelectionBottomSheet extends BottomSheetBehavior {
    private Context ctx;
    private GridLayout selectionOptionsView;
    private GridLayout expParentOptionsView;

    //for initializing fields
    private boolean isCalledOnce=false;

    public SelectionBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx=context;
        setBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState==STATE_COLLAPSED){
                    if(GlobalSelectionTracker.getMySelection().hasSelection())
                    GlobalSelectionTracker.getMySelection().clearSelection();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }


    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        if(!isCalledOnce) {
            selectionOptionsView =parent.findViewById(R.id.selection_options);
            expParentOptionsView =parent.findViewById(R.id.exp_par_selection_options);
            isCalledOnce=true;
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        return super.onInterceptTouchEvent(parent, child, event);
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        return super.onTouchEvent(parent, child, event);
    }
    //todo playlist, search options
    public void setOptionsMenuVisible(boolean visible,RecyclerView recyclerView,boolean isPlaylist){
        if(visible){
            if(!isPlaylist) {
                expParentOptionsView.setVisibility(View.GONE);
                selectionOptionsView.setVisibility(View.VISIBLE);
            }else {
                selectionOptionsView.setVisibility(View.GONE);
                expParentOptionsView.setVisibility(View.VISIBLE);
            }
            setState(STATE_EXPANDED);
            recyclerView.setPadding(0,0,0,ctx.getResources().getDimensionPixelSize(R.dimen.options_peak_height));
        }else{
            setState(STATE_COLLAPSED);
            if(selectionOptionsView!=null)
            selectionOptionsView.setVisibility(View.GONE);
            if(expParentOptionsView!=null)
            expParentOptionsView.setVisibility(View.GONE);

            recyclerView.setPadding(0, 0, 0, ctx.getResources().getDimensionPixelSize(R.dimen.standard_rv_padding));
        }
    }
    public void setSearchSelectionVisible(boolean visible,RecyclerView recyclerView){

    }
}
