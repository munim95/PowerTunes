package com.rigid.powertunes.viewpagers;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.R;
import com.rigid.powertunes.Seekbars.NowPlayingSeekbar;
import com.rigid.powertunes.helper.Helpers;
import com.rigid.powertunes.misc.GlideViewTargetSub;
import com.rigid.powertunes.misc.ResultReceiverCustom;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.viewmodels.SharedFragmentViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;
import androidx.viewpager.widget.ViewPager;

public class NowPlayingHostFragment extends Fragment {
    /**
     * Contains Seekbar and Playback controls
     * */
    //todo CAUTION: media controller could be null, although chances are slim

    private final String TAG = this.getClass().getSimpleName();
    private ViewPager viewPager;
    private NowPlayingPagerAdapter mAdapter;
    private List<Song> _songs;
    private ImageView nowPlayingPlayBtn,nowPlayingPauseBtn,foregroundArt,shuffleImage;
    private FrameLayout repeatHolder;
    private TextView nowPlayingElapsedText,nowPlayingDurationText;
    private SeekBar seekBar;
//    private View disableTouch;
    private SharedFragmentViewModel sharedFragmentViewModel;
    private ResultReceiverCustom resultReceiverCustom=new ResultReceiverCustom(new Handler());
    private boolean isFirstLoad=false;
    private int pos=-1;
    private PlaybackStateCompat playbackStateCompat;
    private Bundle bundle = new Bundle();
    private MediaControllerCompat mediaControllerCompat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Transition transition1 = TransitionInflater.from(getContext()).inflateTransition(R.transition.nowplaying_enter_transition);
        transition1.setDuration(200);
        setEnterTransition(transition1);
        pos=getArguments().getInt("currentposition");
        sharedFragmentViewModel = ViewModelProviders.of(getActivity()).get(SharedFragmentViewModel.class);
        _songs=new ArrayList<>();
        resultReceiverCustom.setReceiver((MainActivity)getActivity());
        mediaControllerCompat=((MainActivity)getActivity()).getMediaControllerCompat();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pager,container,false);
        foregroundArt=v.findViewById(R.id.foregroundAlbumArt);
        seekBar=v.findViewById(R.id.nowPlayingSeekBar);
        nowPlayingElapsedText=v.findViewById(R.id.nowPlayingElapsedTime);
        nowPlayingDurationText=v.findViewById(R.id.nowplayingDuration);
        nowPlayingPlayBtn=v.findViewById(R.id.nowPlayingPlayBtn);
        nowPlayingPauseBtn=v.findViewById(R.id.nowPlayingPauseBtn);
