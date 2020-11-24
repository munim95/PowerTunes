package com.rigid.powertunes.mediaservice;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import com.rigid.powertunes.main.activities.settings.SettingsActivity;
import com.rigid.powertunes.mediaplayer.MediaPlayerControlInterface;
import com.rigid.powertunes.mediaplayer.MediaPlayerMain;
import com.rigid.powertunes.mediaplayer.MediaPlayerInfoListener;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.provider.FetchSongFilesAsync;
import com.rigid.powertunes.provider.FetchSongsAsyncCallbackInterface;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.util.FileUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ServiceLifecycleDispatcher;
import androidx.media.MediaBrowserServiceCompat;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MediaService extends MediaBrowserServiceCompat implements FetchSongsAsyncCallbackInterface, LifecycleOwner {
    private static final String TAG = MediaService.class.getSimpleName();
    private final String DEFAULTROOTID = "root";
    private static final int PLAY_NEXT_TRACK = 0;
    private static final int RELEASE_RESOURCES = 10;
    private static final int PREPARE_MEDIA = 20;
    private static final int PAUSE_TRACK = 30;
    private static final int SEEK_TO = 40;
    private static final int ADD_TO_QUEUE = 50;
    private static final int UPDATE_SONGS = 100;

    private final ServiceLifecycleDispatcher mDispatcher = new ServiceLifecycleDispatcher(this);
    private MediaSessionCompat mediaSessionCompat;
    private MediaNotificationManager mMediaNotificationManager;
    private MediaPlayerControlInterface mediaPlayerControlInterface;
    private boolean mServiceInStartedState;
    private HeadsetConnReceiver headsetConnReceiver;
    private MediaMetadataCompat mPreparedSongMetaData;
//    private Song currSong;
    private long mediaId_=-1;
    private ArrayList<Long> shuffleIds = new ArrayList<>();
    private long[] idArray;
    private int currPos=-1;
    private ResultReceiver mainReceiver;
//    private List<MediaSessionCompat.QueueItem> currentQueue;
    private List<Song> currentQueue;
    private List<Song> songs;
    private SharedPreferences sharedPreferences;
    private int queuePos;
    private int firstPosition;
    private HandlerThread mediaPlayerHandler;
    private PlayerHandler playerHandler;
    private FetchSongFilesAsync fetchSongFilesAsync;

    @CallSuper
    @Override
    public void onCreate() {
        super.onCreate();
        mDispatcher.onServicePreSuperOnCreate();

        //fetch songs
        fetchSongFilesAsync=FetchSongFilesAsync.getInstance();
        fetchSongFilesAsync.setFetchSongsAsyncInterface(this); //onSongsLoad()
        if(!FileUtil.readIt(this,GlobalVariables.FILENAMESELECTION,false)) {
            //first time open settings and open folder selection
            Intent intent = new Intent(this,SettingsActivity.class);
            intent.putExtra("startchooser","dialog");
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //media session
        mediaSessionCompat=new MediaSessionCompat(this, TAG);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        mediaSessionCompat.setCallback(new MediaSessionCallback());
        setSessionToken(mediaSessionCompat.getSessionToken());

        //nedia player interface
        mediaPlayerControlInterface = new MediaPlayerMain(this,
                                                    new MediaPlayerListener(),
                                                    new PlaybackStateCompat.Builder());
        // media notification manager
        mMediaNotificationManager=new MediaNotificationManager(this);

        //todo add bluetooth headset filters
        //todo add telephony manager incoming call filter
        headsetConnReceiver=new HeadsetConnReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        registerReceiver(headsetConnReceiver,filter);

        //mediaplayer handler and new thread
        mediaPlayerHandler = new HandlerThread("PlayerHandler");
        mediaPlayerHandler.start();
        playerHandler = new PlayerHandler(this, mediaPlayerHandler.getLooper());

    }

    /** FetchSongsInterface*/
    //notify onLoadChildren when songs loaded
    @Override
    public void onSongsLoaded(ArrayList<Song> songs) {
        notifyChildrenChanged(DEFAULTROOTID);
//        mediaSessionCompat.getController().getTransportControls()
//                .prepareFromMediaId(""+PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong("currentid",-1),
//                null);
//        mediaSessionCompat.getController().getTransportControls().play();
    }

    @CallSuper
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDispatcher.onServicePreSuperOnStart();
        return START_NOT_STICKY;
    }
    @CallSuper
    @Override
    public void onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy();
        mMediaNotificationManager.onDestroy();
        releaseResources();

    }

    private void releaseResources(){
        mediaPlayerControlInterface.stop();
        mediaPlayerHandler.quit();
        mediaPlayerControlInterface.release();
        mediaSessionCompat.release();
        unregisterReceiver(headsetConnReceiver);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(DEFAULTROOTID,null);
    }

    @Override
    public void onLoadItem(String itemId, @NonNull Result<MediaBrowserCompat.MediaItem> result) {
        super.onLoadItem(itemId, result);

    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(MediaLibrary.getMediaBrowserItems());
    }
