package com.rigid.powertunes.Seekbars;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.helper.Helpers;
import com.rigid.powertunes.misc.ResultReceiverCustom;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION;

public class NowPlayingSeekbar extends AppCompatSeekBar implements SeekBar.OnSeekBarChangeListener, LifecycleOwner, ResultReceiverCustom.Receiver {

    private final String TAG=NowPlayingSeekbar.class.getSimpleName();

//    private ControllerCallback mControllerCallback;
//    private MediaControllerCompat mediaControllerCompat;
    private boolean mActive;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarPositionUpdateTask;
    private int maxSongDuration;
    private boolean isUserTracking;
    private ImageView playBtn,pauseBtn;
    private TextView nowPlayingElapsedText, nowPlayingDurationText;
    private LifecycleRegistry lifecycleRegistry;
    private ResultReceiverCustom resultReceiverCustom=new ResultReceiverCustom(new Handler());
    private Bundle receiverBundle=new Bundle();
    private Bundle progressBundle=new Bundle();
    private MediaControllerCompat mediaControllerCompat;

    public NowPlayingSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        lifecycleRegistry=new LifecycleRegistry(this);
        lifecycleRegistry.setCurrentState(LifecycleRegistry.State.CREATED);
        setOnSeekBarChangeListener(this);
        resultReceiverCustom.setReceiver(((MainActivity)context));
        ResultReceiverCustom resultReceiverCustom2 = new ResultReceiverCustom(new Handler()); //for receiving current position data
        resultReceiverCustom2.setReceiver(this);
        receiverBundle.putParcelable("seekbarreceiver", resultReceiverCustom2);

        //todo null on config change or notification entry when app is closed
        mediaControllerCompat=((MainActivity)context).getMediaControllerCompat();
        if(mediaControllerCompat!=null)
        mediaControllerCompat.registerCallback(callback());
//        SharedFragmentViewModel sharedFragmentViewModel = ViewModelProviders.of((MainActivity) context).get(SharedFragmentViewModel.class);
//        sharedFragmentViewModel.getPlaybackState().observe(this, playbackStateCompatObserver());
//        sharedFragmentViewModel.getMediaMetaDataCompat().observe(this, mediaMetadataCompatObserver());
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
                    pauseBtn.setVisibility(GONE);
                    playBtn.setVisibility(VISIBLE);
                } else{
                    setActive();
                    playBtn.setVisibility(GONE);
                    pauseBtn.setVisibility(VISIBLE);
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
                maxSongDuration= metadata != null?
                        (int) metadata.getLong(METADATA_KEY_DURATION):0;
                setActive();
                nowPlayingDurationText.setText(Helpers.timeConversion((int)TimeUnit.MILLISECONDS.toSeconds(maxSongDuration)));
            }
        };
    }
//    private Observer<PlaybackStateCompat> playbackStateCompatObserver(){
//        return playbackStateCompat -> {
//            if (playbackStateCompat.getState()!=PlaybackStateCompat.STATE_PLAYING) {
//                setInactive(false);
//                pauseBtn.setVisibility(GONE);
//                playBtn.setVisibility(VISIBLE);
//            } else{
//                setActive();
//                playBtn.setVisibility(GONE);
//                pauseBtn.setVisibility(VISIBLE);
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
//            maxSongDuration= mediaMetadataCompat != null?
//                    (int) mediaMetadataCompat.getLong(METADATA_KEY_DURATION):0;
//            setActive();
//            nowPlayingDurationText.setText(Helpers.timeConversion((int)TimeUnit.MILLISECONDS.toSeconds(maxSongDuration)));
//        };
//    }
    private void updateProgressCallbackTask() {
        resultReceiverCustom.send(GlobalVariables.SEEKBAR_POSITION_UPDATE_CODE,receiverBundle);
    }
    //receives current seek position every 100ms
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
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
                100,
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
            lifecycleRegistry.setCurrentState(LifecycleRegistry.State.STARTED);
        }
    }

    public void setInactive(boolean reset) {
        if (mActive) {
            mActive = false;
            stopUpdatingCallbackWithPosition(reset);
        }
    }

    public void setMaxSongDuration(int duration){
        maxSongDuration=duration;
    }

    /**seekbar change listener callbacks*/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        nowPlayingElapsedText.setText(Helpers.timeConversion((int)TimeUnit.MILLISECONDS.toSeconds(progress)));
        int userSelectedPos;
        if(fromUser)
            nowPlayingElapsedText.setText(Helpers.timeConversion((int) TimeUnit.MILLISECONDS.toSeconds(progress)));

    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isUserTracking=true;
        stopUpdatingCallbackWithPosition(false);
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isUserTracking=false;
        progressBundle.putInt("seekbarprogress",seekBar.getProgress());
        resultReceiverCustom.send(GlobalVariables.SEEKBAR_SEEK_UPDATE_CODE,progressBundle); // seek to
//        mediaControllerCompat.getTransportControls().seekTo(seekBar.getProgress());
        startUpdatingCallbackWithPosition();
    }
    public void setPlayPauseBtn(ImageView _playBtn, ImageView _pauseBtn){
        playBtn=_playBtn;
        pauseBtn=_pauseBtn;
    }

    public void setTextViews(TextView nowPlayingElapsedText_, TextView nowPlayingDurationText_){
        nowPlayingElapsedText=nowPlayingElapsedText_;
        nowPlayingDurationText=nowPlayingDurationText_;
    }

    public void destroy() {
        resultReceiverCustom.send(500,null);
        lifecycleRegistry.setCurrentState(LifecycleRegistry.State.DESTROYED);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }
}