//        disableTouch=v.findViewById(R.id.disableTouch);
        shuffleImage=v.findViewById(R.id.nowPlayingShuffle);
        repeatHolder=v.findViewById(R.id.nowPlayingRepeatHolder);
        viewPager = v.findViewById(R.id.nowPlayingViewPager);

        if(savedInstanceState!=null) {
            seekBar.setVisibility(savedInstanceState.getInt("seekbarvisibility"));
            nowPlayingDurationText.setVisibility(savedInstanceState.getInt("nowduration"));
            nowPlayingElapsedText.setVisibility(savedInstanceState.getInt("nowelapsed"));
//            disableTouch.setVisibility(savedInstanceState.getInt("disabletouchvisibility"));
        }
        return v;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(GlobalVariables.shuffleState==PlaybackStateCompat.SHUFFLE_MODE_NONE){
            shuffleImage.setActivated(false);
        }else{
            shuffleImage.setActivated(true);
        }
        if(GlobalVariables.repeatState==PlaybackStateCompat.REPEAT_MODE_NONE){
            repeatHolder.setActivated(false);
            view.findViewById(R.id.nowPlayingRepeatOne).setVisibility(View.GONE);
            view.findViewById(R.id.nowPlayingRepeat).setVisibility(View.VISIBLE);
        }else if(GlobalVariables.repeatState==PlaybackStateCompat.REPEAT_MODE_ONE){
            repeatHolder.setActivated(true);
            view.findViewById(R.id.nowPlayingRepeat).setVisibility(View.GONE);
            view.findViewById(R.id.nowPlayingRepeatOne).setVisibility(View.VISIBLE);

        }else{
            repeatHolder.setActivated(true);
            view.findViewById(R.id.nowPlayingRepeatOne).setVisibility(View.GONE);
            view.findViewById(R.id.nowPlayingRepeat).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter=new NowPlayingPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(mAdapter);
        sharedFragmentViewModel.getCurrentSongsOrder().observe(this, songs -> {
            _songs=songs;
            mAdapter.swapData((ArrayList<Song>) _songs);
            viewPager.setCurrentItem(pos,false);

            nowPlayingDurationText.setText(Helpers.timeConversion((int)TimeUnit.MILLISECONDS.toSeconds(_songs.get(pos).metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))));

            Glide.with(this)
                    .load(_songs.get(pos).imageBytes)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            isFirstLoad=true;
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            isFirstLoad=true;
                            return false;
                        }
                    })
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(getResources().getDimensionPixelSize(R.dimen.round_edges_dimen)))
                            .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true))
                    .into(new GlideViewTargetSub(foregroundArt));

            ((NowPlayingSeekbar)seekBar).setPlayPauseBtn(nowPlayingPlayBtn,nowPlayingPauseBtn);
            ((NowPlayingSeekbar)seekBar).setTextViews(nowPlayingElapsedText, nowPlayingDurationText);
            ((NowPlayingSeekbar)seekBar).setMaxSongDuration((int)_songs.get(pos).metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
            ((NowPlayingSeekbar)seekBar).setInactive(true);
            ((NowPlayingSeekbar)seekBar).setActive();
        });

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            //will be called if manually changed or auto next thus the boolean isPlaybackCompleted
            @Override
            public void onPageSelected(int position) {
                pos=position;
                bundle.putLong("newsongid", Long.parseLong(_songs.get(position).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
                bundle.putInt("currentposition", position);
                // do not call if onplaybackcomplete==true as song is auto set
                resultReceiverCustom.send(GlobalVariables.PLAY_UPDATE_CODE, bundle);
                if(isFirstLoad) {
                    Glide.with(NowPlayingHostFragment.this)
                            .load(_songs.get(position).imageBytes)
                            .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(getResources().getDimensionPixelSize(R.dimen.round_edges_dimen)))
                                    .placeholder(R.mipmap.ic_launcher).skipMemoryCache(true))
                            .into(new GlideViewTargetSub(foregroundArt));
                }
            }
        });
        nowPlayingPlayBtn.setOnClickListener(playPauseListener());
        nowPlayingPauseBtn.setOnClickListener(playPauseListener());

