package com.rigid.powertunes.bottomsheetbehaviours;

import android.animation.Animator;
import android.content.Context;

import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.R;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.graphics.Insets;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by munim on 27/12/2018.
 */

public class MainBottomSheet extends BottomSheetBehavior {
    private final String TAG= MainBottomSheet.class.getSimpleName();

    private boolean isSnappingEnabled = false;
    private View scrimView;
    private RelativeLayout sheetTitleView;
    private FrameLayout sheetOptionsView;
    private RecyclerView recyclerView;
    private View nowPlayingCardLayout;
    private RelativeLayout mainBottomSheetLayout;
    private RelativeLayout bottomsheetMenuItems;

    private boolean isScrimVisible;
    private float childTransY;
    private Context ctx;

    private View lastChildView;
    private boolean disableToggle;

    //for initializing fields
    private boolean isCalledOnce=false;

    /**
     * MAIN BOTTOM SHEET
     * CUSTOM SHEET FEATURES:
     * DONE - When scrolled down (dyconsumed>0) the sheet slides down (opposite to appbarlayout behaviour)
     * DONE - When reached BOTTOM of the RECYCLER VIEW it slides up
     * DONE - When expanded it sets scrim background (black)
     * */

    public MainBottomSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        if(!isCalledOnce) {
            addBottomSheetCallback(bottomSheetCallback());
            isCalledOnce=true;
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return super.layoutDependsOn(parent, child, dependency);
    }

    public void setContext(Context context){
        ctx=context;
        scrimView = ((MainActivity)context).findViewById(R.id.bottomSheetScrim);
        sheetTitleView = ((MainActivity)context).findViewById(R.id.sheetPeakTitle);
        sheetOptionsView = ((MainActivity)context).findViewById(R.id.bottomsheet_frag_container);
        bottomsheetMenuItems=((MainActivity)context).findViewById(R.id.bottomsheet_menu_items);
        nowPlayingCardLayout=((MainActivity)context).findViewById(R.id.nowPlayingCardLayout);
        mainBottomSheetLayout=((MainActivity)context).findViewById(R.id.mainBottomsheetLayout);
    }
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        if(!disableToggle) {
            if (target instanceof RecyclerView) {
                recyclerView = (RecyclerView) target;
                recyclerView.addOnChildAttachStateChangeListener(onChildAttachListener());
            }
        }else {
            if (target instanceof RecyclerView) {
                recyclerView = (RecyclerView) target;
                recyclerView.removeOnChildAttachStateChangeListener(onChildAttachListener());
            }
        }
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);

    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);

    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);
        if(!disableToggle) {
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                boolean isLastVisible = ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findLastVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1;

                if (isLastVisible)
                    if (recyclerView.getChildAt(recyclerView.getAdapter().getItemCount() - 1) != null) {
                        lastChildView = recyclerView.getChildAt(recyclerView.getAdapter().getItemCount() - 1);
                    }
                isLastVisible|=lastChildView!=null;
                childTransY = child.getTranslationY();

//        Log.d(TAG,""+getPeekHeight()+" "+ recyclerView.getPaddingBottom()+" "+child.getTranslationY());
                if (getState() == STATE_COLLAPSED) {
                    //custom sliding behaviour
                    if (isLastVisible && dyConsumed > 0 && lastChildView != null) {
                        if (recyclerView.getLayoutManager().getDecoratedBottom(lastChildView) < child.getTop()) {
                            child.setTranslationY(Math.max(0f, child.getTranslationY() - dyConsumed));
                        }
                    }
                    if (!isLastVisible) {
                        //keep the child's y translation b\w its starting height and peek height
                        child.setTranslationY(Math.max(0f, Math.min((float) getPeekHeight(), child.getTranslationY() + dyConsumed)));
                    }
                }
            }
        }
    }
    public void toggleStaticSheet(boolean disable){
        disableToggle=disable;
        if(disable) {
            setPeekHeight(ctx.getResources().getDimensionPixelSize(R.dimen.bottomCardHeight));
            mainBottomSheetLayout.setTranslationY(0f);
        }
    }
    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    private RecyclerView.OnChildAttachStateChangeListener onChildAttachListener(){
        return new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                if(recyclerView.getChildLayoutPosition(view)==recyclerView.getAdapter().getItemCount()-1) {
                    lastChildView = view;
                }
            }
            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if(view==lastChildView){
                    lastChildView=null;
                }
            }
        };
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        setScrimCanceledOnTouch(parent,event);

        return super.onInterceptTouchEvent(parent, child, event);
    }



    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent event) {
        return super.onTouchEvent(parent, child, event);

    }

    //collapse bottomsheet when scrim is touched
    private void setScrimCanceledOnTouch(CoordinatorLayout parent, MotionEvent event){
        if(isScrimVisible){
                if(event.getY()<parent.findViewById(R.id.nowPlayingCardLayout).getTop() && event.getAction()==MotionEvent.ACTION_UP){
                //clicking above the nowplaying card cancels scrim and collapses sheet
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

    private void translationOnDrag(float slideOffset){
        sheetTitleView.setAlpha(1-slideOffset);
        bottomsheetMenuItems.setAlpha(slideOffset);
        bottomsheetMenuItems.setTranslationY(-slideOffset*getPeekHeight());
        sheetOptionsView.setAlpha(slideOffset);
        sheetOptionsView.setTranslationY(-slideOffset*getPeekHeight());
        if (slideOffset == 1) {
            sheetTitleView.setVisibility(View.INVISIBLE);
        } else {
            sheetTitleView.setVisibility(View.VISIBLE);
        }
    }

    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback(){
        return new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState==STATE_EXPANDED){
                    ((MainActivity)ctx).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                }else if(newState==STATE_COLLAPSED){
                    ((MainActivity)ctx).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if(slideOffset>0){
                    //expanding
                    setScrimViewBehaviour(scrimView,true, slideOffset);
                }else if(slideOffset==0){
                    setScrimViewBehaviour(scrimView,false, slideOffset);
                }
                translationOnDrag(slideOffset);
            }
        };
    }
    private void adaptBottomSheetPeekHeight(WindowInsets windowInsets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // If Q, update peek height according to gesture inset bottom
            Insets gestureInsets = windowInsets.getMandatorySystemGestureInsets();
            setPeekHeight(ctx.getResources().getDimensionPixelSize(R.dimen.bottomCardHeight
                    + gestureInsets.bottom));
        }
    }

    public void hideBottomSheetLayout(boolean hide){
        if(hide) {
            setPeekHeight(0,true);
            nowPlayingCardLayout.animate().translationY(nowPlayingCardLayout.getTop()).setDuration(200L).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    nowPlayingCardLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }else{
            setPeekHeight(ctx.getResources().getDimensionPixelSize(R.dimen.bottomCardHeight),true);
            nowPlayingCardLayout.animate().translationY(0).setDuration(200L).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    nowPlayingCardLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
    }


}
