package com.rigid.powertunes.mediaplayer;

import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;

/**
 * Created by MunimsMac on 22/01/2018.
 */

public interface MediaPlayerControlInterface {

    void loadMedia(Uri uri, long id);

    void release();

    boolean isPlaying();

    void play();

    void reset();

    void pause();

    int fetchCurrentDuration();

    void seekTo(int position);

    void stop();

    void playFromMetaData(MediaMetadataCompat compat,boolean shouldBePaused);

    void setVolume(float vol);

    int getAudioSessionId();
}
