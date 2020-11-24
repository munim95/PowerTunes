package com.rigid.powertunes.loader;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.rigid.powertunes.songmodels.Album;
import com.rigid.powertunes.songmodels.Artist;
import com.rigid.powertunes.songmodels.Genre;
import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;

/**
 * Created by MunimsMac on 10/01/2018.
 */

public class SongsLoader {

//    public static Song getSongForCursor(Cursor cursor) {
//        Song song = new Song();
//
//        if ((cursor != null) && (cursor.moveToFirst())) {
//            long id = cursor.getLong(0);
//            String title = cursor.getString(1);
//            String artist = cursor.getString(2);
//            String album = cursor.getString(3);
//            int duration = cursor.getInt(4);
//            int trackNumber = cursor.getInt(5);
//            long artistId = cursor.getInt(6);
//            long albumId = cursor.getLong(7);
//            long dateAdded = cursor.getLong(8);
//            String data = cursor.getString(9);
//            song = new Song(id, albumId, artistId, title, artist, album, duration, trackNumber,dateAdded,data, imageBytes);
//        }
//
//        return song;
//    }

//    public static ArrayList<Song> getSongsForCursor(Cursor cursor) {
////        Log.d("cursorcount",(cursor!=null?cursor.getCount():0)+"");
//        ArrayList<Song> arrayList = new ArrayList<>(cursor!=null?cursor.getCount():10);
//
//        if ((cursor != null) && (cursor.moveToFirst())) {
//
//            do {
//                long id = cursor.getLong(0);
//                String title = cursor.getString(1);
//                String artist = cursor.getString(2);
//                String album = cursor.getString(3);
//                int duration = cursor.getInt(4);
//                int trackNumber = cursor.getInt(5);
//                long artistId = cursor.getInt(6);
//                long albumId = cursor.getLong(7);
//                long dateAdded = cursor.getLong(8);
//                String data = cursor.getString(9);
//
//                arrayList.add(new Song(id, albumId, artistId, title, artist, album, duration, trackNumber, dateAdded,data, imageBytes));
//
//            } while (cursor.moveToNext());
//        }
//
//        return arrayList;
//    }
//
//    public static ArrayList<Album> getAlbumsForCursor(Cursor cursor){
//        ArrayList<Album> arrayList = new ArrayList<>();
//        if ((cursor != null) && (cursor.moveToFirst())) {
//            do {
//                long albumId = cursor.getLong(0);
//                String artist = cursor.getString(1);
//                String album = cursor.getString(2);
////                int numOfSongs=cursor.getInt(3);
//                String albumKey=cursor.getString(3);
//
//                arrayList.add(new Album(albumId, artist, album, albumKey));
//            } while (cursor.moveToNext());
//        }
//        return arrayList;
//    }
//    public static ArrayList<Artist> getArtistsForCursor(Cursor cursor){
//        ArrayList<Artist> arrayList = new ArrayList<>();
//        if ((cursor != null) && (cursor.moveToFirst())) {
//            do {
//                long artistId = cursor.getLong(0);
//                String artist = cursor.getString(1);
//                String artistKey = cursor.getString(2);
//                int numOfTracks = cursor.getInt(3);
//                arrayList.add(new Artist(artistId,artist, artistKey, numOfTracks));
//            } while (cursor.moveToNext());
//        }
//        return arrayList;
//    }
//    public static ArrayList<Genre> getGenresForCursor(Cursor cursor){
//        ArrayList<Genre> arrayList = new ArrayList<>();
//        if ((cursor != null) && (cursor.moveToFirst())) {
//            do {
//                long id = cursor.getLong(0);
//                String name = cursor.getString(1);
////                int count = cursor.getInt(2);
//                arrayList.add(new Genre(id,name));
//            } while (cursor.moveToNext());
//        }
//        return arrayList;
//    }



    public static Uri getAlbumArtUriById(long id) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), id);
    }

    public static Uri getSongUriById(long id) {
        return ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id);
    }




}