//    public Song getCurrentSong(){
//        return MediaLibrary.getSongIdSparseArray().get(mediaId_);
//    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mDispatcher.getLifecycle();
    }

//    private int skipRange;
    class MediaSessionCallback extends MediaSessionCompat.Callback{
        //todo move skip and add queue methods to PlayerHandler
        private final int ADAPTERSYNCCODE=10;
        private boolean shouldBePaused;
        private Message msg;
        private int lastPosition;

        MediaSessionCallback() {
            super();
        }
        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            //playback modes
            switch(command){
//                case "shuffleoff":
//                    onSetShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
//                    GlobalVariables.shuffleState=PlaybackStateCompat.SHUFFLE_MODE_NONE;
//                    sharedPreferences.edit().putInt("shufflemode",PlaybackStateCompat.SHUFFLE_MODE_NONE).apply();
//                    break;
//                case "shuffleon":
//                    onSetShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
//                    GlobalVariables.shuffleState=PlaybackStateCompat.SHUFFLE_MODE_ALL;
//                    sharedPreferences.edit().putInt("shufflemode",PlaybackStateCompat.SHUFFLE_MODE_ALL).apply();
//                    break;
//                case "repeatoff":
//                    onSetRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
//                    GlobalVariables.repeatState=PlaybackStateCompat.REPEAT_MODE_NONE;
//                    sharedPreferences.edit().putInt("repeatmode",PlaybackStateCompat.REPEAT_MODE_NONE).apply();
//                    break;
//                case "repeatone":
//                    onSetRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
//                    GlobalVariables.repeatState=PlaybackStateCompat.REPEAT_MODE_ONE;
//                    sharedPreferences.edit().putInt("repeatmode",PlaybackStateCompat.REPEAT_MODE_ONE).apply();
//                    break;
//                case "repeatloop":
//                    onSetRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
//                    GlobalVariables.repeatState=PlaybackStateCompat.REPEAT_MODE_ALL;
//                    sharedPreferences.edit().putInt("repeatmode",PlaybackStateCompat.REPEAT_MODE_ALL).apply();
//                    break;
                case"adaptersync":
                    //every time adapter swaps songs(resets values) 'remind' it the current song id
                    if(mediaId_!=-1) {
                        Bundle bundle=new Bundle();
                        bundle.putLong("currentid",mediaId_);
                        cb.send(ADAPTERSYNCCODE,bundle);
                    }
                    break;
                case "updatedeleted":
                    //update deleted position if playing
                    updateDeletedPosition(extras.getIntArray("deletedpositions"));
                    break;
//                case "idarray":
//                    if(extras!=null) {
//                        idArray = extras.getLongArray("idarray");
////                        int i =-1;
////                        for (long l : idArray) {
////                            i=i+1;
////                            if (l == mediaId_) {
////                                break;
////                            }
////                        }
////                        currPos=i;
////                        Log.d(TAG,"idd "+currPos);
//                    }
//                    break;
//                case "currentposition":
//                    break;
                case "mainreceiver":
                    mainReceiver =extras.getParcelable("mainreceiver");
                    break;
                case "delete":
//                    fetchSongFilesAsync.execute(MediaService.this,null,fetchSongFilesAsync.set,false);
                    break;
                case "songs":
                    if(currentQueue!=null)
                        currentQueue.clear();
                    //get whole ordered songs list from MainActivity
                    msg = new Message();
                    msg.what = UPDATE_SONGS;
                    msg.setData(extras);
                    playerHandler.sendMessage(msg);
            }
        }
        @Override
        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            //bundle containing metadata
            //CANNOT BE NULL!!
            if(extras==null) {
                Log.e(TAG, "Prepare Song: Bundle = null !!!");
                return;
            }
            currPos=extras.getInt("currentposition");
//            checkAndSetCurrentQueue();

