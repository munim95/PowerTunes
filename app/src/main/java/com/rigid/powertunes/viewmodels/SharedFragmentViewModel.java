package com.rigid.powertunes.viewmodels;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.rigid.powertunes.songmodels.Song;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedFragmentViewModel extends ViewModel {
    private final MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>();
    private final MutableLiveData<MediaMetadataCompat> mediaMetaDataState = new MutableLiveData<>();
    private final MutableLiveData<Integer> nextPosition = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>();
    private final MutableLiveData<List<Song>> currentFragmentSongList = new MutableLiveData<>();

    public SharedFragmentViewModel() {
        super();
    }

    public void setPlaybackState(PlaybackStateCompat state){
        playbackState.setValue(state);
    }
    public LiveData<PlaybackStateCompat> getPlaybackState(){
        return playbackState;
    }

    public void setMediaMetaDataState(MediaMetadataCompat metadataCompat){ mediaMetaDataState.setValue(metadataCompat);}
    public LiveData<MediaMetadataCompat> getMediaMetaDataCompat(){
        return mediaMetaDataState;
    }

    public void setNextPosition(int position){ nextPosition.setValue(position); }
    public LiveData<Integer> getNextPosition(){ return nextPosition; }

    public void setCurrentPosition(int position){ currentPosition.setValue(position); }
    public LiveData<Integer> getCurrentPosition(){ return currentPosition; }

    //set current fragment songs - now playing card, now playing frag are dependent
    public void setCurrentSongsOrder(List<Song> songList){
        currentFragmentSongList.setValue(songList);
    }
    public LiveData<List<Song>> getCurrentSongsOrder(){
        return currentFragmentSongList;
    }


}
