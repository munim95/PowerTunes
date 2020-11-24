package com.rigid.powertunes.viewmodels;

import android.app.Application;

import com.rigid.powertunes.provider.FetchSongFilesAsync;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.PlaylistSong;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class PlaylistDataViewModel extends AndroidViewModel {

    private MutableLiveData<List<Playlist>> liveDataPlaylists;
    private MutableLiveData<Playlist> liveDataSinglePlaylist;
    private MutableLiveData<List<PlaylistSong>> liveDataplaylistSongs;

    @NonNull
    private final FetchSongFilesAsync fetchSongFilesAsync;

    public PlaylistDataViewModel(@NonNull Application application) {
        super(application);
        fetchSongFilesAsync = FetchSongFilesAsync.getInstance();
    }

    public LiveData<List<Playlist>> getAllPlaylists(){
        if(liveDataPlaylists==null){
            liveDataPlaylists=new MutableLiveData<>();
        }
        liveDataPlaylists=fetchSongFilesAsync.getAllPlaylists(getApplication().getApplicationContext());
        return liveDataPlaylists;
    }

//    public LiveData<Playlist> getPlaylistById(final int playlistId) {
//        if(liveDataSinglePlaylist==null){
//            liveDataSinglePlaylist=new MutableLiveData<>();
//        }
//        liveDataSinglePlaylist=mSongsRepository.getSinglePlaylist(getApplication().getContentResolver(),
//                BaseColumns._ID + "=?",
//                new String[]{
//                        String.valueOf(playlistId)});
//        return liveDataSinglePlaylist;
//    }
//
//    public LiveData<Playlist> getPlaylistByName(final String playlistName) {
//        if(liveDataSinglePlaylist==null){
//            liveDataSinglePlaylist=new MutableLiveData<>();
//        }
//        liveDataSinglePlaylist=mSongsRepository.getSinglePlaylist(getApplication().getContentResolver(),
//                MediaStore.Audio.PlaylistsColumns.NAME + "=?",
//                new String[]{
//                        playlistName});
//        return liveDataSinglePlaylist;
//    }

    public LiveData<List<PlaylistSong>> getPlaylistSongs(final int playlistId){
        if(liveDataplaylistSongs==null){
            liveDataplaylistSongs=new MutableLiveData<>();
        }
        liveDataplaylistSongs=fetchSongFilesAsync.getPlaylistSongs(getApplication().getApplicationContext(),playlistId);
        return liveDataplaylistSongs;
    }
}
