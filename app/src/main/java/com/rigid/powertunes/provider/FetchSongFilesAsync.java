package com.rigid.powertunes.provider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.rigid.powertunes.MultiLevelRvItem;
import com.rigid.powertunes.SettingsProgressCallback;
import com.rigid.powertunes.R;
import com.rigid.powertunes.songmodels.Album;
import com.rigid.powertunes.songmodels.Artist;
import com.rigid.powertunes.songmodels.Folder;
import com.rigid.powertunes.songmodels.Genre;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.PlaylistSong;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.util.FileUtil;
import com.rigid.powertunes.util.PlaylistUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.lifecycle.MutableLiveData;

public class FetchSongFilesAsync {
    //todo ON DELETE UPDATE CONTENT PROVIDER
    /**
     * Main Song Fetching Source.
     * */
    private static final String SUPPORTED_EXT_REGEX_FILTER = "\\.mp3|\\.ogg|\\.aac|\\.flac|\\.wav|\\.opus|\\.mkv|\\.m4a|\\.3gp|\\.mp4";
    private static final String TAG = FetchSongFilesAsync.class.getSimpleName();

    private FetchSongsAsyncCallbackInterface fetchSongsAsyncCallbackInterface;
    private SettingsProgressCallback settingsProgressCallback;

    private MediaMetadataCompat.Builder metaDataBuilder;

    private ArrayList<Song> audioSongs;

    private MutableLiveData<List<Song>> allSongsLiveData, lastAddedLiveData;
    private MutableLiveData<List<Artist>> artistsLiveData;
    private MutableLiveData<List<Album>> albumsLiveData;
    private MutableLiveData<List<Genre>> genresLiveData;
    private MutableLiveData<List<Playlist>> playlistLiveData;
    private MutableLiveData<List<Folder>> foldersLiveData;

    private HandlerThread scanningHandlerThread;
    private Handler fetchHandler;
    public String[] set;

    private static FetchSongFilesAsync getInstance;
    public static FetchSongFilesAsync getInstance(){
        if(getInstance==null)
            getInstance=new FetchSongFilesAsync();
        return getInstance;
    }
    private FetchSongFilesAsync(){
    }

    //returns size of the size of songs
    public void execute(Context context, Set<MultiLevelRvItem> selection,String[] paths, boolean showToast){
        if(scanningHandlerThread==null || !scanningHandlerThread.isAlive()) { //makes sure that thread is dead before starting a new one
            if(metaDataBuilder==null)
                metaDataBuilder=new MediaMetadataCompat.Builder();
            scanningHandlerThread = new HandlerThread("FetchAndScanThread");
            scanningHandlerThread.start();
            fetchHandler = new FetchAndScanHandler(scanningHandlerThread.getLooper(), context, paths, selection, this);
            fetchHandler.sendEmptyMessage(0);
            set=paths;
        }
//        int size=0;
//        FetchAndScanAsync fetchAndScanAsync = new FetchAndScanAsync(context, selection, showToast);
//        try {
//            size=fetchAndScanAsync.execute().get().size();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return size;
    }
    private static class FetchAndScanHandler extends Handler {
        private String[] filePathsSet;
        private Set<MultiLevelRvItem> set;
        private WeakReference<Context> context;
        private WeakReference<FetchSongFilesAsync> fetchSongFilesAsync;
        private Handler mainHandler;

        public FetchAndScanHandler(Looper looper, Context context, String[] set,Set<MultiLevelRvItem> selection, FetchSongFilesAsync fetchSongFilesAsync) {
            super(looper);
            this.filePathsSet =set;
            this.set=selection;
            this.context=new WeakReference<>(context);
            this.mainHandler = new Handler(context.getMainLooper());
            this.fetchSongFilesAsync=new WeakReference<>(fetchSongFilesAsync);
        }