//            mediaId_=Long.parseLong(mediaId);
//            prefEditor.putLong("currentid",mediaId_).apply();
//            queuePos=currPos;
//            if(currPos>=2){
//                queuePos=2;
//            }
            msg=new Message();
            msg.what=PREPARE_MEDIA;
//            msg.arg1=(int)mediaId_;
//            msg.setData(extras);
            playerHandler.sendMessage(msg);
        }


        @Override
        public void onPlay() {
            msg = new Message();
            msg.what = PLAY_NEXT_TRACK;
            playerHandler.sendMessage(msg);
        }

        @Override
        public void onPause() {
            playerHandler.sendEmptyMessage(PAUSE_TRACK);
        }

        @Override
        public void onStop() {
            playerHandler.removeMessages(PREPARE_MEDIA);
            playerHandler.removeMessages(PLAY_NEXT_TRACK);
            msg = new Message();
            msg.what=RELEASE_RESOURCES;
            playerHandler.sendMessage(msg);
        }

        @Override
        public void onSeekTo(long pos) {
//            msg = new Message();
//            msg.arg1=(int)pos;
//            playerHandler.sendEmptyMessage(SEEK_TO);
            mediaPlayerControlInterface.seekTo((int)pos);
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            GlobalVariables.repeatState=repeatMode;
            sharedPreferences.edit().putInt("repeatmode",repeatMode).apply();
            mediaSessionCompat.setRepeatMode(repeatMode);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            GlobalVariables.shuffleState=shuffleMode;
            sharedPreferences.edit().putInt("shufflemode",shuffleMode).apply();
            mediaSessionCompat.setShuffleMode(shuffleMode);

            if(shuffleMode==PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                if(idArray!=null) {
                    for (long i : idArray) {
                        shuffleIds.add(i);
                    }
                    lastPosition = currPos;
                    Collections.shuffle(shuffleIds);
                    GlobalVariables.shouldBePaused = false;
                    mediaSessionCompat.getController().getTransportControls().stop();
                    mediaSessionCompat.getController().getTransportControls()
                            .prepareFromMediaId(shuffleIds.get(0) + "", null);
                    mediaSessionCompat.getController().getTransportControls().play();
                }
            }else
                currPos=lastPosition!=0?lastPosition-1:lastPosition;
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
            if(action.equals("duration")){
                mediaPlayerControlInterface.fetchCurrentDuration();
            }
        }

        @Override
        public void onSkipToNext() {
            //send position over to main activity
            if (queuePos != currentQueue.size()-1) {
                ++currPos;
                Bundle bundle = new Bundle();
//                bundle.putParcelable("songmetadata",currentQueue.get(queuePos));
                bundle.putInt("currentposition",currPos);
                onStop();
                onPrepareFromMediaId("", bundle);
                onPlay();

//                mainReceiver.send(1500,bundle);
//                mediaPlayerControlInterface.playFromMetaData(mPreparedSongMetaData, false);
            }
            //update views
//            nextPositionBundle.putInt("currentposition",currPos);
//            mainReceiver.send(GlobalVariables.PLAYBACK_COMPLETED_UPDATE_CODE,nextPositionBundle);
        }

        @Override
        public void onSkipToPrevious() {
            if (queuePos!=0) {
                --currPos;
                Bundle bundle = new Bundle();
                bundle.putParcelable("songmetadata",currentQueue.get(queuePos));
                bundle.putInt("currentposition",currPos);
                onStop();
                onPrepareFromMediaId("", bundle);
                onPlay();

                mainReceiver.send(1500,bundle);
//                    mediaPlayerControlInterface.playFromMetaData(mPreparedSongMetaData,false);
            }
//                else if(currPos== 0){
//                    Toast.makeText(getApplicationContext(),"Start of list",Toast.LENGTH_SHORT).show();
//                }
            //update views
//            nextPositionBundle.putInt("currentposition",currPos);
//            mainReceiver.send(GlobalVariables.PLAYBACK_COMPLETED_UPDATE_CODE,nextPositionBundle);
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            super.onAddQueueItem(description);
            //set queue title for different frags and add/remove/set null accordingly

        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description, int index) {
//            if(currentQueue==null)
//                currentQueue=new ArrayList<>();
//            currentQueue.add(index,new MediaSessionCompat.QueueItem(description,Long.parseLong(description.getMediaId())));
//            mediaSessionCompat.setQueue(currentQueue);
            //create new queue if doesnt exist already
            //add to queue every time 'queue' option is selected
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            super.onRemoveQueueItem(description);
            //remove from queue as it progresses
//            if(currentQueue!=null) {
//                currentQueue.remove(new MediaSessionCompat.QueueItem(description, Long.parseLong(description.getMediaId())));
//                mediaSessionCompat.setQueue(currentQueue);
//            }
        }

