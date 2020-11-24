package com.rigid.powertunes.mediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import com.rigid.powertunes.bottomsheetfragments.AudioSheetFragment;

import androidx.annotation.NonNull;

/**
 * Created by MunimsMac on 22/01/2018.
 */
public class MediaPlayerMain implements MediaPlayerControlInterface, MediaPlayer.OnErrorListener{

    private final String TAG= MediaPlayerMain.class.getSimpleName();
    private MediaPlayer mMediaPlayer;
    private final MediaPlayerInfoListener mMediaPlayerInfoListener;

    private Context mContext;
    private Uri uri;
    private int duration;
    private int mState;
    private boolean mCurrentMediaPlayedToCompletion;
    private PlaybackStateCompat.Builder stateBuilder;
    private AudioFocusManager audioFocusManager;
    private boolean mPlayOnAudioFocus = false;
    private final EqAudioInterface eqAudioInterface = new AudioSheetFragment();
    private boolean shouldBePaused;
    private long current_id=-1;

    public MediaPlayerMain(Context context, MediaPlayerInfoListener mediaPlayerInfoListener, final PlaybackStateCompat.Builder builder) {
        mContext = context.getApplicationContext();
        audioFocusManager =new AudioFocusManager(context);
        mMediaPlayerInfoListener = mediaPlayerInfoListener;
        stateBuilder=builder;
    }

