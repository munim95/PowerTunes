package com.rigid.powertunes.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.PlaylistSong;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PlaylistsLoader {

    @NonNull
    public static Playlist getPlaylist(@Nullable final Cursor cursor) {
        Playlist playlist = new Playlist();

        if (cursor != null && cursor.moveToFirst()) {
            playlist = getPlaylistFromCursorImpl(cursor);
        }
        if (cursor != null)
            cursor.close();
        return playlist;
    }


    @NonNull
    public static ArrayList<Playlist> getAllPlaylists(@Nullable final Cursor cursor) {
        ArrayList<Playlist> playlists = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlists.add(getPlaylistFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();
        return playlists;
    }

    @NonNull
    private static Playlist getPlaylistFromCursorImpl(@NonNull final Cursor cursor) {
        final int id = cursor.getInt(0);
        final String name = cursor.getString(1);
        return new Playlist(id, name);
    }

//    @NonNull
//    public static ArrayList<PlaylistSong> getPlaylistSongList(Cursor cursor, final int playlistId) {
//        ArrayList<PlaylistSong> songs = new ArrayList<>();
//
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                songs.add(getPlaylistSongFromCursorImpl(cursor, playlistId));
//            } while (cursor.moveToNext());
//        }
//        if (cursor != null) {
//            cursor.close();
//        }
//        return songs;
//    }

//    @NonNull
//    private static PlaylistSong getPlaylistSongFromCursorImpl(@NonNull Cursor cursor, int playlistId) {
//        final long id = cursor.getInt(0);
//        final String title = cursor.getString(1);
//        final int trackNumber = cursor.getInt(2);
//        final long duration = cursor.getLong(3);
//        final int dateAdded = cursor.getInt(4);
//        final long albumId = cursor.getInt(5);
//        final String albumName = cursor.getString(6);
//        final long artistId = cursor.getInt(7);
//        final String artistName = cursor.getString(8);
//        final int idInPlaylist = cursor.getInt(9);
//        String data = cursor.getString(9);
//
//        return new PlaylistSong(id, albumId, artistId, title, artistName, albumName, duration, trackNumber, dateAdded, playlistId, idInPlaylist,data);
//    }

    //following moved to playlist viewmodel
    //    @NonNull
//    public static ArrayList<Playlist> getAllPlaylists(@NonNull final Context context) {
//        return getAllPlaylists(makePlaylistCursor(context, null, null));
//    }
//
//    @NonNull
//    public static Playlist getPlaylist(@NonNull final Context context, final int playlistId) {
//        return getPlaylist(makePlaylistCursor(
//                context,
//                BaseColumns._ID + "=?",
//                new String[] {
//                        String.valueOf(playlistId)
//                }
//        ));
//    }
//
//    @NonNull
//    public static Playlist getPlaylist(@NonNull final Context context, final String playlistName) {
//        return getPlaylist(makePlaylistCursor(
//                context,
//                MediaStore.Audio.PlaylistsColumns.NAME + "=?",
//                new String[]{
//                        playlistName
//                }
//        ));
//    }

    //    public static Cursor makePlaylistCursor(@NonNull final Context context, final String selection, final String[] values) {
    //        try {
    //            return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
    //                    new String[]{
    //                            /* 0 */
    //                            BaseColumns._ID,
//    @Nullable
//                            /* 1 */
//                            MediaStore.Audio.PlaylistsColumns.NAME
//                    }, selection, values, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
//        } catch (SecurityException e) {
//            return null;
//        }
//    }
}
