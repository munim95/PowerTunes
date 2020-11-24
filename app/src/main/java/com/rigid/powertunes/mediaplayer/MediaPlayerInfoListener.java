package com.rigid.powertunes.mediaplayer;

import android.support.v4.media.session.PlaybackStateCompat;

public abstract class MediaPlayerInfoListener {

    public abstract void onPlaybackStateChange(PlaybackStateCompat state);

    public void onPlaybackCompleted() {
    }

}