//        //onplaybackcompleted auto next
        sharedFragmentViewModel.getCurrentPosition().observe(this, integer ->{
            if(isFirstLoad) {
                pos=integer;
                viewPager.setCurrentItem(integer);
            }
        });
        sharedFragmentViewModel.getPlaybackState().observe(this,playbackStateCompat -> {
            this.playbackStateCompat=playbackStateCompat;
            if (playbackStateCompat.getState() != PlaybackStateCompat.STATE_PLAYING) {
                nowPlayingPauseBtn.setVisibility(View.GONE);
                nowPlayingPlayBtn.setVisibility(View.VISIBLE);
            } else {
                nowPlayingPlayBtn.setVisibility(View.GONE);
                nowPlayingPauseBtn.setVisibility(View.VISIBLE);
            }
        });
        shuffleImage.setOnClickListener(clickListener());
        repeatHolder.setOnClickListener(clickListener());
    }

    @Override
    public void onResume() {
        super.onResume();
        seekBar.setVisibility(View.VISIBLE);
        nowPlayingDurationText.setVisibility(View.VISIBLE);
        nowPlayingElapsedText.setVisibility(View.VISIBLE);
        //enable touch
//        disableTouch.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("seekbarvisibilty",seekBar.getVisibility());
        outState.putInt("nowduration",nowPlayingDurationText.getVisibility());
        outState.putInt("nowelapsed",nowPlayingElapsedText.getVisibility());
//        outState.putInt("disabletouchvisibility",disableTouch.getVisibility());
    }

    private View.OnClickListener clickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.nowPlayingShuffle:
                        switch (mediaControllerCompat.getShuffleMode()){
                            case PlaybackStateCompat.SHUFFLE_MODE_NONE:
                                shuffleImage.setActivated(true);
                                mediaControllerCompat.sendCommand("shuffleon", null, null);
                                Toast.makeText(getContext(), "shuffle all", Toast.LENGTH_SHORT).show();
                                break;
                            case PlaybackStateCompat.SHUFFLE_MODE_ALL:
                                shuffleImage.setActivated(false);
                                mediaControllerCompat.sendCommand("shuffleoff", null, null);
                                Toast.makeText(getContext(), "shuffle off", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        break;
                    case R.id.nowPlayingRepeatHolder:
                        switch (mediaControllerCompat.getRepeatMode()){
                            case PlaybackStateCompat.REPEAT_MODE_NONE:
                                repeatHolder.setActivated(true);
                                getView().findViewById(R.id.nowPlayingRepeat).setVisibility(View.GONE);
                                getView().findViewById(R.id.nowPlayingRepeatOne).setVisibility(View.VISIBLE);
                                mediaControllerCompat.sendCommand("repeatone",null,null);
                                Toast.makeText(getContext(),"repeat one",Toast.LENGTH_SHORT).show();
                                break;
                            case PlaybackStateCompat.REPEAT_MODE_ONE:
                                getView().findViewById(R.id.nowPlayingRepeatOne).setVisibility(View.GONE);
                                getView().findViewById(R.id.nowPlayingRepeat).setVisibility(View.VISIBLE);
                                mediaControllerCompat.sendCommand("repeatloop",null,null);
                                Toast.makeText(getContext(),"repeat all",Toast.LENGTH_SHORT).show();
                                break;
                            case PlaybackStateCompat.REPEAT_MODE_ALL:
                                repeatHolder.setActivated(false);
                                getView().findViewById(R.id.nowPlayingRepeatOne).setVisibility(View.GONE);
                                getView().findViewById(R.id.nowPlayingRepeat).setVisibility(View.VISIBLE);
                                mediaControllerCompat.sendCommand("repeatoff",null,null);
                                Toast.makeText(getContext(),"repeat off",Toast.LENGTH_SHORT).show();
                                break;
                        }
                }
            }
        };
    }
    private View.OnClickListener playPauseListener() {
        Bundle bundle = new Bundle();
        return v -> {
            if(playbackStateCompat!=null) {
                if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PAUSED) {
                    bundle.putBoolean("playpauseclick",true);
                    resultReceiverCustom.send(GlobalVariables.PLAY_PAUSE_UPDATE_CODE,bundle);
                } else if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    bundle.putBoolean("playpauseclick",false);
                    resultReceiverCustom.send(GlobalVariables.PLAY_PAUSE_UPDATE_CODE,bundle);
                }
            }
        };
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((NowPlayingSeekbar)seekBar).destroy();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
//        Glide.with(this).clear(foregroundArt);
    }
//    /**
//     * Prepares the shared element transition from and back to the grid fragment.
//     */
//    private void prepareSharedElementTransition() {
//        TransitionInflater inflater = TransitionInflater.from(getContext());
//        Transition transition =
//                inflater
//                        .inflateTransition(R.transition.image_shared_transition);
//        transition.setDuration(200);
//        setSharedElementEnterTransition(transition);
//
//        Transition transition1 = inflater.inflateTransition(R.transition.nowplaying_enter_transition);
//        transition1.setDuration(200);
//        setEnterTransition(transition1);
//
//        // A similar mapping is set at the GridFragment with a setExitSharedElementCallback.
//        setEnterSharedElementCallback(
//                new SharedElementCallback() {
//
//                    @Override
//                    public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
//                        seekBar.setVisibility(View.VISIBLE);
//                        nowPlayingDurationText.setVisibility(View.VISIBLE);
//                        nowPlayingElapsedText.setVisibility(View.VISIBLE);
//                        //enable touch
//                        disableTouch.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
//                        // Map the first shared element name to the child ImageView.
//                        sharedElements.put(names.get(0), getView().findViewById(R.id.foregroundAlbumArt));
//                    }
//                });
//    }
    class NowPlayingPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<Song> songs;

        private NowPlayingPagerAdapter(@NonNull FragmentManager fm) {
            super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return NowPlayingFragment.newInstance(songs.get(position));
        }

        @Override
        public int getCount() {
            return songs!=null?songs.size():0;
        }

        private void swapData(ArrayList<Song> songs){
            this.songs=songs;
            notifyDataSetChanged();
        }
    }
}