//        //CHECK THIS
        private void updateDeletedPosition(int[] deletedpositions){
            if(deletedpositions!=null) {
                int deletedpos = -1;
                for (int i : deletedpositions) {
                    if (i == currPos) {
                        //deleted song was playing
                        deletedpos = i;
                        break;
                    }
                }
                if (deletedpos != -1) {
                    onSkipToNext();
                }
            }

        }
    }
    private void updateSongs(Bundle extras) {
        if(extras!=null)
            songs = extras.getParcelableArrayList("songs");
        if(currentQueue!=null)
            currentQueue.clear();
    }
     /**
                                    WINDOW PANE ALGORITHM FOR QUEUE
        (e.g for max 5 songs, only add 2 songs above and below current song, to improve efficiency)
        (Handled asynchronously)
               [ (0) | 1 | 2 ]     (s==3),
             [ 0 | (1) | 2 | 3 ]   (s==4),
           [ 0 | 1 | (2) | 3 | 4 ] (s==5)...

          (i == 0) = Math.max(0,curr - 2);
          (i == 1) = Math.max(0,curr - 1);
          (i == 2) = curr;
          (i == 3) = curr + 1;
          (i == 4) = curr + 2;
        */
    private void addToQueueUsingWindowPane() {
        final int MAX = Math.min(songs.size(),11);
        final boolean isOdd = MAX%2!=0; //equal items on each side if odd
        final int nItemsRequiredOnEachSide = isOdd?MAX/2:(MAX-1)/2;
//        skipRange = isOdd?nItemsRequiredOnEachSide:MAX/2;
        for(int i = 0; i<MAX; i++) {
            int i1 = i;
            int size = MAX;
            if(currPos<nItemsRequiredOnEachSide){
                //to make sure we don't have duplicates or loop over the same object again
                //N.B Even though a HashSet does the job of not allowing duplicates,
                //our aim is to reduce overhead for maximum efficiency thus we use ArrayList instead
                int itemsNeededOnTheLeft = nItemsRequiredOnEachSide - currPos; //items needed on LHS of curr pos to match itemsrequired
                if(i<itemsNeededOnTheLeft) //skip until itemsNeededOnTheLeft = 0
                    continue;

                size = MAX - itemsNeededOnTheLeft; //new total size depending on items needed
                i1 = i-itemsNeededOnTheLeft; //new i so list index always starts with 0 (i==itemsNeededOnTheLeft)
            }
            int range = 0; // always keeps n items before and after the current item
            if(i < nItemsRequiredOnEachSide || i > nItemsRequiredOnEachSide) {
                range = i - nItemsRequiredOnEachSide;
            }
            if(range==0) { // current item position
                queuePos = i1;
                firstPosition = Math.max(0,currPos-nItemsRequiredOnEachSide); //real position of first song in list
            }

            if(currentQueue.size()!=size) { //add all in empty list
                currentQueue.add(i1,songs.get(Math.max(0,Math.min(songs.size()-1,currPos+range))));
            }
            else { //replace in full list
                currentQueue.set(i1,songs.get(Math.max(0,Math.min(songs.size()-1,currPos+range))));
            }
            if(currPos+range==songs.size()-1)
                break;
        }
    }
    //checks if queue needs to be updated if out of range or queue position needs to change otherwise
    private void checkAndSetCurrentQueue() {
        if(songs!=null) {
            if (currentQueue == null) {
                currentQueue = new ArrayList<>();
                firstPosition = currPos;
            }
            boolean isAtStartOrEndOfQueue = currentQueue.isEmpty() ||
                    currPos == firstPosition + (currentQueue.size()-1) ||
                    currPos == Math.max(0, firstPosition);
            boolean isOutOfRange = (currPos > firstPosition + (currentQueue.size()-1) ||
                    currPos < Math.max(0, firstPosition));

            //generate new queue
            if(isAtStartOrEndOfQueue||isOutOfRange) {
                currentQueue.clear();
                addToQueueUsingWindowPane();
//                playerHandler.sendEmptyMessage(ADD_TO_QUEUE);
            } else {
                // if in range then sync queue position
//                    int first = currPos - Math.abs(currPos - firstPosition);
                queuePos = Math.max(0, (currPos - firstPosition));
            }
        }
    }