    private void initializeMediaPlayer() {
        if (mMediaPlayer != null)
            return;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
            mCurrentMediaPlayedToCompletion=true;
            mMediaPlayerInfoListener.onPlaybackCompleted();
            // Set the state to "paused" because it most closely matches the state
            // in MediaPlayer with regards to available state transitions compared
            // to "stop".
            // Paused allows: seekTo(), start(), pause(), stop()
            // Stop allows: stop()
            setNewState(PlaybackStateCompat.STATE_PAUSED);
        });

        //init listeners for callbacks...
        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public void loadMedia(Uri uri, long id) {
        initializeMediaPlayer();
        current_id=id;
        if(this.uri!=uri) {
            this.uri = uri;
            try {
                mMediaPlayer.setDataSource(mContext, uri);
            } catch (Exception e) {
                Log.e(TAG, "loadMedia(), setDataSource() for URI - " + uri + ": " + e.toString(), e);
                Toast.makeText(mContext, "Error loading track", Toast.LENGTH_LONG).show();
            }
            try {
                mMediaPlayer.prepare();
            } catch (Exception e) {
                Log.e(TAG, "loadMedia(), prepare(): " + e.toString());
            }
//            if (mMediaPlayer != null) {
//                eqAudioInterface.onLoad(mMediaPlayer.getAudioSessionId());
//
//                if (shouldBePaused)
//                    setNewState(PlaybackStateCompat.STATE_PAUSED);
//            }
        }
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            Log.d(TAG, "RELEASE");
            mMediaPlayer.release();
            mMediaPlayer = null;
            setNewState(PlaybackStateCompat.STATE_NONE);
        }
        else
            Log.e(TAG, "RELEASE called when MEDIA PLAYER == NULL!");
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            Log.d(TAG, "RESET");
            mMediaPlayer.reset();
            setNewState(PlaybackStateCompat.STATE_STOPPED);

        }
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void playFromMetaData(MediaMetadataCompat compat, boolean shouldBePaused) {
        Uri mediaUri = compat.getDescription().getMediaUri();
        this.shouldBePaused = shouldBePaused;
        if(mediaUri!=uri){
            if(!shouldBePaused) {
//                loadMedia(mediaUri);
                play();
//                mMediaPlayer.start();
//                setNewState(PlaybackStateCompat.STATE_PLAYING);
            }else{
//                loadMedia(mediaUri);
//                setNewState(PlaybackStateCompat.STATE_PLAYING);
//                setNewState(PlaybackStateCompat.STATE_PAUSED);
            }
//            mMediaPlayer.setOnPreparedListener(mp -> {
//                if(!shouldBePaused) {
//                    mp.start();
//                    setNewState(PlaybackStateCompat.STATE_PLAYING);
//                }else{
//                    setNewState(PlaybackStateCompat.STATE_PLAYING);
//                    setNewState(PlaybackStateCompat.STATE_PAUSED);
//                }
//            });
        }else{
            if(mMediaPlayer!=null && !mMediaPlayer.isPlaying()) {
                if(shouldBePaused)
                    pause();
                else
                    play();
            }
        }
    }

    @Override
    public void setVolume(float vol) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(vol, vol);
        }
    }

    @Override
    public int getAudioSessionId() {
        return mMediaPlayer!=null?
                mMediaPlayer.getAudioSessionId():-1;
    }

    @Override
    public void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            //Log.d("PLAY", "PLAYING");
            if (audioFocusManager.requestAudioFocus()) {
                mMediaPlayer.start();
                setNewState(PlaybackStateCompat.STATE_PLAYING);
            }
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null) {
            if (!mPlayOnAudioFocus) {
                audioFocusManager.abandonAudioFocus();
            }
            mMediaPlayer.pause();
            setNewState(PlaybackStateCompat.STATE_PAUSED);
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            audioFocusManager.abandonAudioFocus();

            mMediaPlayer.stop();
            setNewState(PlaybackStateCompat.STATE_STOPPED);
        }
    }


    @Override
    public int fetchCurrentDuration() {
        duration = mMediaPlayer.getDuration();
        return mMediaPlayer.getDuration();
    }


    // This is the main reducer for the player state machine.
    private void setNewState(@PlaybackStateCompat.State int newPlayerState) {
        mState = newPlayerState;
        // Whether playback goes to completion, or whether it is stopped, the
        // mCurrentMediaPlayedToCompletion is set to true.
        if (newPlayerState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true;}
        // Work around for MediaPlayer.getCurrentPosition() when it changes while not playing.
        final long reportPosition =
                mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();

//        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(getAvailableActions());
        stateBuilder.setState(newPlayerState,
                reportPosition,
                1.0f,
                SystemClock.elapsedRealtime());
        stateBuilder.setActiveQueueItemId(current_id);
        mMediaPlayerInfoListener.onPlaybackStateChange(stateBuilder.build());
    }


    /**
     * Set the current capabilities available on this session. Note: If a capability is not
     * listed in the bitmask of capabilities then the MediaSession will not handle it. For
     * example, if you don't want ACTION_STOP to be handled by the MediaSession, then don't
     * included it in the bitmask that's returned.
     */
    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        switch (mState) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    @Override
    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
            setNewState(mState);
        }
    }

    /**
     * Internal Error Listener
     * */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        setNewState(PlaybackStateCompat.STATE_ERROR);
        Log.e("MediaErrorListener",
                "ERROR TYPE: "+
                            (i==MediaPlayer.MEDIA_ERROR_UNKNOWN?"Unknown Error":
                            i==MediaPlayer.MEDIA_ERROR_SERVER_DIED?"Server Died":"CHECK CODE: "+i) +
                        "TYPE EXTRA: " +
                            (i1==MediaPlayer.MEDIA_ERROR_IO?"IO Error":
                            i1==MediaPlayer.MEDIA_ERROR_MALFORMED?"Malformed":
                            i1==MediaPlayer.MEDIA_ERROR_TIMED_OUT?"Timed Out":
                            i1==MediaPlayer.MEDIA_ERROR_UNSUPPORTED?"Unsupported": "UNKNOWN. CHECK CODE: "+i1));
        return false;
    }


    /**
     * Audio Focus Manager
     * */
    class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {
        private static final float MEDIA_VOLUME_DEFAULT = 1.0f;
        private static final float MEDIA_VOLUME_DUCK = 0.2f;

        private final Context mApplicationContext;
        private final AudioManager mAudioManager;


        AudioFocusManager(@NonNull Context context) {
            mApplicationContext = context.getApplicationContext();
            mAudioManager = (AudioManager) mApplicationContext.getSystemService(Context.AUDIO_SERVICE);
        }

        private boolean requestAudioFocus() {
            final int result;
            if(mAudioManager!=null){
                result = mAudioManager.requestAudioFocus(this,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
                return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            }
            return false;
        }

        private void abandonAudioFocus() {
            if(mAudioManager!=null)
                mAudioManager.abandonAudioFocus(this);
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPlayOnAudioFocus && !isPlaying()) {
                        play();
                        Log.d("audiofocus", "gainnn");
                    } else if (isPlaying()) {
                        setVolume(MEDIA_VOLUME_DEFAULT);
                    }
                    mPlayOnAudioFocus = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    setVolume(MEDIA_VOLUME_DUCK);
                    Log.d("audiofocus", "duckkk");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (isPlaying()) {
                        mPlayOnAudioFocus = true;
                        pause();
                        Log.d("audiofocus", "transienttt");
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    abandonAudioFocus();
                    mPlayOnAudioFocus = false;
                    pause();
                    break;
            }
        }
    }
}
