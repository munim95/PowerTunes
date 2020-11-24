package com.rigid.powertunes.viewpagers;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.rigid.powertunes.GlobalVariables;
import com.rigid.powertunes.main.activities.MainActivity;
import com.rigid.powertunes.R;
import com.rigid.powertunes.misc.GlideViewTargetSub;
import com.rigid.powertunes.misc.ResultReceiverCustom;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.viewmodels.SharedFragmentViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class NowPlayingCardFragment extends Fragment {
    private ImageView cardSongImage,pauseImg,playImg;
    private TextView cardSongName;
    private Song currentSong;
    private ResultReceiverCustom resultReceiverCustom=new ResultReceiverCustom(new Handler());
    private PlaybackStateCompat playbackStateCompat;

    public static NowPlayingCardFragment newInstance(Song song){
        Bundle bundle = new Bundle();
        bundle.putParcelable("currentsong",song);
        final NowPlayingCardFragment nowPlayingFragment = new NowPlayingCardFragment();
        nowPlayingFragment.setArguments(bundle);
        return nowPlayingFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentSong=getArguments().getParcelable("currentsong");
        resultReceiverCustom.setReceiver(((MainActivity)getActivity()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedFragmentViewModel sharedFragmentViewModel = ViewModelProviders.of(getActivity()).get(SharedFragmentViewModel.class);
        sharedFragmentViewModel.getPlaybackState().observe(getViewLifecycleOwner(), playbackStateCompat -> {
            this.playbackStateCompat=playbackStateCompat;
            if(playbackStateCompat.getActiveQueueItemId()==currentSong._id) {
                if (playbackStateCompat.getState() != PlaybackStateCompat.STATE_PLAYING) {
                    pauseImg.setVisibility(View.GONE);
                    playImg.setVisibility(View.VISIBLE);
                } else {
                    pauseImg.setVisibility(View.VISIBLE);
                    playImg.setVisibility(View.GONE);
                }
            }
        });

        View v =inflater.inflate(R.layout.now_playing_card,container,false);
        cardSongImage=v.findViewById(R.id.cardSongImage);
        cardSongName = v.findViewById(R.id.cardSongName);
        pauseImg=v.findViewById(R.id.cardPauseBtn);
        playImg=v.findViewById(R.id.cardPlayBtn);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cardSongName.setText(currentSong.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        setNowPlayingCardControls(nowPlayingCardListener());
        Glide.with(this)
                .load(currentSong.imageBytes)
                .apply(new RequestOptions().transforms(new CenterCrop(),
                        new RoundedCorners(10))
                        .skipMemoryCache(true)
                        .placeholder(R.mipmap.ic_launcher))
                .into(new GlideViewTargetSub(cardSongImage));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setNowPlayingCardControls(View.OnClickListener clickListener){
        playImg.setOnClickListener(clickListener);
        pauseImg.setOnClickListener(clickListener);
    }
    private View.OnClickListener nowPlayingCardListener(){
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
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Glide.with(this).clear(cardSongImage);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
