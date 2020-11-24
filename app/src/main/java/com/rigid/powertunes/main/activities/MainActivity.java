package com.rigid.powertunes.main.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.media.MediaBrowserServiceCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.rigid.powertunes.MetaDataChangeObserver;
import com.rigid.powertunes.bottomsheetfragments.AudioSheetFragment;
import com.rigid.powertunes.main.activities.settings.SettingsActivity;
import com.rigid.powertunes.main.fragments.QueueFragment;
import com.rigid.powertunes.dialogs.EditTagDialog;
import com.rigid.powertunes.main.fragments.LastAddedFragment;
import com.rigid.powertunes.mediaservice.MediaBrowserHelper;
import com.rigid.powertunes.mediaservice.MediaService;
import com.rigid.powertunes.R;
import com.rigid.powertunes.Seekbars.NowPlayingCardSeekBar;
import com.rigid.powertunes.bottomsheetbehaviours.ConfirmationBottomSheet;
import com.rigid.powertunes.bottomsheetbehaviours.MainBottomSheet;
import com.rigid.powertunes.bottomsheetbehaviours.SelectionOptionsListener;
import com.rigid.powertunes.bottomsheetfragments.MainFoldersFragment;
import com.rigid.powertunes.bottomsheetfragments.AddToPlaylistFragment;
import com.rigid.powertunes.main.fragments.AllSongsFragment;
import com.rigid.powertunes.main.fragments.SearchFragment;
import com.rigid.powertunes.mediaplayer.PlaybackModeInterface;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.misc.GlobalSelectionTracker;
import com.rigid.powertunes.misc.ResultReceiverCustom;
import com.rigid.powertunes.songmodels.Album;
import com.rigid.powertunes.songmodels.Artist;
import com.rigid.powertunes.songmodels.Folder;
import com.rigid.powertunes.songmodels.Genre;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.util.Utils;
import com.rigid.powertunes.viewmodels.SharedFragmentViewModel;
import com.rigid.powertunes.viewpagers.NowPlayingCardFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION;

public class MainActivity extends AppCompatActivity implements ResultReceiverCustom.Receiver {
    // TODO: 2019-05-30  refine first time startup screen
    // TODO: SYNC VIEWPAGER POSITION WITH SERVICE (USE SAME LIST?) + SCAN NEW SONGS SHOULD UPDATE EVERYTHING

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static boolean FIRST_LOAD = true;

    private long mBackPressedCurrTime;
    //shared among fragments
    private MediaControllerCompat mediaControllerCompat;
    private MetaDataChangeObserver metaDataChangeObserver;

    private MediaBrowserHelper mMediaBrowserHelper;
    private SharedFragmentViewModel sharedFragmentViewModel;

    private SelectionOptionsListener selectionOptionsListener;
    private BottomSheetBehavior
    bottomsheetBehaviour,
    bottomsheetConfirmationBehaviour,
    bottomsheetPlaylistBehaviour;

    //bottom card
    //    private TextView cardTitle;
    private SeekBar seekBar;
    private NowPlayingCardPagerAdapter mAdapter;
    private ViewPager2 viewPager;
    private byte[] reuseByteData;
    private ImageView blurryBg,nowPlayingCardBlurryBg;
    private RelativeLayout bottomsheetLayout;
    //    private BlurAsyncTasker blurAsyncTasker;
    private ArrayList<Song> _songs;
    private List<Song> queueSongs;

    private SharedPreferences sharedPreferences;
    private FrameLayout startUpLockView;
    private MainActivity.StoragePermissions storagePermissions;
    private boolean preventDoubleClick;
    private ResultReceiverCustom resultReceiverCustom=new ResultReceiverCustom(new Handler());
    private int currPosition=-1;
    private long currentId=-1;
    private Bundle idArraybundle;
    private boolean viewPagerScrollLock =false; //prevents on page changed call on bottom card
    private boolean fragChanged = false;
    private Bundle currentMediaBundle = new Bundle(); //contains position and metadata
    private int currentPlaylistFragment = -1;
    private boolean FIRST_CLICK = true;

    @PlaybackModeInterface.RepeatMode
    private int repeat = PlaybackModeInterface.REPEAT_MODE_NONE;

