package com.rigid.powertunes.Seekbars;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.R;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.helper.Helpers;
import com.rigid.powertunes.misc.ResultReceiverCustom;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION;

/**
 * Created by MunimsMac on 01/03/2018.
 */

public class NowPlayingCardSeekBar extends androidx.appcompat.widget.AppCompatSeekBar
        implements SeekBar.OnSeekBarChangeListener, ResultReceiverCustom.Receiver {

    private final String TAG= NowPlayingCardSeekBar.class.getSimpleName();

    private boolean mActive;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarPositionUpdateTask;
    private int maxSongDuration;
    private TextView timerText;
    private View indicator;
    private View nowPlayingCard;
    private Context ctx;
    private ResultReceiverCustom resultReceiverCustom=new ResultReceiverCustom(new Handler());
    private Bundle receiverBundle=new Bundle();
    private MediaControllerCompat mediaControllerCompat;

    public NowPlayingCardSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx=context;
        setOnSeekBarChangeListener(this);
        resultReceiverCustom.setReceiver(((MainActivity)context));
        ResultReceiverCustom resultReceiverCustom2 = new ResultReceiverCustom(new Handler()); //for receiving current position data
        resultReceiverCustom2.setReceiver(this);
        receiverBundle.putParcelable("seekbarreceiver", resultReceiverCustom2);
    }
    public void setMediaControllerCompat(MediaControllerCompat mediaControllerCompat){
        this.mediaControllerCompat=mediaControllerCompat;
        if(mediaControllerCompat!=null)
            mediaControllerCompat.registerCallback(callback());
    }
    private MediaControllerCompat.Callback callback() {
        return new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                super.onPlaybackStateChanged(state);
                if (state.getState()!=PlaybackStateCompat.STATE_PLAYING) {
                    setInactive(false);
                } else{
                    setActive();
                }

                //fixes glitch in seekbar due to state change
                final int progress = state != null
                        ? (int) state.getPosition()
                        : 0;
                setProgress(progress);
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                super.onMetadataChanged(metadata);
                setInactive(true);
                setProgress(0);
                maxSongDuration= metadata!=null?
                        (int)metadata.getLong(METADATA_KEY_DURATION):0;
                setActive();
            }
        };
    }
//    private Observer<PlaybackStateCompat> playbackStateCompatObserver(){
//        return playbackStateCompat -> {
//            if (playbackStateCompat.getState()!=PlaybackStateCompat.STATE_PLAYING) {
//                setInactive(false);
//            } else{
//                setActive();
//            }
//
//            //fixes glitch in seekbar due to state change
//            final int progress = playbackStateCompat != null
//                    ? (int) playbackStateCompat.getPosition()
//                    : 0;
//            setProgress(progress);
//        };
//    }
//    private Observer<MediaMetadataCompat> mediaMetadataCompatObserver(){
//        return mediaMetadataCompat -> {
//            setInactive(true);
//            setProgress(0);
//            maxSongDuration= mediaMetadataCompat!=null?
//                    (int)mediaMetadataCompat.getLong(METADATA_KEY_DURATION):0;
//            setActive();
//        };
//    }

    //handles indicator ondraw
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        indicatorOnDraw();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getActionMasked()==MotionEvent.ACTION_DOWN){
            indicator.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200);
        }else if(event.getActionMasked()==MotionEvent.ACTION_UP){
            indicator.animate().alpha(0).scaleX(0.8f).scaleY(0.8f).setDuration(200);
        }else if(event.getActionMasked()==MotionEvent.ACTION_CANCEL){
            indicator.animate().alpha(0).scaleX(0.8f).scaleY(0.8f).setDuration(200);
        }
        return super.onTouchEvent(event);
    }

    private void indicatorOnDraw(){
        //progress
//        int lineLeft=((getWidth()*getProgress())/getMax())/10;
        // 10dp is the total margin on either side
        int totalCardWidth=nowPlayingCard.getWidth()+ctx.getResources().getDimensionPixelSize(R.dimen.nowplayingcard_margins);
        //left position of the indicator and also bound
        int startSide=(totalCardWidth-getWidth())/2;
        //right positon of the indicator
        int rightSide=(getWidth()+startSide)-indicator.getWidth();
        //right bound of the indicator where it stops
        int rightBound=getWidth()-(indicator.getWidth()/2);

        if(getThumb().getBounds().centerX()<(startSide/2)) {
            indicator.setX(startSide);
        }else if(getThumb().getBounds().centerX()>startSide/2 && getThumb().getBounds().centerX()<rightBound) {
            indicator.setX(startSide/2+getThumb().getBounds().centerX());
        }else{
            indicator.setX(rightSide);
        }
    }

    private void updateProgressCallbackTask() {
        resultReceiverCustom.send(GlobalVariables.SEEKBAR_POSITION_UPDATE_CODE,receiverBundle);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        //retrieve time position of song and set
        if(resultCode==100){
            setProgress((int)resultData.getLong("seekbarposition"));
        }
    }

    private void startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (mSeekbarPositionUpdateTask == null) {
            mSeekbarPositionUpdateTask = this::updateProgressCallbackTask;
        }
        //scheduled every second
        mExecutor.scheduleAtFixedRate(
                mSeekbarPositionUpdateTask,
                0,
                1000,
                TimeUnit.MILLISECONDS);
    }

    private void stopUpdatingCallbackWithPosition(boolean resetUIPlaybackPosition) {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
            removeCallbacks(mSeekbarPositionUpdateTask);
            mSeekbarPositionUpdateTask = null;
            if (resetUIPlaybackPosition) {
                setProgress(0);
            }
        }
    }

    public void setActive() {
        if (!mActive) {
                mActive = true;
                setMax(maxSongDuration);
                startUpdatingCallbackWithPosition();
        }
    }

    public void setInactive(boolean reset) {
        if (mActive) {
                mActive = false;
                stopUpdatingCallbackWithPosition(reset);
        }
    }

    /**seekbar change listener callbacks*/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        timerText.setText(Helpers.timeConversion((int)TimeUnit.MILLISECONDS.toSeconds(progress)));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        stopUpdatingCallbackWithPosition(false);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
//        progressBundle.putInt("seekbarprogress",seekBar.getProgress());
        mediaControllerCompat.getTransportControls().seekTo(seekBar.getProgress());
//        resultReceiverCustom.send(GlobalVariables.SEEKBAR_SEEK_UPDATE_CODE,progressBundle); // seek to
        startUpdatingCallbackWithPosition();
    }
    public void setIndicator(View _indicator){
        indicator=_indicator;
    }
    public void setNowPlayingCard(View _nowPlayingCard){
        nowPlayingCard=_nowPlayingCard;
    }
    public void setTimerText(TextView text){
        timerText=text;
    }

    public void setMaxSongDuration(int duration){
        maxSongDuration=duration;
    }

}
