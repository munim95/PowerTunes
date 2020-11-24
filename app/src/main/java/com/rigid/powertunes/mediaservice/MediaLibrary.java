package com.rigid.powertunes.mediaservice;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.LongSparseArray;

import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MediaLibrary {
    private static ArrayList<Song> allSongs;
    private static LongSparseArray<Song> songIdSparseArray;

    private static final TreeMap<Long, MediaMetadataCompat> songTree = new TreeMap<>();
    private static MediaMetadataCompat.Builder metaDataBuilder;
    private static List<MediaBrowserCompat.MediaItem> mediaItems;

    static {
        metaDataBuilder = new MediaMetadataCompat.Builder();
    }

    public static void setAllSongs(ArrayList<Song> songs){
        if(songs!=null) {
            if (songs.size() != 0){
                allSongs = songs;
                createMediaMetaDataFromList();
            }
        }
    }

    //create treemap of metadata for each song
    private static void createMediaMetaDataFromList(){
        songIdSparseArray = new LongSparseArray<>();
        mediaItems = new ArrayList<>();
        for(Song song: allSongs) {
//            MediaMetadataCompat metadataObject = metaDataBuilder
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id + "")
//                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
//                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artistName)
//                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
//                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
//                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, String.valueOf(song.contentUri))
//                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
//                            song.imageBytes!=null?
//                                    BitmapFactory.decodeByteArray(song.imageBytes,0,song.imageBytes.length,options):
//                                    null)
//                    .build();
            //id to builder object
            songTree.put(Long.parseLong(song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)), song.metadataCompat);
            mediaItems.add(new MediaBrowserCompat.MediaItem(song.metadataCompat.getDescription(),MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
            //id to song object
            songIdSparseArray.put(Long.parseLong(song.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)),song);
        }
    }
    //for fetching respective song object for its id
    public static LongSparseArray<Song> getSongIdSparseArray(){
        return songIdSparseArray;
    }
    //when app started first time started load first song
    public static ArrayList<Song> getTotalSongsList(){
        return allSongs;
    }
    public static List<MediaBrowserCompat.MediaItem> getMediaBrowserItems() {
        return mediaItems;
    }
    //create metadata object of one song from treemap
    public static MediaMetadataCompat getSongMetaDataForId(long id){
        return songTree.get(id);
    }
//    public static Bitmap getAlbumBitmap(String mediaId) {
//        return ImageLoader.getInstance().loadImageSync(getSongMetaDataForId(Long.parseLong(mediaId))
//                .getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
//    }

}