    @PlaybackModeInterface.ShuffleMode
    private int shuffle=PlaybackModeInterface.SHUFFLE_MODE_NONE;

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startUpLockView=findViewById(R.id.startUpLockView);
        startUpLockView.setVisibility(View.VISIBLE);
        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);

        storagePermissions = new StoragePermissions();
        if(storagePermissions.checkPermissions()){
            startUpLockView.setVisibility(View.GONE);
            initMediaBrowserService();
            handleSavedInstanceState(savedInstanceState);
        } else {
            startUpLockView.setVisibility(View.VISIBLE);
            storagePermissions.askPermissions();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ((TextView) startUpLockView.findViewById(R.id.startUpScanText)).setText("Loading Songs...");
                handleSavedInstanceState(null);
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ((TextView) startUpLockView.findViewById(R.id.startUpScanText)).setText(getResources().getString(R.string.permissions_denied));
                    startUpLockView.findViewById(R.id.startUpScanText).setOnClickListener((v) -> {
                        storagePermissions.askPermissions();
                    });
                }
                ////in the case where user denies permission and checks 'dont show again' too ...
                else {
                    ((TextView) startUpLockView.findViewById(R.id.startUpScanText)).setText(getResources().getString(R.string.permissions_open_settings));
                    //start device settings
                    startUpLockView.findViewById(R.id.startUpScanText).setOnClickListener((v) -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    });
                }
            }
        }
    }

    private void handleSavedInstanceState(Bundle savedInstanceState) {
        initStartUpUiStuff();
        if(savedInstanceState==null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.bottomsheet_frag_container,
                    new MainFoldersFragment(),"MainFoldersFragment").commit();
            ((TextView)findViewById(R.id.foldersMenuTxt)).setText(getResources().getString(R.string.app_name));
//            initDefaultFragment();
//            initEqAudio();
        }else{
            findViewById(R.id.nowPlayingCardLayout).setVisibility(savedInstanceState.getInt("cardvisibility"));
            shuffle=savedInstanceState.getInt("shufflestate");
            repeat=savedInstanceState.getInt("repeatstate");
            if (shuffle==PlaybackStateCompat.SHUFFLE_MODE_ALL){
                findViewById(R.id.shuffleImage).setActivated(true);
            } else {
                findViewById(R.id.shuffleImage).setActivated(false);
            }
            if(repeat==PlaybackStateCompat.REPEAT_MODE_NONE){
                findViewById(R.id.repeatFrameLayout).setActivated(false);
                findViewById(R.id.repeatOneImage).setVisibility(View.GONE);
                findViewById(R.id.repeatImage).setVisibility(View.VISIBLE);
            }else if(repeat==PlaybackStateCompat.REPEAT_MODE_ONE){
                findViewById(R.id.repeatFrameLayout).setActivated(true);
                findViewById(R.id.repeatImage).setVisibility(View.GONE);
                findViewById(R.id.repeatOneImage).setVisibility(View.VISIBLE);
            }else{
                findViewById(R.id.repeatFrameLayout).setActivated(true);
                findViewById(R.id.repeatOneImage).setVisibility(View.GONE);
                findViewById(R.id.repeatImage).setVisibility(View.VISIBLE);
            }
//            if(!sharedPreferences.getBoolean(GlobalVariables.BOTTOM_CARD_BLUR_SWITCH_KEY, false)) {
//                reuseByteData = savedInstanceState.getByteArray("imagedata");
//                blurAsyncTasker=new BlurAsyncTasker(this, nowPlayingCardBlurryBg, blurryBg);
//                blurAsyncTasker.execute(reuseByteData);
//            }
        }
    }
    //todo curr pos may not always be valid since the list could've changed.
    /**
     * sets current fragment from last saved state and launches now playing if clicked from notification
     * */
    private void initDefaultFragment() {
        if(sharedPreferences.getInt("currentfragment",-1)!=-1)
        {
//            currentId=sharedPreferences.getLong("currentid",-1);
//            currPosition=sharedPreferences.getInt("currentposition",-1);
//            currentMediaBundle.putInt("currentposition",currPosition);
            final FragmentTransaction transaction= getSupportFragmentManager().beginTransaction();
            Bundle bundle = new Bundle();

            //if opened from notification, launch now playing frag
            if(getIntent().getIntExtra("launchnowplaying",0)==1)
                bundle.putInt("launchnowplaying",1);
            //load last state
            switch(sharedPreferences.getInt("currentfragment",-1)) {
                case GlobalVariables.ALL_SONGS:
                    final AllSongsFragment allSongsFragment = new AllSongsFragment();
                    allSongsFragment.setArguments(bundle);
                    transaction.replace(R.id.mainFragContainer, allSongsFragment,allSongsFragment.getClass().getSimpleName())
                            .addToBackStack(null);
                    break;
                case GlobalVariables.LAST_ADDED:
                    final LastAddedFragment lastAddedFragment = new LastAddedFragment();
                    lastAddedFragment.setArguments(bundle);
                    transaction.replace(R.id.mainFragContainer, lastAddedFragment,lastAddedFragment.getClass().getSimpleName())
                            .addToBackStack(null);
                    break;
            }
            transaction.commit();
        } else {
            //load by default
            final AllSongsFragment allSongsFragment = new AllSongsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.mainFragContainer,
                    allSongsFragment,allSongsFragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commit();
        }
    }
    //to communicate to service and listen for media callbacks
    private void initMediaBrowserService() {
        mMediaBrowserHelper = new MediaBrowserConnection(this, MediaService.class);
        mMediaBrowserHelper.onStart();
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());
        //shared data frag model - shared across all ui components
        sharedFragmentViewModel= ViewModelProviders.of(this).get(SharedFragmentViewModel.class);
