package com.rigid.powertunes.recyclerviewhelpers;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rigid.powertunes.R;
import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import androidx.recyclerview.widget.RecyclerView;

public class ScrollerLinearLayout extends LinearLayout {

    //todo disable touch on recycler view
    private ArrayList<Song> songs;
    private RecyclerView recyclerView;
    private TextView letterBox;
    private FrameLayout letterBoxHolder;
    private View emptyView;

    public ScrollerLinearLayout(Context context) {
        super(context);
    }

    public ScrollerLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(songs!=null) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                letterBoxHolder.animate().scaleX(1f).scaleY(1f).setDuration(150L);
                emptyView.animate().alpha(0.5f).setDuration(150L);
                letterBox.animate().alpha(1f).setDuration(150L);
                for (int i = 0; i < getChildCount(); i++) {
                    if (getChildAt(i) instanceof TextView) {
                        if (event.getY() >= getChildAt(i).getY() && event.getY() <= getChildAt(i).getBottom()) {
                            String s = ((TextView) getChildAt(i)).getText().toString();
                            if(event.getY()>=getChildAt(i).getTop() && event.getY()<=getHeight()) {
                                letterBoxHolder.setY(event.getY() + letterBoxHolder.getTop());
                                letterBox.setText(s);
                            }
                            if (calculateIndexesForName(songs).containsKey(s)) {
                                recyclerView.scrollToPosition(calculateIndexesForName(songs).get(s));
//                                Log.d("scroller", ((TextView) getChildAt(i)).getText() + "");
                            }
                        }
                    }
                }
            }
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                for (int i = 0; i < getChildCount(); i++) {
                    if (getChildAt(i) instanceof TextView) {
                        if (event.getY() >= getChildAt(i).getTop() && event.getY() <= getChildAt(i).getBottom()) {
                            String s = ((TextView) getChildAt(i)).getText().toString();
                            if(event.getY()>=getChildAt(i).getTop() && event.getY()<=getHeight()) {
                                letterBoxHolder.setY(event.getY() + letterBoxHolder.getTop());
                                letterBox.setText(s);
                            }
                            if (calculateIndexesForName(songs).containsKey(s)) {
                                recyclerView.scrollToPosition(calculateIndexesForName(songs).get(s));
//                                Log.d("scroller", ((TextView) getChildAt(i)).getText() + "");
                            }
                        }
                    }
                }
            }
            if(event.getActionMasked()==MotionEvent.ACTION_UP){
                letterBoxHolder.animate().scaleX(0.8f).scaleY(0.8f).setDuration(150L);
                emptyView.animate().alpha(0f).setDuration(150L);
                letterBox.animate().alpha(0f).setDuration(150L);
//                letterBox.setVisibility(GONE);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    private HashMap<String, Integer> calculateIndexesForName(ArrayList<Song> items){
        HashMap<String, Integer> mapIndex = new LinkedHashMap<>();
        for (int i = 0; i<items.size(); i++){
            String name = items.get(i).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            String index = name.substring(0,1);
            index = index.toUpperCase();

            if (!mapIndex.containsKey(index)) {
                mapIndex.put(index, i);
            }
        }
        return mapIndex;
    }

    public void swapData(ArrayList<Song> songs){
        this.songs=songs;
    }
    public void setRecyclerView(RecyclerView recyclerView){
        this.recyclerView=recyclerView;
    }
    public void setLetterBox(View view) {
        letterBox=view.findViewById(R.id.letterBox);
        letterBoxHolder=view.findViewById(R.id.letterBoxHolder);
        emptyView=view.findViewById(R.id.emptyView);
    }


}