//    private void seekTo(int pos){
//        mediaPlayerControlInterface.seekTo(pos);
//    }
    private void playNextSong(MediaMetadataCompat mediaMetadataCompat, boolean shouldBePaused) {
        if(mediaMetadataCompat==null) return;
        mediaPlayerControlInterface.play();
    }
    private void sendDataToMainActivity(int currPos, long mediaId_) {
        Bundle b = new Bundle();
        b.putInt("currentposition",currPos);
        b.putLong("currentid",mediaId_);
        mainReceiver.send(1500,b);
    }
    //called every time on song change
    private void prepareSongMetaData() {
        Log.d(TAG,"prepare: queue pos "+ queuePos);
        checkAndSetCurrentQueue();
        mPreparedSongMetaData = currentQueue.get(queuePos).metadataCompat;
        mediaId_=Long.parseLong(mPreparedSongMetaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        mediaPlayerControlInterface.loadMedia(Uri.parse(mPreparedSongMetaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)),
                mediaId_);
        sharedPreferences.edit().putLong("currentid",mediaId_).apply();
        mediaSessionCompat.setMetadata(mPreparedSongMetaData);
        sendDataToMainActivity(currPos,mediaId_);
//        if(!sharedPreferences.getBoolean(GlobalVariables.BOTTOM_CARD_BLUR_SWITCH_KEY, false)) {
//            Bitmap blurry =
//                    RenderScriptBlurBuilder.blur(this,mPreparedSongMetaData.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
//            Bundle b = new Bundle();
//            b.putParcelable("bitmap", blurry);
//            mainReceiver.send(120, b);
//        }
        if (!mediaSessionCompat.isActive()) {
            mediaSessionCompat.setActive(true);
        }
    }

    /**
     * Player Handler
     **/
    private static class PlayerHandler extends Handler{
        @NonNull
        private final WeakReference<MediaService> mService;

        private PlayerHandler(final MediaService service, @NonNull final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }
        @Override
        public void handleMessage(Message msg) {
            final MediaService service = mService.get();
            if (service == null) return;
            switch (msg.what) {
                case UPDATE_SONGS:
                    service.updateSongs(msg.getData());
                    break;
                case PREPARE_MEDIA:
                    service.prepareSongMetaData();
                    break;
                case PLAY_NEXT_TRACK:
                    service.playNextSong(service.mPreparedSongMetaData,msg.arg1!=0);
                    break;
                case PAUSE_TRACK:
                    service.mediaPlayerControlInterface.pause();
                    break;
                case SEEK_TO:
                    service.mediaPlayerControlInterface.seekTo(msg.arg1);
                    break;
                case RELEASE_RESOURCES: //stop
                    service.mediaPlayerControlInterface.release();
                    service.mediaSessionCompat.setActive(false);
                    break;
//                case ADD_TO_QUEUE:
//                    service.addToQueueUsingWindowPane();
//                    break;
            }
        }
    }

    // MediaPlayerControlInterface Callback: MediaPlayerControlInterface state -> MediaService.
    private class MediaPlayerListener extends MediaPlayerInfoListener {
        private final ServiceManager mServiceManager;
        private int i = 0;

        private MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @SuppressLint("SwitchIntDef")
        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // Report the state to the MediaSession.
            mediaSessionCompat.setPlaybackState(state);
            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mServiceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mServiceManager.moveServiceOutOfStartedState();
                    break;
            }
        }
        @Override
        public void onPlaybackCompleted() {
            super.onPlaybackCompleted();
            //handle playback modes on song completion
            if(mediaSessionCompat.getController().getShuffleMode()==PlaybackStateCompat.SHUFFLE_MODE_NONE
                    || (mediaSessionCompat.getController().getShuffleMode()==PlaybackStateCompat.SHUFFLE_MODE_ALL
                    &&mediaSessionCompat.getController().getRepeatMode()!=PlaybackStateCompat.REPEAT_MODE_NONE)){

//                if(mediaSessionCompat.getController().getRepeatMode()==PlaybackStateCompat.REPEAT_MODE_GROUP) {
//                    //jump to next in list
//                    mediaSessionCompat.getController().getTransportControls().skipToNext();
//                }else
                if(mediaSessionCompat.getController().getRepeatMode()==PlaybackStateCompat.REPEAT_MODE_NONE){
                    //stop on last
                    if(mediaId_!=Long.parseLong(songs
                            .get(songs.size()-1).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))) {
                        mediaSessionCompat.getController().getTransportControls().skipToNext();
                    }else{
                        Toast.makeText(getApplicationContext(),"End of list",Toast.LENGTH_SHORT).show();
                    }
                }else if(mediaSessionCompat.getController().getRepeatMode()==PlaybackStateCompat.REPEAT_MODE_ONE){
                    //loop current song
                    mediaSessionCompat.getController().getTransportControls().pause();
                    mediaSessionCompat.getController().getTransportControls().play();

                }else if(mediaSessionCompat.getController().getRepeatMode()==PlaybackStateCompat.REPEAT_MODE_ALL){
                    //start again from start of list
                    if(mediaId_==Long.parseLong(currentQueue
                            .get(currentQueue.size()-1).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))){
//                        currentPlaybackBundle.putInt("currentposition",0);
//                        GlobalVariables.currentPosition=0;
//                        GlobalVariables.shouldBePaused=false;
                        Bundle bundle = new Bundle();
//                        bundle.putParcelable("songmetadata",currentQueue.get(0));
                        bundle.putInt("currentposition",0);
                        mediaSessionCompat.getController().getTransportControls().stop();
                        mediaSessionCompat.getController().getTransportControls()
                                .prepareFromMediaId(currentQueue.get(0).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID), bundle);
                        mediaSessionCompat.getController().getTransportControls().play();
//                        Log.d(LOG_TAG,"all "+MainActivity.currentPosition);
                    }else {
                        mediaSessionCompat.getController().getTransportControls().skipToNext();
                    }
                }
            }else if(mediaSessionCompat.getController().getShuffleMode()==PlaybackStateCompat.SHUFFLE_MODE_ALL
                    && mediaSessionCompat.getController().getRepeatMode()==PlaybackStateCompat.REPEAT_MODE_NONE){
                if(shuffleIds!=null && shuffleIds.size()>0) {
                    ++i;
                    if (i > shuffleIds.size() - 1) i = 0;

//                GlobalVariables.currentPosition=i;
                    GlobalVariables.shouldBePaused = false;
                    mediaSessionCompat.getController().getTransportControls().stop();
                    mediaSessionCompat.getController().getTransportControls()
                            .prepareFromMediaId(shuffleIds.get(i) + "", null);
                    mediaSessionCompat.getController().getTransportControls().play();
//                Log.d(LOG_TAG,"shuffle");
                }
            }
        }
    }
    /**
     * Service manager (handles notification display)
     * */
    private class ServiceManager {
        private void moveServiceToStartedState(PlaybackStateCompat state) {
            if (!mServiceInStartedState) {
                ContextCompat.startForegroundService(
                        MediaService.this,
                        new Intent(MediaService.this, MediaService.class));
                mServiceInStartedState = true;
                startForeground(MediaNotificationManager.NOTIFICATION_ID,
                        mMediaNotificationManager.getNotification(
                                mediaSessionCompat.getController().getMetadata(), state, getSessionToken()));
            }
        }
        private void updateNotificationForPause(PlaybackStateCompat state) {
            if (!mServiceInStartedState) {
                ContextCompat.startForegroundService(
                        MediaService.this,
                        new Intent(MediaService.this, MediaService.class));
                mServiceInStartedState = true;
            }
            mMediaNotificationManager.getNotificationManager()
                    .notify(MediaNotificationManager.NOTIFICATION_ID,
                            mMediaNotificationManager.getNotification(
                                    mediaSessionCompat.getController().getMetadata(), state, getSessionToken()));
            stopForeground(false);
        }
        private void moveServiceOutOfStartedState() {
            stopForeground(true);
            stopSelf();
            mServiceInStartedState = false;
        }
    }

    //todo ADD TOGGLE OPTION IN SETTINGS on headphone connection continue playing from last song
    public class HeadsetConnReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    if (mediaPlayerControlInterface.isPlaying()) {
                        mediaSessionCompat.getController().getTransportControls().pause();
                        Toast.makeText(context, "Headphones Disconnected", Toast.LENGTH_SHORT).show();
                    }
                case AudioManager.ACTION_HEADSET_PLUG:
                    if(intent.getIntExtra("state", 0)==1){
                        Toast.makeText(context, "Headphones Connected", Toast.LENGTH_SHORT).show();
                        if(mPreparedSongMetaData !=null&&!mediaPlayerControlInterface.isPlaying())
                            mediaSessionCompat.getController().getTransportControls().play();
                    }
            }

        }
    }

}
