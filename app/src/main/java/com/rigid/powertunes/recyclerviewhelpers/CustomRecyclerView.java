package com.rigid.powertunes.recyclerviewhelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerView extends RecyclerView {
    private boolean shouldDisable;
    public CustomRecyclerView(@NonNull Context context) {
        super(context);
    }

    public CustomRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        Log.d("customrv","dis"+shouldDisable);
        if(shouldDisable) {
            return false;
        }else
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.d("customrv","dis"+shouldDisable);
        if(shouldDisable) {
            return false;
        }else{
            return super.onTouchEvent(e);
        }
    }

    public void shouldDisableTouch(boolean disable){
        shouldDisable=disable;
    }
}
