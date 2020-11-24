package com.rigid.powertunes.viewmodels;

import android.app.Application;
import android.util.Log;

import com.rigid.powertunes.provider.FetchSongFilesAsync;
import com.rigid.powertunes.songmodels.Album;
import com.rigid.powertunes.songmodels.Artist;
import com.rigid.powertunes.songmodels.Folder;
import com.rigid.powertunes.songmodels.Genre;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class SearchDataViewModel extends AndroidViewModel {
    private MutableLiveData<List<Song>> liveSongs;
    private MutableLiveData<List<Artist>> liveArtists;
    private MutableLiveData<List<Album>> liveAlbums;
    private MutableLiveData<List<Genre>> liveGenres;
    private MutableLiveData<List<Folder>> liveFolders;
    private MutableLiveData<List<Playlist>> livePlaylists;

    private MediatorLiveData<List<Object>> mediatorLiveData;

    @NonNull
    private final FetchSongFilesAsync fetchSongFilesAsync;

    public SearchDataViewModel(@NonNull Application application) {
        super(application);
        fetchSongFilesAsync=FetchSongFilesAsync.getInstance();

    }

    private LiveData<List<Song>> songQuery(String query) {
        if (liveSongs == null) {
            liveSongs = new MutableLiveData<>();
        }
        liveSongs=fetchSongFilesAsync.getSongsForQuery(query);
        return liveSongs;

    }
    private LiveData<List<Album>> albumQuery(String query) {
        if (liveAlbums == null) {
            liveAlbums = new MutableLiveData<>();
        }
        liveAlbums=fetchSongFilesAsync.getAlbumsForQuery(query);
        return liveAlbums;

    }
    private LiveData<List<Artist>> artistQuery(String query) {
        if (liveArtists == null) {
            liveArtists=new MutableLiveData<>();
        }
        liveArtists=fetchSongFilesAsync.getArtistsForQuery(query);
        return liveArtists;
    }
    private LiveData<List<Genre>> genreQuery(String query) {
        if (liveGenres == null) {
            liveGenres=new MutableLiveData<>();
        }
        liveGenres=fetchSongFilesAsync.getGenresForQuery(query);
        return liveGenres;
    }
    private LiveData<List<Folder>> folderQuery(String query) {
        if (liveFolders == null) {
            liveFolders=new MutableLiveData<>();
        }
        liveFolders=fetchSongFilesAsync.getFoldersForQuery(query);
        return liveFolders;
    }
    private LiveData<List<Playlist>> playlistQuery(String query) {
        if (livePlaylists == null) {
            livePlaylists=new MutableLiveData<>();
        }
        livePlaylists=fetchSongFilesAsync.getPlaylistsForQuery(getApplication().getApplicationContext(),query);
        return livePlaylists;
    }

    //merge search live data
    public MediatorLiveData<List<Object>> mediatorLiveData(final String query){
        if(mediatorLiveData==null)
            mediatorLiveData=new MediatorLiveData<>();

        final List<Object> objects = new ArrayList<>();

        mediatorLiveData.addSource(songQuery(query), songs -> {
            if(songs.size()!=0) {
                objects.add("Songs");
                objects.addAll(songs);
            }
        });
       mediatorLiveData.addSource(albumQuery(query), albums -> {
           if(albums.size()!=0) {
               objects.add("Albums");
               objects.addAll(albums);
           }

       });
       mediatorLiveData.addSource(artistQuery(query), artists -> {
           if(artists.size()!=0) {
               objects.add("Artists");
               objects.addAll(artists);
           }

       });
       mediatorLiveData.addSource(genreQuery(query), genres -> {
           if(genres.size()!=0) {
               objects.add("Genres");
               objects.addAll(genres);
           }

       }); mediatorLiveData.addSource(folderQuery(query), folders -> {
           if(folders.size()!=0) {
               objects.add("Folders");
               objects.addAll(folders);
           }

       }); mediatorLiveData.addSource(playlistQuery(query), playlists -> {
           if(playlists.size()!=0) {
               objects.add("Playlists");
               objects.addAll(playlists);
           }
           mediatorLiveData.setValue(objects);

       });

       return mediatorLiveData;
    }

}
