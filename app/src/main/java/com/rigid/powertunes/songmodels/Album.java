package com.rigid.powertunes.songmodels;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.rigid.powertunes.GlobalVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Album implements Parcelable {
    //columns
    public final String albumName;
    public final String artistName;
//    public final int numOfTracks;

    private List<Song> songsForAlbum;

    public Album() {
        this.artistName = "";
        this.albumName = "";
//        this.numOfTracks=-1;
    }

    @Override
    public int hashCode() {
        long result=-1;
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        return (int)result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Album album = (Album) obj;

        if (!Objects.equals(albumName, album.albumName)) return false;
        return Objects.equals(artistName, album.artistName);
    }

    public Album(String _artistName, String _albumName) {
        this.artistName = _artistName;
        this.albumName = _albumName;
        songsForAlbum=new ArrayList<>();

//        this.numOfTracks=_numOfTracks;
    }

    public void addSongsForAlbum(Song song){
        if(song!=null){
            songsForAlbum.add(song);
        }
    }


    public List<Song> getChildList() {
        Collections.sort(songsForAlbum, (o1, o2) ->
                o1.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                        .compareTo(o2.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)));
        return songsForAlbum;
    }

    public void setIsPlaying(boolean playing){
        GlobalVariables.currentSelectedFolder=albumName;
        if(playing){
            GlobalVariables.currentPlayingFolder=albumName;
        }else{
            GlobalVariables.currentPlayingFolder="";
        }
    }
    public boolean isCurrentPlayingFolder(){
        return albumName.equals(GlobalVariables.currentPlayingFolder);
    }
    public boolean isCurrentSelectedFolder(){
        return albumName.equals(GlobalVariables.currentSelectedFolder);
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(albumName);
        dest.writeString(artistName);
        dest.writeTypedList(songsForAlbum);
    }

    protected Album(Parcel in) {
        albumName = in.readString();
        artistName = in.readString();
        songsForAlbum = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}
