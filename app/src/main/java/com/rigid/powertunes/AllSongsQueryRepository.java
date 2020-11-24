package com.rigid.powertunes;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

import com.rigid.powertunes.loader.PlaylistsLoader;
import com.rigid.powertunes.loader.SongsLoader;
import com.rigid.powertunes.songmodels.Album;
import com.rigid.powertunes.songmodels.Artist;
import com.rigid.powertunes.songmodels.Genre;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.PlaylistSong;
import com.rigid.powertunes.songmodels.Song;
import com.rigid.powertunes.util.SongsUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class AllSongsQueryRepository {
    /**
     * MADE REDUNDANT. MOVED TO DIRECT FILE FETCHING.
     * */
    private static final String TAG =AllSongsQueryRepository.class.getSimpleName();

    private static final int TOKEN_QUERY = 0;
    private static final int ALBUM_TYPE=1;
    private static final int ARTIST_TYPE=2;
    private static final int GENRES_TYPE=3;
    private static final int DELETESONG=4;
    private static final int SINGLESONG=5;
    private static final int ALLPLAYLISTS = 6;
    private static final int SINGLEPLAYLIST = 7;
    private static final int PLAYLISTSONGS = 8;
    private static final int SONGQUERY = 9;
    private static final int ARTISTQUERY = 10;
    private static final int ALBUMQUERY = 11;

    private static final int TESTQUERY = 12;

    private final Uri MUSIC_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private final Uri PLAYLIST_URI = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
    private final Uri ARTISTS_URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
    private final Uri GENRES_URI = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;

    private final String[] PROJECTION = {"_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id","date_added","_data"};
    private final String[] ALBUMPROJECTION = {"album_id","artist", "album", "album_key"};
    private final String[] ARTISTPROJECTION = {"_id","artist","artist_key","number_of_tracks"};
    private final String[] GENRESPROJECTION = {"_id","name"};

    private final String DEFAULT_SORT_ORDER = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

//    private QueryHandler mQueryHandler;
//
//    private static int playListId_=-1;
//    @NonNull
//        public static AllSongsQueryRepository getInstance() {
//            return new AllSongsQueryRepository();
//        }
//
//        public MutableLiveData<List<Song>> fetchAllSongs(ContentResolver contentResolver) {
//            mQueryHandler = new QueryHandler(contentResolver);
//
//            final MutableLiveData<List<Song>> result = new MutableLiveData<>();
//            // Pass MutableLiveData in as a cookie, so we can set the result
//            // in OnQueryComplete
//            mQueryHandler.startQuery(TOKEN_QUERY, result,
//                    MUSIC_URI, PROJECTION, null,
//                    null, DEFAULT_SORT_ORDER);
//            return result;
//        }
//        public MutableLiveData<List<Song>> fetchRecentlyAdded(ContentResolver contentResolver){
//            mQueryHandler= new QueryHandler(contentResolver);
//            final MutableLiveData<List<Song>> result = new MutableLiveData<>();
//            mQueryHandler.startQuery(TOKEN_QUERY, result, MUSIC_URI, PROJECTION, null,
//                    null, MediaStore.MediaColumns.DATE_ADDED+" DESC");
//            return result;
//        }
//        public MutableLiveData<List<Album>> fetchAlbums(ContentResolver contentResolver){
//            mQueryHandler=new QueryHandler(contentResolver);
//            final MutableLiveData<List<Album>> result = new MutableLiveData<>();
//            mQueryHandler.startQuery(ALBUM_TYPE,result,MUSIC_URI,ALBUMPROJECTION,"0==0 ) GROUP BY (" + MediaStore.Audio.Media.ALBUM_ID,
//                    null, MediaStore.Audio.Media.ALBUM);
//            return result;
//        }
//        public MutableLiveData<List<Artist>> fetchArtists(ContentResolver contentResolver){
//            mQueryHandler=new QueryHandler(contentResolver);
//            final MutableLiveData<List<Artist>> result = new MutableLiveData<>();
//            mQueryHandler.startQuery(ARTIST_TYPE,result,ARTISTS_URI,ARTISTPROJECTION,"0==0 ) GROUP BY (" + MediaStore.Audio.Media._ID,
//                    null, MediaStore.Audio.Media.ARTIST);
//            return result;
//        }
//        public MutableLiveData<List<Genre>> fetchGenres(ContentResolver contentResolver){
//            mQueryHandler=new QueryHandler(contentResolver);
//            final MutableLiveData<List<Genre>> result = new MutableLiveData<>();
//            mQueryHandler.startQuery(GENRES_TYPE,
//                    result,
//                    GENRES_URI,
//                    GENRESPROJECTION,
//                    "0==0 ) GROUP BY (" + MediaStore.Audio.Genres.NAME,
//                    null,
//                    MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
//            return result;
//        }
//
//
//        public void deleteSongForUri(ContentResolver contentResolver, ArrayList<Song> deletedSongs) {
//            mQueryHandler=new QueryHandler(contentResolver);
//            final StringBuilder selection = new StringBuilder();
//            selection.append(BaseColumns._ID + " IN (");
//            for (int i = 0; i < deletedSongs.size(); i++) {
//                selection.append(deletedSongs.get(i).id);
//                if (i < deletedSongs.size() - 1) {
//                    selection.append(",");
//                }
//
//            }
//            selection.append(")");
//            mQueryHandler.startQuery(DELETESONG,
//                    null,
//                    MUSIC_URI,
//                    new String[]{BaseColumns._ID, MediaStore.MediaColumns.DATA},
//                    selection.toString(),
//                    null,
//                    null);
//            mQueryHandler.startDelete(DELETESONG,null,MUSIC_URI,selection.toString(),null);
//        }
//
//        public MutableLiveData<List<Playlist>> getAllPlaylists(ContentResolver contentResolver){
//            mQueryHandler=new QueryHandler(contentResolver);
//            final MutableLiveData<List<Playlist>> result=new MutableLiveData<>();
//            mQueryHandler.startQuery(ALLPLAYLISTS,
//                    result,
//                    PLAYLIST_URI,
//                    new String[]{BaseColumns._ID, MediaStore.Audio.PlaylistsColumns.NAME},
//                    null,
//                    null,
//                    MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
//            return result;
//        }
//        public MutableLiveData<Playlist> getSinglePlaylist(ContentResolver contentResolver, final String selection, final String[] values){
//            mQueryHandler=new QueryHandler(contentResolver);
//            final MutableLiveData<Playlist> result=new MutableLiveData<>();
//            mQueryHandler.startQuery(SINGLEPLAYLIST,
//                    result,
//                    PLAYLIST_URI,
//                    new String[]{BaseColumns._ID, MediaStore.Audio.PlaylistsColumns.NAME},
//                    selection,
//                    values,
//                    MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
//            return result;
//        }
//        public MutableLiveData<List<PlaylistSong>> getPlaylistSongs(ContentResolver contentResolver, final int playlistId){
//            playListId_=playlistId;
//            mQueryHandler= new QueryHandler(contentResolver);
//            final MutableLiveData<List<PlaylistSong>> result=new MutableLiveData<>();
//            mQueryHandler.startQuery(PLAYLISTSONGS,result,MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
//                    new String[]{
//                            MediaStore.Audio.Playlists.Members.AUDIO_ID,// 0
//                            MediaStore.Audio.AudioColumns.TITLE,// 1
//                            MediaStore.Audio.AudioColumns.TRACK,// 2
//                            MediaStore.Audio.AudioColumns.DURATION,// 3
//                            MediaStore.Audio.AudioColumns.DATE_ADDED,// 4
//                            MediaStore.Audio.AudioColumns.ALBUM_ID,// 5
//                            MediaStore.Audio.AudioColumns.ALBUM,// 6
//                            MediaStore.Audio.AudioColumns.ARTIST_ID,// 7
//                            MediaStore.Audio.AudioColumns.ARTIST,// 8
//                            MediaStore.Audio.Playlists.Members._ID, // 9
//                            MediaStore.Audio.Playlists.Members.DATA},//10
//                    MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''",
//                    null,
//                    MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
//            return result;
//        }
//        public MutableLiveData<List<Song>> getSongsForQuery(ContentResolver cr, final String query){
//            mQueryHandler = new QueryHandler(cr);
//            final MutableLiveData<List<Song>> result = new MutableLiveData<>();
//            mQueryHandler.startQuery(SONGQUERY, result,
//                    MUSIC_URI, PROJECTION, MediaStore.Audio.AudioColumns.TITLE + " LIKE ?",
//                    new String[]{"%" + query + "%"}, DEFAULT_SORT_ORDER);
//            return result;
//        }
//        public MutableLiveData<List<Artist>> getArtistsForQuery(ContentResolver cr, final String query){
//            mQueryHandler = new QueryHandler(cr);
//            final MutableLiveData<List<Artist>> result = new MutableLiveData<>();
//            mQueryHandler.startQuery(ARTISTQUERY, result,
//                    ARTISTS_URI, ARTISTPROJECTION, MediaStore.Audio.AudioColumns.ARTIST + " LIKE ?",
//                    new String[]{"%" + query + "%"}, MediaStore.Audio.Media.ARTIST);
//            return result;
//        }
//        public MutableLiveData<List<Album>> getAlbumsForQuery(ContentResolver cr, final String query){
//            mQueryHandler = new QueryHandler(cr);
//            final MutableLiveData<List<Album>> result = new MutableLiveData<>();
//            mQueryHandler.startQuery(ALBUMQUERY, result,
//                    MUSIC_URI, ALBUMPROJECTION, MediaStore.Audio.AudioColumns.ALBUM + " LIKE ?",
//                    new String[]{"%" + query + "%"}, MediaStore.Audio.Media.ALBUM);
//            return result;
//        }
//
//        public void startTestQuery(ContentResolver cr, final String selection,final String[] selectionArgs){
//            mQueryHandler=new QueryHandler(cr);
//            mQueryHandler.startQuery(TESTQUERY, null,MUSIC_URI,null,
//                    selection,selectionArgs,null);
//            Log.d(TAG,""+selection + " args "+ selectionArgs.length);
//        }

    //AsyncQueryHandler
//        private static class QueryHandler extends AsyncQueryHandler {
//            private ContentResolver contentResolver;
//
//            private ArrayList<Song> songList;
//            private ArrayList<Album> albumList;
//            private ArrayList<Artist> artistList;
//            private ArrayList<Genre> genreList;
//            private ArrayList<Playlist> playlists;
//            private Playlist singlePlaylist;
//            private ArrayList<PlaylistSong> playlistSongs;
//            private ArrayList<Song> querySongs;
//            private ArrayList<Artist> queryArtist;
//            private ArrayList<Album> queryAlbum;
//
//            private QueryHandler(ContentResolver cr) {
//                super(cr);
//                contentResolver=cr;
//            }
//            @Override
//            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
//                try {
//                    switch (token) {
//                        case TOKEN_QUERY:
////                            Log.d(TAG,(cursor!=null)+"");
//                            final MutableLiveData<List<Song>> mutableData = (MutableLiveData<List<Song>>) cookie;
////                            songList = SongsLoader.getSongsForCursor(cursor);
//                            mutableData.setValue(songList);
//                            MediaLibrary.setAllSongs(songList);
//                            break;
//                        case ALBUM_TYPE:
//                            final MutableLiveData<List<Album>> mutableAlbumData = (MutableLiveData<List<Album>>) cookie;
//                            albumList = SongsLoader.getAlbumsForCursor(cursor);
//                            mutableAlbumData.setValue(albumList);
//                            Log.d(TAG,albumList.size()+" albums");
//                            break;
//                        case ARTIST_TYPE:
//                            final MutableLiveData<List<Artist>> mutableArtistData = (MutableLiveData<List<Artist>>) cookie;
//                            artistList = SongsLoader.getArtistsForCursor(cursor);
//                            mutableArtistData.setValue(artistList);
//                            Log.d(TAG,artistList.size()+" artists");
//                            break;
//                        case GENRES_TYPE:
//                            final MutableLiveData<List<Genre>> mutableGenreData = (MutableLiveData<List<Genre>>) cookie;
////                            genreList = SongsLoader.getGenresForCursor(cursor);
//                            mutableGenreData.setValue(genreList);
////                            Log.d(TAG,genreList.size()+" genres" + cursor.getCount());
//                            break;
//                        case ALLPLAYLISTS:
//                            final MutableLiveData<List<Playlist>> mutableLiveData=(MutableLiveData<List<Playlist>>)cookie;
//                            playlists=PlaylistsLoader.getAllPlaylists(cursor);
//                            mutableLiveData.setValue(playlists);
//                            break;
//                        case SINGLEPLAYLIST:
//                            final MutableLiveData<Playlist> mutableSinglePlaylist=(MutableLiveData<Playlist>)cookie;
//                            singlePlaylist=PlaylistsLoader.getPlaylist(cursor);
//                            mutableSinglePlaylist.setValue(singlePlaylist);
//                            break;
//                        case PLAYLISTSONGS:
//                            if(playListId_!=-1) {
//                                Log.d(TAG,"playlistid" + playListId_);
//                                final MutableLiveData<List<PlaylistSong>> _playlistSongs = (MutableLiveData<List<PlaylistSong>>) cookie;
////                                playlistSongs = PlaylistsLoader.getPlaylistSongList(cursor, playListId_);
//                                _playlistSongs.setValue(playlistSongs); }
//                            break;
//                        case DELETESONG:
//                            //delete from files
//                            SongsUtil.deleteTracks(cursor);
//                            contentResolver.notifyChange(Uri.parse("content://media"), null);
//                            break;
//                        case SONGQUERY:
//                            final MutableLiveData<List<Song>> _querySongs=(MutableLiveData<List<Song>>)cookie;
////                            querySongs=SongsLoader.getSongsForCursor(cursor);
////                            Log.d(TAG,"songquery" + querySongs.toString());
//                            _querySongs.setValue(querySongs);
//                            break;
//                        case ARTISTQUERY:
//                            final MutableLiveData<List<Artist>> _queryArtist=(MutableLiveData<List<Artist>>)cookie;
//                            queryArtist=SongsLoader.getArtistsForCursor(cursor);
////                            Log.d(TAG,"artist" + (queryArtist.size()>0?queryArtist.get(0).artist:queryArtist.size()));
//                            _queryArtist.setValue(queryArtist);
//                            break;
//                        case ALBUMQUERY:
//                            final MutableLiveData<List<Album>> _queryAlbum=(MutableLiveData<List<Album>>)cookie;
//                            queryAlbum=SongsLoader.getAlbumsForCursor(cursor);
////                            Log.d(TAG,"album" + (queryAlbum.size()>0?queryAlbum.get(0).albumName:queryAlbum.size()));
//                            _queryAlbum.setValue(queryAlbum);
//                            break;
//                        case TESTQUERY:
////                            Log.d(TAG,""+ SongsLoader.getSongsForCursor(cursor).toString());
//                            break;
//                    }
//                } finally {
//                    if (cursor != null) {
//                        cursor.close();
//                    }
//                }
//            }
//    }
}
