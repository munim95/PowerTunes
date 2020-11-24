package com.rigid.powertunes.mediaplayer;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

public interface PlaybackModeInterface {
    int SHUFFLE_MODE_NONE=0;
    int SHUFFLE_MODE_ALL=1;

    int REPEAT_MODE_NONE=0;
    int REPEAT_MODE_ONE=1;
    int REPEAT_MODE_ALL =2;
//    int REPEAT_MODE_NEXT=3;

    @IntDef({SHUFFLE_MODE_NONE,SHUFFLE_MODE_ALL})
    @Retention(RetentionPolicy.SOURCE)
    @interface ShuffleMode{}

    @IntDef({REPEAT_MODE_NONE,REPEAT_MODE_ONE, REPEAT_MODE_ALL})
    @Retention(RetentionPolicy.SOURCE)
    @interface RepeatMode{}

    void onShuffleMode(@ShuffleMode int shuffleMode);
    void onRepeatMode(@RepeatMode int repeatMode);


}