        private void publishProgress(String... strs){
            mainHandler.post(()-> {
                        if (fetchSongFilesAsync.get().settingsProgressCallback != null)
                            fetchSongFilesAsync.get().settingsProgressCallback.onProgressUpdate(strs[0]);
                    });
        }
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    /* files cursor gets all files in external storage then checks if those files exist
                      in MediaStore.Audio and if duration and album art does not exist not then uses
                      MediaMetaDataRetriever to get them.
                     */
                    publishProgress(context.get().getString(R.string.songs_scan_prepare));
                    ArrayList<Uri> uris =  getAudioUris();
                    if(uris.size()!=0) {
                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        for(Uri uri : uris) {
                            try(ParcelFileDescriptor pfd = context.get().getContentResolver().openFileDescriptor(uri,"r")) {
                                if(pfd!=null) {
                                    mediaMetadataRetriever.setDataSource(pfd.getFileDescriptor());
                                    Cursor c = context.get().getContentResolver().query(uri,null,null,null,null);
                                    if(c!=null){
                                        while(c.moveToNext()){
                                            Log.d(TAG,"columns: "+c.getColumnNames()[0]);
                                        }
                                        c.close();
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG,"FILE DOES NOT EXIST OR INVALID URI.");
                                e.printStackTrace();
                            }
                        }
                    }
//                    String[] strFiles = getAudioPathsFromSelectedFiles();
//                    fetchSongFilesAsync.get().audioSongs = new ArrayList<>();
//                    if(strFiles.length!=0) {
//                        long id, dur, da;
//                        byte[] img;
//                        String g, t, ar = null, al = null, path;
//                        //gets all files with audio media type
//                        Cursor f = context.get().getContentResolver().query(
//                                MediaStore.Files.getContentUri("external"),
//                                null,
//                                        Utils.makeSQLSelectionArgs(MediaStore.Files.FileColumns.MEDIA_TYPE+"="+
//                                                        MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO,
//                                                MediaStore.Files.FileColumns.DATA,strFiles.length),
//                                strFiles,
//                                null);
//                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//                        //scan FILES TABLE
//                        if (f != null && f.getCount() != 0) {
//                            while (f.moveToNext()) {
//                                float progress = (float) fetchSongFilesAsync.get().audioSongs.size() / f.getCount() * 100;
//                                publishProgress(context.get().getString(R.string.songs_scanning,(int)progress));
//
//                                id = f.getLong(f.getColumnIndex(MediaStore.Files.FileColumns._ID));
//
//                                da = f.getLong(f.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED));
//                                t = f.getString(f.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
//                                path = f.getString(f.getColumnIndex(MediaStore.Files.FileColumns.DATA));
//                                mediaMetadataRetriever.setDataSource(path);
//
//
//                                //check audio cursor for missing values
//                                Cursor c = context.get().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                                        new String[]{MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM},
//                                        MediaStore.Audio.Media._ID + " LIKE ?", new String[]{id + ""}, null);
//                                if (c != null && c.getCount() != 0) {
//                                    c.moveToFirst();
//                                    dur = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION));
//                                    ar = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                                    al = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
//                                    c.close();
//                                } else {
//                                    //just get duration
//                                    dur=Long.parseLong(mediaMetadataRetriever.extractMetadata(METADATA_KEY_DURATION));
//                                    if(dur==-1) {
//                                        Log.e(TAG, "DURATION = -1. Removing song...");
//                                        continue;
//                                    }
//                                }
//
//                                g = getGenreNameForId(context.get(), id);
//
////                                Glide.with(context.get()).load(mediaMetadataRetriever.getEmbeddedPicture());
////                                img = mediaMetadataRetriever.getEmbeddedPicture();
////                                BitmapFactory.Options options = new BitmapFactory.Options();
////                                options.inSampleSize=4;
////                                song.imageBytes!=null?
////                                        BitmapFactory.decodeByteArray(song.imageBytes,0,song.imageBytes.length,options):
////                                        null;
//                                MediaMetadataCompat metadataObject = fetchSongFilesAsync.get().metaDataBuilder
//                                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id + "")
//                                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
//                                                al != null ? al.trim() : context.get().getString(R.string.unknown_album))
//                                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST,
//                                                ar != null ? ar.trim() : context.get().getString(R.string.unknown_artist))
//                                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE,
//                                                g != null && !g.equalsIgnoreCase("<unknown>") ? g.trim() : "Unknown")
//                                        .putString(MediaMetadataCompat.METADATA_KEY_DATE,String.valueOf(da))
//                                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, dur)
//                                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE,
//                                                t != null ? t : path!=null?
//                                                        new File(path).getName().split(SUPPORTED_EXT_REGEX_FILTER)[0]:
//                                                context.get().getString(R.string.unknown_song))
//                                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
//                                                MediaStore.Files.getContentUri("external", id).toString())
//                                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
//                                        .build();
//                                Song song = new Song(
//                                        path,
//                                        null,
//                                        metadataObject, id);
//                                //finally add prepared song
//                                fetchSongFilesAsync.get().audioSongs.add(song);
//                            }
//                            f.close();
//                            mediaMetadataRetriever.release();
//                        }
//                    }
//                    publishProgress(context.get().getResources().getString(R.string.songs_scanned,fetchSongFilesAsync.get().audioSongs.size()));
//                    MediaLibrary.setAllSongs(fetchSongFilesAsync.get().audioSongs);
//                    fetchSongFilesAsync.get().fetchSongsAsyncCallbackInterface.onSongsLoaded(fetchSongFilesAsync.get().audioSongs);
//                    //main handler
//                    outsideHandler.post( () -> {
//                        fetchSongFilesAsync.get().onPostExecuteCallback();
//                    });

                    break;
            }
            context.clear();
            getLooper().quitSafely();
        }
        private ArrayList<Uri> getAudioUris(){
            ArrayList<Uri> uris = new ArrayList<>();
            if(set!=null) {
                for (MultiLevelRvItem item : set){
                    ArrayList<Uri> uris1 = FileUtil.getAudioFilesForFile(item.getDocumentFile());
                    if(uris1.size()!=0)
                        uris.addAll(uris1);
                }
            }
            return uris;
        }
        private String[] getAudioPathsFromSelectedFiles(){
            ArrayList<String> audioFiles = new ArrayList<>();
            //find audio files in path
            if(set!=null){  //*unused* - except for scanning explicitly//
                for (MultiLevelRvItem fileItemModel : set) {
                    if(FileUtil.getAudioFilesForFile(fileItemModel.getDocumentFile())!=null &&
                            FileUtil.getAudioFilesForFile(fileItemModel.getDocumentFile()).size()!=0){
//                        audioFiles.addAll(FileUtil.getAudioFilesForFile(fileItemModel.getDocumentFile()));
                    }
                }
            }else {
                for (String str : filePathsSet) {
                    //songs in str path
                    File file = new File(str);
                    ArrayList<String> audios = FileUtil.getAudioFilesForFile(file);
                    if (audios != null && audios.size() != 0) {
                        audioFiles.addAll(audios);
                    }
                }
            }
            //convert audio files to absolute paths
            String[] strFiles = new String[audioFiles.size()];
            return audioFiles.toArray(strFiles);
//            if(audioFiles.size()!=0) {
//                int i = 0;
//                for (File file1 : audioFiles) {
//                    strFiles[i++] = file1.getAbsolutePath();
//                }
//            }else{
//                getLooper().quit();
//            }
        }
        private byte[] getAlbumArtForArtistId(Context context,long id){
            Cursor a = context.getContentResolver().query(MediaStore.Audio.Artists.Albums.getContentUri("external",id),
                    new String[]{MediaStore.Audio.Artists.Albums.ALBUM_ART},
                    null,null,null);
            byte[] b_=null;
            if(a!=null&&a.getCount()!=0){
                a.moveToFirst();
                Bitmap b=BitmapFactory.decodeFile(a.getString(a.getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM_ART)));
                if(b!=null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                    b_= stream.toByteArray();
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "BYTE STREAM: FAILED TO RELEASE RESOURCES");
                    }
                    b.recycle();
                }
                a.close();
            }
            return b_;
        }
        private String getGenreNameForId(Context context,long id){
            Cursor genres_c = context.getContentResolver()
                    .query(MediaStore.Audio.Genres.getContentUriForAudioId("external", (int)id),
                            null,null,null,null);
            String g=null;
            if(genres_c!=null && genres_c.getCount()!=0){
                genres_c.moveToFirst();
                g = genres_c.getString(genres_c.getColumnIndex(MediaStore.Audio.Genres.NAME));
                genres_c.close();
            }
            return g;
        }
        private boolean calculateDifference(ArrayList<Song> oldList,String[] newList){
            if(oldList==null)
                return false;
            ArrayList<String> pathsold = new ArrayList<>(oldList.size());
            for(Song song : oldList){
                pathsold.add(song.data);
            }
            ArrayList<String> paths = new ArrayList<>(newList.length);
            paths.addAll(Arrays.asList(newList));
            Collections.sort(pathsold, String::compareTo);
            Collections.sort(paths, String::compareTo);
            return pathsold.equals(paths);
        }
    }


    public int getSelectedNumber(){
        return audioSongs!=null?audioSongs.size():0;
    }


    public void setFetchSongsAsyncInterface(FetchSongsAsyncCallbackInterface _fetchSongsAsyncCallbackInterface){
        fetchSongsAsyncCallbackInterface = _fetchSongsAsyncCallbackInterface;
    }
    public void setSettingsPostExecuteCallback(SettingsProgressCallback _settingsCallback){
        settingsProgressCallback = _settingsCallback;
    }

    //update live data with new data
    private void onPostExecuteCallback(){
        if(settingsProgressCallback!=null)
            settingsProgressCallback.onComplete();
        getAllSongs();
        getRecentlyAdded();
        getArtists();
        getAlbums();
        getGenres();
        getFoldersSongs();
    }

    /**
     * SEND LIVEDATA TO VIEWMODELS
     * */
    public MutableLiveData<List<Song>> getAllSongs(){
        if(allSongsLiveData==null){
            allSongsLiveData= new MutableLiveData<>();
        }
        if(audioSongs!=null) {
            ArrayList<Song> songs = new ArrayList<>(audioSongs);
            Collections.sort(songs, (o1, o2) -> o1.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                    .compareTo(o2.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)));
            allSongsLiveData.setValue(songs);
        }
        return allSongsLiveData;
    }

    public MutableLiveData<List<Song>> getRecentlyAdded(){
        if(lastAddedLiveData==null){
            lastAddedLiveData=new MutableLiveData<>();
        }
        if(audioSongs!=null) {
            ArrayList<Song> songs = new ArrayList<>(audioSongs);
            Collections.sort(songs, (o1, o2) -> String.valueOf(o2.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DATE))
                    .compareTo(String.valueOf(o1.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DATE))));
            lastAddedLiveData.setValue(songs);
        }
        return lastAddedLiveData;
    }

    public MutableLiveData<List<Artist>> getArtists(){
        if (artistsLiveData == null) {
            artistsLiveData = new MutableLiveData<>();
        }
        if(audioSongs!=null) {
            ArrayList<Artist> artistList = new ArrayList<>();
            HashMap<String,Integer> map = genTrackNumForArtistMap(audioSongs);
            for (Song song : audioSongs) {
                String artistName = song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                Artist artist = new Artist(artistName,map.get(artistName));
                if (!artistList.contains(artist)) {
                    artist.addSongsForArtist(song);
                    artistList.add(artist);
                }else{
                    artistList.get(artistList.indexOf(artist)).addSongsForArtist(song);
                }
            }
            artistsLiveData.setValue(artistList);
        }
        return artistsLiveData;
    }
    //return HashMap for (k - artist , v - number of tracks) for the artist
    private HashMap<String,Integer> genTrackNumForArtistMap(List<Song> songs){
        HashMap<String,Integer> tracksMap = new HashMap<>();
        for(Song song : songs) {
            String artistName = song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            if(tracksMap.containsKey(artistName)){
                tracksMap.put(artistName,tracksMap.get(artistName)+1);
            }else{
                tracksMap.put(artistName,1);
            }
        }
        return tracksMap;
    }
    public MutableLiveData<List<Album>> getAlbums(){
        if (albumsLiveData == null) {
            albumsLiveData = new MutableLiveData<>();
        }
        if(audioSongs!=null) {
            ArrayList<Album> albumsList = new ArrayList<>();
            HashMap<String,String> map = genAlbumToArtistMap(audioSongs);
            for (Song song : audioSongs) {
                String albumName = song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
                Album album1 = new Album(map.get(albumName),albumName);
                if (!albumsList.contains(album1)) {
                    album1.addSongsForAlbum(song);
                    albumsList.add(album1);
                }else {
                    albumsList.get(albumsList.indexOf(album1)).addSongsForAlbum(song);
                }
            }
            albumsLiveData.setValue(albumsList);
        }
        return albumsLiveData;
    }
    //Artists for albums from songs
    private HashMap<String,String> genAlbumToArtistMap(List<Song> songs){
        HashMap<String,String> tracksMap = new HashMap<>();
        for(Song song:songs){
            String albumName = song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
            if(!tracksMap.containsKey(albumName))
                tracksMap.put(albumName,song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        }
        return tracksMap;
    }
    public MutableLiveData<List<Genre>> getGenres(){
        if (genresLiveData == null) {
            genresLiveData = new MutableLiveData<>();
        }
        if(audioSongs!=null) {
            ArrayList<Genre> genres = new ArrayList<>();
            for (Song song : audioSongs) {
                String _genre = song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
                Genre genre = new Genre(_genre);
                if (!genres.contains(genre)) {
                    genre.addSongsForGenre(song);
                    genres.add(genre);
                } else {
                    genres.get(genres.indexOf(genre)).addSongsForGenre(song);
                }
            }
            genresLiveData.setValue(genres);
        }
        return genresLiveData;
    }
    public MutableLiveData<List<Playlist>> getAllPlaylists(Context context){
        if(playlistLiveData==null){
            playlistLiveData=new MutableLiveData<>();
        }
        ArrayList<Playlist> playlists = PlaylistUtil.getAllPlaylistsInFile(context);
        for(Playlist playlist : playlists){
            for(PlaylistSong playlistSong: PlaylistUtil.getSongsForPlaylist(context,playlist.id)){
                playlist.addSongsForPlaylist(playlistSong);
            }
        }
        playlistLiveData.setValue(playlists);
        return playlistLiveData;
    }
    public MutableLiveData<List<PlaylistSong>> getPlaylistSongs(Context context,int playlistId){
        MutableLiveData<List<PlaylistSong>> mutableLiveData = new MutableLiveData<>();
        mutableLiveData.setValue(PlaylistUtil.getSongsForPlaylist(context,playlistId));
        return mutableLiveData;
    }
    public MutableLiveData<List<Folder>> getFoldersSongs(){
        if(foldersLiveData==null){
            foldersLiveData = new MutableLiveData<>();
        }
        if(audioSongs!=null) {
            ArrayList<Folder> folders = new ArrayList<>();
            for (Song song : audioSongs) {
                Folder folder = new Folder(new File(song.data).getParentFile().getName());
                if(!folders.contains(folder)){
                    folder.addSongsToFolder(song);
                    folders.add(folder);
                }else{
                    folders.get(folders.indexOf(folder)).addSongsToFolder(song);
                }
            }
            foldersLiveData.setValue(folders);
        }
        return foldersLiveData;
    }
    /**
     * Logic for search query
     * */
    public MutableLiveData<List<Song>> getSongsForQuery(String query){
        ArrayList<Song> querySongs = new ArrayList<>();
        for(Song song : audioSongs){
            if(song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE).toLowerCase().contains(query.toLowerCase())){
                querySongs.add(song);
            }
        }
        MutableLiveData<List<Song>> queryData = new MutableLiveData<>();
        queryData.setValue(querySongs);
        return queryData;
    }
    public MutableLiveData<List<Album>> getAlbumsForQuery(String query){
        ArrayList<Album> queryAlbums = new ArrayList<>();
        for (Map.Entry<String, String> entry : genAlbumToArtistMap(audioSongs).entrySet()) {
            if(entry.getKey().toLowerCase().contains(query.toLowerCase()))
                queryAlbums.add(new Album(entry.getValue(),entry.getKey()));
        }
        for (Album album : queryAlbums) {
            for (Song song : audioSongs) {
                if (song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM).equalsIgnoreCase(album.albumName)) {
                    album.addSongsForAlbum(song);
                }
            }
        }
        MutableLiveData<List<Album>> queryData = new MutableLiveData<>();
        queryData.setValue(queryAlbums);
        return queryData;
    }
    public MutableLiveData<List<Artist>> getArtistsForQuery(String query){
        ArrayList<Artist> queryArtists = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : genTrackNumForArtistMap(audioSongs).entrySet()) {
            if(entry.getKey().toLowerCase().contains(query.toLowerCase()))
                queryArtists.add(new Artist(entry.getKey(),entry.getValue()));
        }
        for (Artist artist1 : queryArtists) {
            for (Song song : audioSongs) {
                if (song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST).equalsIgnoreCase(artist1.artist)) {
                    artist1.addSongsForArtist(song);
                }
            }
        }
        MutableLiveData<List<Artist>> queryData = new MutableLiveData<>();
        queryData.setValue(queryArtists);
        return queryData;
    }
    public MutableLiveData<List<Genre>> getGenresForQuery(String query){
        ArrayList<Genre> queryGenres = new ArrayList<>();
        Set<String> currGenres = new HashSet<>();
        for (Song song : audioSongs) {
            String genre = song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            if (!genre.equals("")) {
                currGenres.add(genre);
            }
        }
        for(String str : currGenres){
            if(str.toLowerCase().contains(query.toLowerCase())) {
                queryGenres.add(new Genre(str));
            }
        }
        for (Genre genre : queryGenres) {
            for (Song song : audioSongs) {
                if (song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_GENRE).equals(genre.genreName)) {
                    genre.addSongsForGenre(song);
                }
            }
        }
        MutableLiveData<List<Genre>> genreData = new MutableLiveData<>();
        genreData.setValue(queryGenres);
        return genreData;
    }
    public MutableLiveData<List<Folder>> getFoldersForQuery(String query){
        ArrayList<Folder> foldersQuery = new ArrayList<>();
        Set<String> folderNames = new HashSet<>();
        for (Song song : audioSongs) {
            folderNames.add(new File(song.data).getParentFile().getName());
        }
        for (String str : folderNames) {
            if(str.toLowerCase().contains(query.toLowerCase()))
                foldersQuery.add(new Folder(str));
        }
        for (Folder folder : foldersQuery) {
            for (Song song : audioSongs) {
                if (folder.folderName.equals(new File(song.data).getParentFile().getName()))
                    folder.addSongsToFolder(song);
            }
        }

        MutableLiveData<List<Folder>> foldersData = new MutableLiveData<>();
        foldersData.setValue(foldersQuery);
        return foldersData;
    }
    public MutableLiveData<List<Playlist>> getPlaylistsForQuery(Context context,String query){
        ArrayList<Playlist> queryPlaylists = new ArrayList<>();
        ArrayList<Playlist> playlists = PlaylistUtil.getAllPlaylistsInFile(context);
        for(Playlist playlist : playlists){
            if(playlist.name.toLowerCase().contains(query.toLowerCase())){
                for(PlaylistSong playlistSong: PlaylistUtil.getSongsForPlaylist(context,playlist.id)){
                    playlist.addSongsForPlaylist(playlistSong);
                }
                queryPlaylists.add(playlist);
            }
        }
        MutableLiveData<List<Playlist>> playlistData = new MutableLiveData<>();
        playlistData.setValue(queryPlaylists);
        return playlistData;
    }

}
