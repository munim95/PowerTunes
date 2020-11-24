package com.rigid.powertunes;

import android.net.Uri;
import android.support.v4.media.session.PlaybackStateCompat;

public final class GlobalVariables {

    //TODO make all these redundant and shift to ipc codes (to reduce static use)
    public static final Uri DOCUMENTS_PRIMARY_URI= Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A/document/primary%3A");
    public static int currentPosition = 0;
    public static int lastPosition = -1; // redundant
    public static int viewpagerLastPos = -1;// redundant
    public static long currentSongId = -1;
    public static long[] idArray = null;// redundant
    public static boolean shouldBePaused = true;
    public static boolean firstTime = true; // first time for app load
    public static boolean firstTimeQueue = true;
    public static int currentFragment = -1;
    public static String currentPlayingFolder="";
    public static String currentSelectedFolder="";
    @PlaybackStateCompat.ShuffleMode
    public static int shuffleState=PlaybackStateCompat.SHUFFLE_MODE_NONE;
    @PlaybackStateCompat.RepeatMode
    public static int repeatState=PlaybackStateCompat.REPEAT_MODE_NONE;

    //*** DO NOT EDIT ***
    /**
     * USER APP DATA FILE NAMES
     * */
    public final static String FILENAMEPREF = "dirprefdata"; //DO NOT ALTER - restore ui for files dialog
    public final static String FILENAMESELECTION = "selectiondata"; //DO NOT ALTER - selected files

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREF_VERSION_CODE_KEY = "version_code";
    /**
     * Fragment Assigned Integers
     **/
    public final static int ALL_SONGS=0;
    public final static int LAST_ADDED=1;
    public final static int ARTISTS=2;
    public final static int ALBUMS=3;
    public final static int GENRES=4;
    public final static int FOLDERS=5;
    public final static int PLAYLISTS=6;
    /**
     * IPC codes b/w app components and main activity
     * */
    public final static int ON_ADAPTER_UPDATE=0;
    public final static int PLAY_UPDATE_CODE =100;
    public final static int PLAYBACK_COMPLETED_UPDATE_CODE=200;
    public final static int ID_ARRAY_UPDATE_CODE=300;
    public final static int SEEKBAR_SEEK_UPDATE_CODE=400;
    public final static int SEEKBAR_POSITION_UPDATE_CODE=500;
    public final static int PLAY_PAUSE_UPDATE_CODE=600;

    /**
     * final string values
     * */
    public final static String CURRENT_POSITION="currentposition";
    public final static String CURRENT_ID="currentid";
    public static final String BOTTOM_CARD_BLUR_SWITCH_KEY="disable_bottom_card_blur";
    public static final String BOTTOM_CARD_STATIC_SWITCH_KEY="disable_bottom_card_static";
}