//        sharedFragmentViewModel.getCurrentSongsOrder().observe(this, onSongsChangedObserver());
        sharedFragmentViewModel.getCurrentPosition().observe(this,currentPositionObserver());
    }
    /**init equalizer logic on startup*/
    private void initEqAudio(){
        final AudioSheetFragment audioSheetFragment = new AudioSheetFragment();
        Bundle bundle= new Bundle();
        bundle.putBoolean("onstartup",true); //only init non ui elements for eq (frequency, on/off, preset)
        audioSheetFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(audioSheetFragment,"AudioSheetFragment").commit();
    }

    private void initStartUpUiStuff() {
        Utils.setSoftInputMode(this,WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentCallbacks(),false);
//        mAdapter = new NowPlayingCardPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.nowPlayingCardViewPager);
        mAdapter=new NowPlayingCardPagerAdapter(getSupportFragmentManager(),getLifecycle());
        viewPager.setAdapter(mAdapter);
        viewPager.registerOnPageChangeCallback(onPageChangeListener());
//        viewPager.setOffscreenPageLimit(1);
        seekBar = findViewById(R.id.customSeekbar);
//        cardTitle=findViewById(R.id.cardSongName);

        initSheetOptionsButtons(selectionOptionsListener());
        //bottom sheet
        bottomsheetLayout = findViewById(R.id.mainBottomsheetLayout);
        bottomsheetBehaviour= MainBottomSheet.from(bottomsheetLayout);
        ((MainBottomSheet) bottomsheetBehaviour).setContext(this);
        RelativeLayout layout = findViewById(R.id.delConfirmationBottomSheet);
        bottomsheetConfirmationBehaviour = ConfirmationBottomSheet.from(layout);
        RelativeLayout playlistLayout = findViewById(R.id.new_playlist_sheet);
        bottomsheetPlaylistBehaviour = ConfirmationBottomSheet.from(playlistLayout);

        //preferably one click listener would do for options in the same layout
        findViewById(R.id.repeatFrameLayout).setOnClickListener(repeatModeListener());
        findViewById(R.id.shuffleImage).setOnClickListener(shuffleModeListener());
        findViewById(R.id.search).setOnClickListener(searchClickListener());
        findViewById(R.id.queueImage).setOnClickListener(queueClickListener());

        //bottomsheet menu
        findViewById(R.id.settingsMenu).setOnClickListener(bottomSheetMenuListener());
        findViewById(R.id.foldersMenu).setOnClickListener(bottomSheetMenuListener());

        ((NowPlayingCardSeekBar)seekBar).setIndicator(findViewById(R.id.seekbarIndicator));
        ((NowPlayingCardSeekBar)seekBar).setNowPlayingCard(findViewById(R.id.nowPlayingCardLayout));
        ((NowPlayingCardSeekBar)seekBar).setTimerText(findViewById(R.id.cardElapsedTimeText));

        blurryBg=findViewById(R.id.foldersBlurryBackground);
        nowPlayingCardBlurryBg=findViewById(R.id.nowPlayingCardBlurryBg);

        resultReceiverCustom.setReceiver(this);
    }

    private ViewPager2.OnPageChangeCallback onPageChangeListener(){
        return new ViewPager2.OnPageChangeCallback() {
            int previousState = ViewPager.SCROLL_STATE_SETTLING;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                if(!viewPagerScrollLock) {
                    currentMediaBundle.putInt("currentposition", position);
                    currentMediaBundle.putBoolean("isViewPager",true);
                    corePlayHandle(_songs,currentMediaBundle);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                //distinguishes bw programmatic drag and user drag
                if (previousState == ViewPager.SCROLL_STATE_DRAGGING
                        && state == ViewPager.SCROLL_STATE_SETTLING)
                    viewPagerScrollLock = false;

                else if (previousState == ViewPager.SCROLL_STATE_SETTLING
                        && state == ViewPager.SCROLL_STATE_IDLE)
                    viewPagerScrollLock = true;

                previousState=state;
            }
        };
    }

    private ResultReceiver songsAdapterReceiver;
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        boolean isMediaBrowserValid = mMediaBrowserHelper!=null && mMediaBrowserHelper.getMediaController()!=null;
        switch(resultCode){
            case GlobalVariables.ON_ADAPTER_UPDATE: //on new songs adapter instance (new frag)
                songsAdapterReceiver = resultData!=null?resultData.getParcelable("adapterreceiver"):null;
                if(songsAdapterReceiver !=null) {
//                    currentMediaBundle.putInt("currentposition",currPosition);
                    currentMediaBundle.putLong("currentid",currentId);
                    songsAdapterReceiver.send(GlobalVariables.ON_ADAPTER_UPDATE,currentMediaBundle);
                }
                break;
            case GlobalVariables.PLAY_UPDATE_CODE:
                if(isMediaBrowserValid)
                    corePlayHandle(_songs,resultData);
//                viewPager.setCurrentItem(resultData.getInt("currentposition"));
                break;
//            case GlobalVariables.PLAYBACK_COMPLETED_UPDATE_CODE: //from service get next/previous/shuffle position depending on play mode
                //playback completed - get next pos and update viewmodel
//                currPosition=resultData.getInt("currentposition");
//                sharedFragmentViewModel.setCurrentPosition(currPosition);
//                viewPager.setCurrentItem(currPosition);
//                break;
//            case GlobalVariables.ID_ARRAY_UPDATE_CODE: // id array to service
//                if(mMediaBrowserHelper!=null&&mMediaBrowserHelper.getMediaController()!=null)
//                    mMediaBrowserHelper.getMediaController().sendCommand("idarray", resultData, null);
//                break;
            case GlobalVariables.SEEKBAR_SEEK_UPDATE_CODE: // seekbar progress update(nowplaying and card seekbar)
                if(isMediaBrowserValid)
                    mMediaBrowserHelper.getTransportControls().seekTo(resultData.getInt("seekbarprogress"));
                break;
            /* seekbar position update- called frequently every 100/1000ms (nowplaying and card seekbar)*/
            case GlobalVariables.SEEKBAR_POSITION_UPDATE_CODE:
                if(isMediaBrowserValid) {
                    ResultReceiver seekbarReceiver=resultData!=null?resultData.getParcelable("seekbarreceiver"):null;
                    if(seekbarReceiver!=null && mMediaBrowserHelper.getMediaController().getPlaybackState()!=null) {
                        Bundle bundle = new Bundle();
                        bundle.putLong("seekbarposition", mMediaBrowserHelper.getMediaController().getPlaybackState().getPosition());
                        seekbarReceiver.send(100, bundle);
                    }
                }
                break;
            case GlobalVariables.PLAY_PAUSE_UPDATE_CODE: //nowplaying frag/card frag play/pause button handle
                if(isMediaBrowserValid) {
                    if(resultData.getBoolean("playpauseclick")){
                        mMediaBrowserHelper.getTransportControls().play();
                    }else{
                        mMediaBrowserHelper.getTransportControls().pause();
                    }
                }
                break;
            case 1000:
                //update card
                if(!resultData.getBoolean("notifyqueue") &&
                        resultData.getInt("removeposition",-1)!=-1) {
                    queueSongs.remove(resultData.getInt("removeposition"));
                    mAdapter.notifyItemRemoved(resultData.getInt("removeposition"));
                }
                else if(resultData.getBoolean("notifyqueue")) {
                    if (resultData.getInt("from") < resultData.getInt("to")) {
                        for (int i = resultData.getInt("from"); i < resultData.getInt("to"); i++) {
                            Collections.swap(_songs, i, i + 1);
                        }
                    } else {
                        for (int i = resultData.getInt("from"); i > resultData.getInt("to"); i--) {
                            Collections.swap(_songs, i, i - 1);
                        }
                    }
                    queueSongs = _songs;
                    mAdapter.notifyItemMoved(resultData.getInt("from"),resultData.getInt("to"));
                    if(resultData.getBoolean("isplaying")) {
                        currPosition = resultData.getInt("to");
                        viewPager.setCurrentItem(currPosition, false);
                    }
                }
                break;
            case 10:
                //scans files again
                if(isMediaBrowserValid)
                    mMediaBrowserHelper.getMediaController().sendCommand("delete",null,null);
                break;
            case 120:
                //retrieve blurred bitmap
                Bitmap b = resultData.getParcelable("bitmap");
                if(b!=null){
                    RequestOptions requestOptions2 = new RequestOptions()
                            .transforms(new CenterCrop(), new RoundedCorners(getResources()
                                    .getDimensionPixelSize(R.dimen.round_edges_dimen)))
                            .skipMemoryCache(true);
                    Glide.with(this)
                            .load(b)
                            .apply(requestOptions2)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(nowPlayingCardBlurryBg);
                    Glide.with(this)
                            .load(b)
                            .apply(requestOptions2)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(blurryBg);
                }else {
                    Glide.with(this).clear(nowPlayingCardBlurryBg);
                    Glide.with(this).clear(blurryBg);
                }
                break;
            case 1500:
                if (songsAdapterReceiver!=null) {
                    currPosition=resultData.getInt("currentposition",-1);
                    currentId=resultData.getLong("currentid",-1);
                    if(GlobalVariables.currentFragment!=currentPlaylistFragment)
                        currentMediaBundle.putLong("currentid",currentId);
                    else {
                        currentMediaBundle.putInt("currentposition", currPosition);
                        currentMediaBundle.putLong("currentid",-1);
                    }
                    songsAdapterReceiver.send(GlobalVariables.ON_ADAPTER_UPDATE, currentMediaBundle);

                    Log.d(TAG,"okkk"+currPosition);
                    if(currPosition!=-1)
                        viewPager.setCurrentItem(currPosition);
                    else
                        viewPager.setCurrentItem(0);
//                    sendSongsListToService(_songs);
                }
                break;
        }
    }
    /**
     * Single main source for song play handling
     * Receives play requests from all recycler views and objects
     * */
    private void corePlayHandle(List<Song> _songs, Bundle bundle) {
        if(mMediaBrowserHelper.getMediaController()==null) {
            Log.e(TAG,"Core Play Handle: media controller is null!");
            return;
        }
        if(bundle==null) {
            Log.e(TAG,"Play Handle: Bundle data = null!!!");
            return;
        }
//        MediaMetadataCompat metadataCompat = bundle.getParcelable("songmetadata");
        if(!GlobalVariables.currentSelectedFolder.equals("")) {
            Toast.makeText(this,String.format("Removed playing queue '%s'",GlobalVariables.currentSelectedFolder),Toast.LENGTH_SHORT).show();
            GlobalVariables.currentPlayingFolder = "";
            GlobalVariables.currentSelectedFolder = "";
        }
        //if diff frag click then change current list
        if(currentPlaylistFragment != GlobalVariables.currentFragment &&
                !bundle.getBoolean("isViewPager",false)) {
            currentPlaylistFragment = GlobalVariables.currentFragment;
            currPosition=-1;
            onSongsChanged(_songs);
        }
        if (bundle.getInt("currentposition") != currPosition) {
//            currentId=Long.parseLong(metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            currPosition = bundle.getInt("currentposition");
            if (FIRST_CLICK) {
                mMediaBrowserHelper.getTransportControls().prepareFromMediaId(currentId + "", bundle);
                mMediaBrowserHelper.getTransportControls().play(); //hotfix - player has to start before it can respond
                mMediaBrowserHelper.getTransportControls().pause();
                FIRST_CLICK = false;
            } else {
                mMediaBrowserHelper.getTransportControls().stop();
                mMediaBrowserHelper.getTransportControls().prepareFromMediaId(currentId + "", bundle);
                mMediaBrowserHelper.getTransportControls().play();
            }
        } else {
            mMediaBrowserHelper.getTransportControls().play();
        }
//        resultReceiverCustom.send(1500,bundle);
    }
    /**
     * Current song list that's playing
     * Called ONCE if song is clicked for the first time in frag
     * Queue is also set here
     **/
    private void onSongsChanged(List<Song> songs) {
        fragChanged=true;
        _songs = (ArrayList<Song>) songs;
        sendSongsListToService(_songs);
        swapCardAdapterSongs(_songs);

//        if(_songs!=null)
//        if(currPosition!=-1)
//            viewPager.setCurrentItem(currPosition);
//        else
//            viewPager.setCurrentItem(0);
    }
