package com.rigid.powertunes.recyclerviewhelpers;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.mediaservice.MediaLibrary;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{
    private GestureDetector gestureDetector;
    private ListClickListener mListener;


    public RecyclerTouchListener(Context ctx, final RecyclerView recyclerView, ListClickListener listClickListener){
        mListener = listClickListener;

        gestureDetector = new GestureDetector(ctx, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                //Log.d("GESTURE DETECTED", "ACTION UP" + e);
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //helpful in determining if scrolling up or down
//                Log.d("oon scroll", "Y= "+ distanceY);
//                rvIsScrollIdle = !(distanceY > 0);

                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
//                Log.d("GESTURE DETECTED", "LONG PRESS "+recyclerView.getScrollState());
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && mListener != null) {
                        mListener.onLongClick(child, recyclerView.getChildLayoutPosition(child));
                    }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        //Log.d("Intercept Event", "INTERCEPTING \n" + e);
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && mListener != null && gestureDetector.onTouchEvent(e)) {
                int position = rv.getChildLayoutPosition(child);
                mListener.click(child, position);
            }

        //true if onTouchEvent is to be called, false if you want gesture detector to handle events
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

}
