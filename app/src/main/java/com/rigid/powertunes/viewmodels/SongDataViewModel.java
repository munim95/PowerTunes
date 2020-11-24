package com.rigid.powertunes.viewmodels;

import android.app.Application;
import android.util.Log;

import com.rigid.powertunes.provider.FetchSongFilesAsync;
import com.rigid.powertunes.songmodels.Album;
import com.rigid.powertunes.songmodels.Artist;
import com.rigid.powertunes.songmodels.Folder;
import com.rigid.powertunes.songmodels.Genre;
import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

public class SongDataViewModel extends ViewModel {

    private MutableLiveData<List<Song>> liveDataSongs;
    private MutableLiveData<List<Album>> liveDataAlbums;
    private MutableLiveData<List<Artist>> liveDataArtists;
    private MutableLiveData<List<Genre>> liveDataGenres;
    private MutableLiveData<List<Folder>> liveDataFolders;

    @NonNull
    private final FetchSongFilesAsync fetchSongFilesAsync;

    public SongDataViewModel() {
        super();
        fetchSongFilesAsync=FetchSongFilesAsync.getInstance();
    }

    public MutableLiveData<List<Song>> getSongs() {
        if (liveDataSongs == null) {
            liveDataSongs = new MutableLiveData<>();
        }
        liveDataSongs=fetchSongFilesAsync.getAllSongs();
        return liveDataSongs;
    }
    public MutableLiveData<List<Song>> getRecentlyAdded() {
        if (liveDataSongs == null) {
            liveDataSongs = new MutableLiveData<>();
        }
        liveDataSongs=fetchSongFilesAsync.getRecentlyAdded();
        return liveDataSongs;
    }
    public MutableLiveData<List<Album>> getAlbums() {
        if (liveDataAlbums == null) {
            liveDataAlbums = new MutableLiveData<>();
        }
        liveDataAlbums=fetchSongFilesAsync.getAlbums();
        return liveDataAlbums;
    }
    public MutableLiveData<List<Artist>> getArtists() {
        if (liveDataArtists == null) {
            liveDataArtists = new MutableLiveData<>();
        }
        liveDataArtists=fetchSongFilesAsync.getArtists();
        return liveDataArtists;
    }
    public MutableLiveData<List<Genre>> getGenres() {
        if (liveDataGenres == null) {
            liveDataGenres = new MutableLiveData<>();
        }
        liveDataGenres=fetchSongFilesAsync.getGenres();
        return liveDataGenres;
    }
    public MutableLiveData<List<Folder>> getFolders(){
        if(liveDataFolders==null){
            liveDataFolders = new MutableLiveData<>();
        }
        liveDataFolders=fetchSongFilesAsync.getFoldersSongs();
        return liveDataFolders;
    }
}