//    private Observer<List<Song>> onSongsChangedObserver(){
//        return songs -> {
//            fragChanged=true;
//            Log.d(TAG,"onSongsChangedObserver");
//            _songs = (ArrayList<Song>) songs;
//            swapCardAdapterSongs(_songs);
//            if(currPosition!=-1)
//                viewPager.setCurrentItem(currPosition,false);
//            sendSongsListToService(_songs);
////            if(mMediaBrowserHelper!=null&&mMediaBrowserHelper.getMediaController()!=null)
////                mMediaBrowserHelper.getMediaController().sendCommand("idarray",
////                        sendSongsListToService(_songs), null);
////            if(mMediaBrowserHelper!=null&&mMediaBrowserHelper.getMediaController()!=null)
////                mMediaBrowserHelper.getMediaController().sendCommand("idarray",
////                        sendSongsListToService(songs), null);
////            resultReceiverCustom.send(GlobalVariables.ID_ARRAY_UPDATE_CODE,sendSongsListToService(songs));
//        };
//    }
    /**
     * Current song position observer
     * */
    private Observer<Integer> currentPositionObserver() {
        return integer -> {
            currPosition=integer;
//            if(fragChanged){
//                swapCardAdapterSongs(_songs,integer);
//                if(mMediaBrowserHelper!=null&&mMediaBrowserHelper.getMediaController()!=null)
//                    mMediaBrowserHelper.getMediaController().sendCommand("idarray",
//                            sendSongsListToService(_songs), null);
//                fragChanged=false;
//            }else
            viewPager.setCurrentItem(integer);
//            sharedPreferences.edit().putInt("currentposition",integer).apply();
        };
    }
    //change data-set when song list changes
    private void swapCardAdapterSongs(ArrayList<Song> songs){
//        viewPagerScrollLock = true;
        //BUG (OLD VIEWPAGER) : creates new instance of adapter to fix an issue where
        // notify data set changed does not work if you want to change the entire data set
        //it loads the next page instance and keeps it cached
        mAdapter.swapData(songs);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * queue controls the ORDER of play to SERVICE
     * */
    private void sendSongsListToService(List<Song> songs) {
        if(mMediaBrowserHelper.getMediaController()==null) {
            Log.e(TAG, "media controller dead. ");
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("songs",(ArrayList<Song>) songs);
        mMediaBrowserHelper.getMediaController().sendCommand("songs",bundle,resultReceiverCustom);
    }
    //todo view null on config change
//    private static class BlurAsyncTasker extends AsyncTask<Bitmap,Void,Bitmap> {
//        private WeakReference<Context> context;
//        private WeakReference<ImageView> view1,view2;
//        private Exception e;
//
//        private BlurAsyncTasker(Context context,ImageView view1, ImageView view2){
//            this.context = new WeakReference<>(context);
//            this.view1=new WeakReference<>(view1);
//            this.view2=new WeakReference<>(view2);
//        }
//        @Override
//        protected Bitmap doInBackground(Bitmap... bytes) {
//            Bitmap blurryBitmap=null;
//            if(bytes[0]!=null) {
////                BitmapFactory.Options options = new BitmapFactory.Options();
////                options.inSampleSize=4;
////                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes[0],
////                        0, bytes[0].length,options);
//                try {
//                    blurryBitmap = RenderScriptBlurBuilder.blur(context.get(), bytes[0]);
//                } catch (Exception e) {
//                    this.e = e;
//                }
//            }
//            return blurryBitmap;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            if(e!=null) {
//                e.printStackTrace();
//            }else {
//                RequestOptions requestOptions2 = new RequestOptions()
//                        .transforms(new CenterCrop(), new RoundedCorners(context.get().getResources()
//                                .getDimensionPixelSize(R.dimen.round_edges_dimen)))
//                        .skipMemoryCache(true);
//                Glide.with(view1.get())
//                        .load(bitmap)
//                        .apply(requestOptions2)
//                        .transition(DrawableTransitionOptions.withCrossFade())
//                        .into(view1.get());
//                Glide.with(view2.get())
//                        .load(bitmap)
//                        .apply(requestOptions2)
//                        .transition(DrawableTransitionOptions.withCrossFade())
//                        .into(view2.get());
//            }
//        }
//        @Override
//        protected void onCancelled(Bitmap bitmap) {
//            if(bitmap!=null && !bitmap.isRecycled()) {
//                Glide.with(view1.get()).clear(view1.get());
//                Glide.with(view2.get()).clear(view2.get());
//                bitmap.recycle();
//            }
//        }
//    }

    private boolean isMainFrag=false;
    //handle ui related tasks for fragments
    private FragmentManager.FragmentLifecycleCallbacks fragmentCallbacks(){
        return new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
                super.onFragmentCreated(fm, f, savedInstanceState);
                if (f.getTag() != null && f.getTag().equals("NowPlayingHostFragment")) {
                    ((MainBottomSheet) bottomsheetBehaviour).hideBottomSheetLayout(true);
                }
            }
            @Override
            public void onFragmentActivityCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
                super.onFragmentActivityCreated(fm, f, savedInstanceState);
                if(f.getTag()!=null) {
                    switch (f.getTag()) {
                        case "AllSongsFragment":
                            isMainFrag=true;
                            GlobalVariables.currentFragment=GlobalVariables.ALL_SONGS;
                            ((TextView)findViewById(R.id.currentSelectionText)).setText(R.string.all_songs);
                            ((TextView)findViewById(R.id.currentSelectionText))
                                    .setCompoundDrawablesWithIntrinsicBounds(getResources()
                                                    .getDrawable(R.drawable.ic_music_note_black_24dp,null),
                                            null,null,null);
                            ((AllSongsFragment)f).songDataViewModel.getSongs().observe(f,(songs -> {
                                _songs=(ArrayList<Song>) songs;
                                if(FIRST_LOAD) {
                                    onSongsChanged(_songs);
                                    FIRST_LOAD=false;
                                }
                            }));
                            break;
                        case "LastAddedFragment":
                            isMainFrag=true;
                            GlobalVariables.currentFragment=GlobalVariables.LAST_ADDED;
                            ((TextView)findViewById(R.id.currentSelectionText)).setText(R.string.last_added);
                            ((TextView)findViewById(R.id.currentSelectionText))
                                    .setCompoundDrawablesWithIntrinsicBounds(getResources()
                                                    .getDrawable(R.drawable.ic_add_24dp,null),
                                            null,null,null);
                            ((LastAddedFragment)f).songDataViewModel.getRecentlyAdded().observe(f,(songs -> {
                                _songs=(ArrayList<Song>) songs;
                                if(FIRST_LOAD) {
                                    onSongsChanged(_songs);
                                    FIRST_LOAD=false;
                                }
                            }));
                            break;
                        case "FoldersSongsFragment":
                            isMainFrag=true;
                            Object o = f.getArguments().getParcelable("folder");
                            if(o instanceof Artist){
                                GlobalVariables.currentFragment=GlobalVariables.ARTISTS;
                                ((TextView)findViewById(R.id.currentSelectionText)).setText(((Artist)o).artist);
                                ((TextView)findViewById(R.id.currentSelectionText))
                                        .setCompoundDrawablesWithIntrinsicBounds(getResources()
                                                .getDrawable(R.drawable.ic_mic_24dp,null),
                                                null,null,null);
                                _songs=(ArrayList<Song>) ((Artist) o).getChildList();
                            }else if(o instanceof Album){
                                GlobalVariables.currentFragment=GlobalVariables.ALBUMS;
                                ((TextView)findViewById(R.id.currentSelectionText)).setText(((Album)o).albumName);
                                ((TextView)findViewById(R.id.currentSelectionText))
                                        .setCompoundDrawablesWithIntrinsicBounds(getResources()
                                                .getDrawable(R.drawable.ic_album_black_24dp,null),
                                                null,null,null);
                                _songs=(ArrayList<Song>) ((Album) o).getChildList();
                            }else if(o instanceof Genre){
                                GlobalVariables.currentFragment=GlobalVariables.GENRES;
                                ((TextView)findViewById(R.id.currentSelectionText)).setText(((Genre)o).genreName);
                                ((TextView)findViewById(R.id.currentSelectionText))
                                        .setCompoundDrawablesWithIntrinsicBounds(getResources().
                                                getDrawable(R.drawable.ic_library_music_24dp,null),
                                                null,null,null);
                                _songs=(ArrayList<Song>) ((Genre) o).getChildList();
                            }else if(o instanceof Folder){
                                GlobalVariables.currentFragment=GlobalVariables.FOLDERS;
                                ((TextView)findViewById(R.id.currentSelectionText)).setText(((Folder)o).folderName);
                                ((TextView)findViewById(R.id.currentSelectionText))
                                        .setCompoundDrawablesWithIntrinsicBounds(getResources().
                                                getDrawable(R.drawable.ic_folder_24dp,null),
                                                null,null,null);
                                _songs=(ArrayList<Song>) ((Folder) o).getSongsInFolders();
                            }else if(o instanceof Playlist){
                                GlobalVariables.currentFragment=GlobalVariables.PLAYLISTS;
                                ((TextView)findViewById(R.id.currentSelectionText)).setText(((Playlist)o).name);
                                ((TextView)findViewById(R.id.currentSelectionText))
                                        .setCompoundDrawablesWithIntrinsicBounds(getResources().
                                                getDrawable(R.drawable.ic_list_24dp,null),
                                                null,null,null);
                                //todo playlist songs
                            }
                            break;
                        default:
                            mBackPressedCurrTime =0;
                    }
                    sharedPreferences.edit().putInt("currentfragment",GlobalVariables.currentFragment).apply();
                }
            }
            @Override
            public void onFragmentViewDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentViewDestroyed(fm, f);
                if (f.getTag() != null && f.getTag().equals("NowPlayingHostFragment")) {
                    ((MainBottomSheet) bottomsheetBehaviour).hideBottomSheetLayout(false);
                }
            }
        };
    }
    @Override
    public void onBackPressed() {
        //todo if now playing open go back to frag before exiting
        if(!GlobalSelectionTracker.getMySelection().hasSelection()) {
            if(bottomsheetBehaviour.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                if(isMainFrag) {
                    // # 2000 ms, desired time passed between two back presses.
                    if (mBackPressedCurrTime + 2000 > System.currentTimeMillis()) {
                        super.onBackPressed();
                        return;
                    }
                    mBackPressedCurrTime = System.currentTimeMillis();
                    Toast.makeText(getBaseContext(), "Press back again to exit.", Toast.LENGTH_SHORT).show();
                } else
                    super.onBackPressed();
            } else
                bottomsheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            GlobalSelectionTracker.getMySelection().clearSelection();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        preventDoubleClick=false;
//        if(mMediaBrowserHelper==null) {
//            if(storagePermissions.checkPermissions()){
//                initMediaBrowserService();
//            }
//        }
//        if(_songs==null||_songs.isEmpty()) {
//            startUpLockView.setVisibility(View.VISIBLE);
//            ((TextView) startUpLockView.findViewById(R.id.startUpScanText)).setText("No Songs. Please tap below to select folders with songs.");
//            startUpLockView.findViewById(R.id.startUpScanText).setOnClickListener((v) -> {
//                if (!preventDoubleClick) {
//                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//                    intent.putExtra("startchooser", "dialog");
//                    startActivity(intent);
//                    preventDoubleClick = true;
//                }
//            });
//        }
        adjustBottomSheetHeightOnConfigChange();
        if(sharedPreferences.getBoolean(GlobalVariables.BOTTOM_CARD_BLUR_SWITCH_KEY, false)){
            Glide.with(nowPlayingCardBlurryBg).clear(nowPlayingCardBlurryBg);
            Glide.with(blurryBg).clear(blurryBg);
            Glide.get(this).clearMemory();
            reuseByteData=null;
        }
        ((MainBottomSheet)bottomsheetBehaviour)
                .toggleStaticSheet(sharedPreferences.getBoolean(GlobalVariables.BOTTOM_CARD_STATIC_SWITCH_KEY, false));
    }

    private void adjustBottomSheetHeightOnConfigChange(){
        // Store dimensions of the screen in size
        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        ViewGroup.LayoutParams layoutParams = bottomsheetLayout.getLayoutParams();
        // Set the height of the bottomsheet proportional to 70% of the screen width
        if(display.getRotation()==Surface.ROTATION_90||
                display.getRotation()==Surface.ROTATION_270){
            layoutParams.height=(int)(size.y * 0.70);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaBrowserHelper.onStop();
//        if(blurAsyncTasker!=null) {
//            blurAsyncTasker.cancel(true);
//            blurAsyncTasker=null;
//        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("cardvisibility",findViewById(R.id.nowPlayingCardLayout).getVisibility());
        outState.putByteArray("imagedata",reuseByteData);
        outState.putInt("shufflestate",GlobalVariables.shuffleState);
        outState.putInt("repeatstate",GlobalVariables.repeatState);
    }

    /**
     * SHEET OPTIONS SETTER
     * */
    private void initSheetOptionsButtons(View.OnClickListener clickListener){
        findViewById(R.id.selection_options).findViewById(R.id.closeSelect).setOnClickListener(clickListener);
        findViewById(R.id.selection_options).findViewById(R.id.deleteSelect).setOnClickListener(clickListener);
        findViewById(R.id.selection_options).findViewById(R.id.addToPlaylistSelect).setOnClickListener(clickListener);
        findViewById(R.id.selection_options).findViewById(R.id.addToQueueSelect).setOnClickListener(clickListener);
        findViewById(R.id.selection_options).findViewById(R.id.shareSelect).setOnClickListener(clickListener);
        findViewById(R.id.selection_options).findViewById(R.id.editInforSelect).setOnClickListener(clickListener);
        findViewById(R.id.selection_options).findViewById(R.id.albumArtSelect).setOnClickListener(clickListener);

        findViewById(R.id.exp_par_selection_options).findViewById(R.id.closeSelect).setOnClickListener(clickListener);
        findViewById(R.id.exp_par_selection_options).findViewById(R.id.deleteSelect).setOnClickListener(clickListener);
        findViewById(R.id.exp_par_selection_options).findViewById(R.id.addToQueueSelect).setOnClickListener(clickListener);
        findViewById(R.id.exp_par_selection_options).findViewById(R.id.shareSelect).setOnClickListener(clickListener);
        findViewById(R.id.exp_par_selection_options).findViewById(R.id.editInforSelect).setOnClickListener(clickListener);
        findViewById(R.id.exp_par_selection_options).findViewById(R.id.albumArtSelect).setOnClickListener(clickListener);

        findViewById(R.id.bottomDialogDelete).setOnClickListener(clickListener);
        findViewById(R.id.bottomDialogRemove).setOnClickListener(clickListener);
    }

    private View.OnClickListener selectionOptionsListener(){
        return v -> {
            switch (v.getId()){
                case R.id.closeSelect:
                    selectionOptionsListener.closeSelect();
                    break;
                case R.id.deleteSelect:
                    ((ConfirmationBottomSheet)bottomsheetConfirmationBehaviour)
                            .showConfirmationDialog(false,false,(((GridLayout)(v.getParent())).getId()==R.id.exp_par_selection_options));
                    break;
                case R.id.bottomDialogDelete: //confirmation dialog delete btn
                    selectionOptionsListener.deleteSelect(false);
                    break;
                case R.id.bottomDialogRemove: //confirmation dialog remove btn (playlist)
                    selectionOptionsListener.deleteSelect(true);
                    break;
                case R.id.addToPlaylistSelect:
                    final AddToPlaylistFragment addToPlaylistFragment = new AddToPlaylistFragment();
                    getSupportFragmentManager().beginTransaction()
                                .replace(R.id.playlist_frag_container, addToPlaylistFragment,addToPlaylistFragment.getClass().getSimpleName())
                                .commit();
                    bottomsheetPlaylistBehaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                    selectionOptionsListener.addToPlaylistSelect();
                    break;
                case R.id.shareSelect:
                    selectionOptionsListener.shareSelect();
                    break;
                    //include all features including album art however only allowing image picking from gallery for now...
                case R.id.addToQueueSelect:
                    //features:
                    //1. option - keep list once finished or scrap
                    //2. option - keep last song from list or scrap as you progress
                    //3. option - delete queue once finished or keep
                    //4. option - song that is not in queue is clicked then delete the queue (ask first)

//                    addSongsToQueue();
                    selectionOptionsListener.addToQueueSelect();
                    break;
                case R.id.albumArtSelect:
                    Toast.makeText(MainActivity.this,"Under Development. Available In Next Update.", Toast.LENGTH_SHORT).show();
                    selectionOptionsListener.albumArtSelect();
                    break;
                case R.id.editInforSelect:
                    //todo only one allowed
                    Song _song=null;
                       for(Uri uri : GlobalSelectionTracker.getMySelection().getSelection()){
                           for(Song song : _songs){
                               if(song.data.equals(uri.getPath())) {
                                   _song = song;
                                   break;
                               }
                           }
                       }
                    EditTagDialog.createEditDialog(_song).show(getSupportFragmentManager(),"edittagdialog");
                    selectionOptionsListener.editInforSelect();
                    break;
            }
        };
    }

    //add songs to queue
    // TODO: 2019-06-01 save queue ids to disk
    private void addSongsToQueue(){
        if(queueSongs==null)
            queueSongs=new ArrayList<>();
        //used when queue left
        long lastIdBeforeQueueStarted = currentId;
        for(Uri uri : GlobalSelectionTracker.getMySelection().getSelection()) {
            for(Song song : _songs){
                if(song.data.equals(uri.getPath())) {
                    queueSongs.add(song);
                }
            }
        }
        GlobalSelectionTracker.getMySelection().clearSelection();
//        sharedFragmentViewModel.setCurrentSongsOrder(queueSongs);
        Toast.makeText(this,
                String.format("Added %s songs to queue.",GlobalSelectionTracker.getMySelection().getSelection().size()),
                Toast.LENGTH_SHORT)
                .show();
        //if queue is empty, start the queue, otherwise add to bottom of the list
        if(GlobalVariables.firstTimeQueue && !queueSongs.isEmpty()) {
//            currentMediaBundle.putLong("songid", queueSongs.get(0).id);
            currentMediaBundle.putInt("currentposition", 0);
            corePlayHandle(queueSongs,currentMediaBundle);
//            resultReceiverCustom.send(GlobalVariables.PLAY_UPDATE_CODE, currentMediaBundle);
            sharedFragmentViewModel.setCurrentPosition(0);
            Toast.makeText(this, "Started queue.", Toast.LENGTH_SHORT)
                    .show();
            GlobalVariables.firstTimeQueue=false;
        }
        //save to load ids when app is opened again
    }
    public void setSelectionOptionsListener(SelectionOptionsListener _selectListener){
        selectionOptionsListener=_selectListener;
    }

    /**
     * options in the bottom layout (sheet peak layout)
     **/
    private View.OnClickListener shuffleModeListener(){
        return v -> {
            switch(mMediaBrowserHelper.getMediaController().getShuffleMode()){
                case PlaybackStateCompat.SHUFFLE_MODE_NONE:
                    findViewById(R.id.shuffleImage).setActivated(true);
                    mMediaBrowserHelper.getMediaController().sendCommand("shuffleon",null,null);
                    Toast.makeText(getApplicationContext(),"shuffle all",Toast.LENGTH_SHORT).show();
                    GlobalVariables.shuffleState=PlaybackStateCompat.SHUFFLE_MODE_ALL;
                    break;
                case PlaybackStateCompat.SHUFFLE_MODE_ALL:
                    findViewById(R.id.shuffleImage).setActivated(false);
                    mMediaBrowserHelper.getMediaController().sendCommand("shuffleoff",null,null);
                    Toast.makeText(getApplicationContext(),"shuffle off",Toast.LENGTH_SHORT).show();
                    GlobalVariables.shuffleState=PlaybackStateCompat.SHUFFLE_MODE_NONE;
                    break;
            }
        };
    }
    private View.OnClickListener repeatModeListener(){
        return v -> {
            switch (mMediaBrowserHelper.getMediaController().getRepeatMode()){
                case PlaybackStateCompat.REPEAT_MODE_NONE:
                    findViewById(R.id.repeatFrameLayout).setActivated(true);
                    findViewById(R.id.repeatImage).setVisibility(View.GONE);
                    findViewById(R.id.repeatOneImage).setVisibility(View.VISIBLE);
                    //next mode
                    mMediaBrowserHelper.getMediaController().sendCommand("repeatone",null,null);
                    Toast.makeText(getApplicationContext(),"repeat one",Toast.LENGTH_SHORT).show();
                    break;
                case PlaybackStateCompat.REPEAT_MODE_ONE:
                    findViewById(R.id.repeatOneImage).setVisibility(View.GONE);
                    findViewById(R.id.repeatImage).setVisibility(View.VISIBLE);
                    //next mode
                    mMediaBrowserHelper.getMediaController().sendCommand("repeatloop",null,null);
                    Toast.makeText(getApplicationContext(),"repeat all",Toast.LENGTH_SHORT).show();
                    break;
                case PlaybackStateCompat.REPEAT_MODE_ALL:
                    findViewById(R.id.repeatFrameLayout).setActivated(false);
                    //advance to next in list
                    //next mode
                    mMediaBrowserHelper.getMediaController().sendCommand("repeatoff",null,null);
                    Toast.makeText(getApplicationContext(),"repeat off",Toast.LENGTH_SHORT).show();
                    break;
            }
        };
    }
    //search button
    private View.OnClickListener searchClickListener(){
        final SearchFragment searchFragment = new SearchFragment();
        return v -> {
            if(!searchFragment.isAdded()) {
                getSupportFragmentManager().beginTransaction().
                        add(R.id.mainFragContainer, searchFragment, searchFragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();
            }
        };
    }
    private View.OnClickListener queueClickListener(){
        final QueueFragment queueFragment = new QueueFragment();
        return v -> {
            if (!queueFragment.isAdded()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFragContainer, queueFragment, queueFragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();
                sharedFragmentViewModel.setCurrentSongsOrder(queueSongs);
            }
        };
    }

    /**
     * bottomsheet menu buttons
     **/
    private View.OnClickListener bottomSheetMenuListener(){
        return new View.OnClickListener(){
            final MainFoldersFragment mainFoldersFragment = new MainFoldersFragment();
            @Override
            public void onClick(View v) {
                final FragmentTransaction transaction= getSupportFragmentManager().beginTransaction();
                switch (v.getId()) {
                    case R.id.settingsMenu:
                        if(!preventDoubleClick) {
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            preventDoubleClick=true;
                        }
                        break;
                    case R.id.foldersMenu:
                        transaction
                                .replace(R.id.bottomsheet_frag_container, mainFoldersFragment,mainFoldersFragment.getClass().getSimpleName());
                        ((TextView)findViewById(R.id.foldersMenuTxt)).setText(getResources().getString(R.string.app_name));
                        break;
                }
                transaction.commit(); }
        };
    }

    public MediaControllerCompat getMediaControllerCompat(){
        return mediaControllerCompat;
    }

    private class MediaBrowserConnection extends MediaBrowserHelper {
        private MediaBrowserConnection(Context context, Class<? extends MediaBrowserServiceCompat> serviceClass) {
            super(context, serviceClass);
        }
        @Override
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
            //todo when this is called songs should never be null
            mediaControllerCompat=mediaController;
            initDefaultFragment();
            // send mainactivity receiver to service
            Bundle receiverBundle = new Bundle();
            receiverBundle.putParcelable("mainreceiver",resultReceiverCustom);
            mediaController.sendCommand("mainreceiver",receiverBundle,null);
//            mediaController.sendCommand("idarray", idArraybundle, resultReceiverCustom); // send id array for skip next/prev to service

            //to prevent null pointer exception set page listener here
//            viewPager.addOnPageChangeListener(onPageChangeListener());

            ((NowPlayingCardSeekBar)seekBar).setMediaControllerCompat(mediaController);
            ((NowPlayingCardSeekBar)seekBar).setInactive(true);
            ((NowPlayingCardSeekBar)seekBar).setActive();

            //restore last state
            GlobalVariables.shuffleState=sharedPreferences.getInt("shufflemode",0);
            GlobalVariables.repeatState=sharedPreferences.getInt("repeatmode",0);

            if(GlobalVariables.shuffleState==PlaybackStateCompat.SHUFFLE_MODE_NONE){
                mediaController.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                findViewById(R.id.shuffleImage).setActivated(false);
            }
            else{
                mediaController.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                findViewById(R.id.shuffleImage).setActivated(true);
            }
            if(GlobalVariables.repeatState==PlaybackStateCompat.REPEAT_MODE_NONE) {
                mediaController.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                findViewById(R.id.repeatFrameLayout).setActivated(false);
                findViewById(R.id.repeatOneImage).setVisibility(View.GONE);
                findViewById(R.id.repeatImage).setVisibility(View.VISIBLE);
            }
            else if(GlobalVariables.repeatState==PlaybackStateCompat.REPEAT_MODE_ONE) {
                mediaController.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                findViewById(R.id.repeatFrameLayout).setActivated(true);
                findViewById(R.id.repeatImage).setVisibility(View.GONE);
                findViewById(R.id.repeatOneImage).setVisibility(View.VISIBLE);
            }
            else {
                mediaController.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
                findViewById(R.id.repeatFrameLayout).setActivated(true);
                findViewById(R.id.repeatOneImage).setVisibility(View.GONE);
                findViewById(R.id.repeatImage).setVisibility(View.VISIBLE);
            }
        }
        //on songs load
        @Override
        protected void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            if(children.size()>0 && startUpLockView.getVisibility()==View.VISIBLE) {
                startUpLockView.setVisibility(View.GONE);
            }else{
                if(startUpLockView.getVisibility()==View.VISIBLE) {
                    ((TextView) startUpLockView.findViewById(R.id.startUpScanText)).setText("No Songs. Please tap below to select folders with songs.");
                    startUpLockView.findViewById(R.id.startUpScanText).setOnClickListener((v) -> {
                        if (!preventDoubleClick) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            intent.putExtra("startchooser", "dialog");
                            startActivity(intent);
                            preventDoubleClick = true;
                        }
                    });
                }
            }
        }
    }
