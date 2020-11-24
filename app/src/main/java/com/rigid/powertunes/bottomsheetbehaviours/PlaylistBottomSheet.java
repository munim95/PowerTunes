package com.rigid.powertunes.bottomsheetbehaviours;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.R;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class PlaylistBottomSheet extends BottomSheetBehavior {
    private Context ctx;
    private View scrimView;
    private boolean isScrimVisible;

    //for initializing fields
    private boolean isCalledOnce = false;

    public PlaylistBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        setBottomSheetCallback(new BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState==STATE_EXPANDED){
                    ((MainActivity)ctx).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                }else if(newState==STATE_COLLAPSED){
                    ((MainActivity)ctx).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    ((MainActivity)ctx).getSupportFragmentManager().beginTransaction()
                            .remove(((MainActivity)ctx).getSupportFragmentManager().findFragmentByTag("AddToPlaylistFragment"));
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > 0) {
                    //expanding
                    setScrimViewBehaviour(scrimView, true, slideOffset);
                } else if (slideOffset == 0) {
                    setScrimViewBehaviour(scrimView, false, slideOffset);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        setScrimCanceledOnTouch(child, event);
        return super.onInterceptTouchEvent(parent, child, event);
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        return super.onTouchEvent(parent, child, event);

    }

    //collapse bottomsheet when scrim is touched
    private void setScrimCanceledOnTouch(View child, MotionEvent event) {
        if (isScrimVisible) {
            //cancel on touch above child
            if (event.getY() < child.getTop() && event.getAction() == MotionEvent.ACTION_UP) {
                setState(STATE_COLLAPSED);
                isScrimVisible = false;
            }
        }
    }

    private void setScrimViewBehaviour(View scrimView, boolean visibilty, float alpha) {
        isScrimVisible = visibilty;

        if (visibilty) {
            scrimView.setVisibility(View.VISIBLE);
            scrimView.setClickable(true);
            scrimView.setAlpha(alpha * 0.5f);
        } else {
            scrimView.setVisibility(View.GONE);
            scrimView.setClickable(false);
        }
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        if (!isCalledOnce) {
            scrimView = parent.findViewById(R.id.bottomSheetScrim);

            isCalledOnce = true;
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }


}