//    public void setMetaDataChangeObserver(MetaDataChangeObserver metaDataChangeObserver){
//        this.metaDataChangeObserver = metaDataChangeObserver;
//    }
    private class MediaBrowserListener extends MediaControllerCompat.Callback {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            if(playbackState==null) {return;}
            sharedFragmentViewModel.setPlaybackState(playbackState);
        }
        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {return; }
            currentId=Long.parseLong(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
//            viewPagerScrollLock =true;
//            metaDataChangeObserver.onMetaDataChange(mediaMetadata);
//            sharedFragmentViewModel.setMediaMetaDataState(mediaMetadata);

            //set title of bottom card
//            cardTitle.setText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            ((NowPlayingCardSeekBar)seekBar).setMaxSongDuration((int)mediaMetadata.getLong(METADATA_KEY_DURATION));
            ((NowPlayingCardSeekBar)seekBar).setInactive(true);
            ((NowPlayingCardSeekBar)seekBar).setActive();

//            if (!sharedPreferences.getBoolean(GlobalVariables.BOTTOM_CARD_BLUR_SWITCH_KEY, false)) {
//                if (blurAsyncTasker != null && !blurAsyncTasker.isCancelled()) {
//                    blurAsyncTasker.cancel(true);
//                }
//                blurAsyncTasker = new BlurAsyncTasker(MainActivity.this, nowPlayingCardBlurryBg, blurryBg);
////                reuseByteData = MediaLibrary.getSongIdSparseArray().get(Long.parseLong(mediaMetadata.getDescription().getMediaId())).imageBytes;
//                blurAsyncTasker.execute(mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
//            }
            //position based on current frags list, not currently set IDs playlist in service
//            if(_songs!=null && _songs.size()!=0) {
//                currPosition=_songs.indexOf(MediaLibrary.getSongIdSparseArray().get(Long.parseLong(mediaMetadata.getDescription().getMediaId())));
//                sharedFragmentViewModel.setCurrentPosition(currPosition);
//                Log.d(TAG,currPosition +" <- curr");
//            }
//            viewPagerScrollLock =false;
        }
    }

    private ArrayList<Song> curr_songs;
    private class NowPlayingCardPagerAdapter extends FragmentStateAdapter {

//        private NowPlayingCardPagerAdapter(@NonNull FragmentManager fm) { super(fm); }

        public NowPlayingCardPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        void swapData(ArrayList<Song> songs){
            curr_songs=songs;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return NowPlayingCardFragment.newInstance(curr_songs.get(position));
        }

        @Override
        public long getItemId(int position) {
            return curr_songs.get(position)._id;
        }

        @Override
        public boolean containsItem(long itemId) {
            return super.containsItem(itemId);
        }

        @Override
        public int getItemCount() {
            return curr_songs!=null?curr_songs.size():0;
        }
    }

    //----- STRORAGE PERMISSIONS CLASS ------
    private class StoragePermissions {
        boolean checkPermissions() {
            return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)==0
                    && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==0;
        }
        void askPermissions() {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);

        }
    }

}
