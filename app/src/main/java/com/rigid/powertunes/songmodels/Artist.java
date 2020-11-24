package com.rigid.powertunes.songmodels;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaMetadataCompat;

import com.rigid.powertunes.GlobalVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Artist implements Parcelable {
    //columns
    public final String artist;
    public final int numOfTracks;

    private List<Song> songsForArtist;

    public Artist() {
        this.artist = "";
        this.numOfTracks=-1;
    }

    public Artist(String _artistName, int _numOfTracks) {
        this.artist = _artistName;
        this.numOfTracks = _numOfTracks;
        songsForArtist =new ArrayList<>();
    }

    public void addSongsForArtist(Song song){
        if(song!=null){
            songsForArtist.add(song);
        }
    }


//    public List<Song> getSongsForAlbum(){
    public List<Song> getChildList() {
        Collections.sort(songsForArtist, (o1, o2) -> o1.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                .compareTo(o2.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)));
        return songsForArtist;
    }
    public void setIsPlaying(boolean playing){
        GlobalVariables.currentSelectedFolder=artist;
        if(playing){
            GlobalVariables.currentPlayingFolder=artist;
        }else{
            GlobalVariables.currentPlayingFolder="";
        }
    }
    public boolean isCurrentPlayingFolder(){
        return artist.equals(GlobalVariables.currentPlayingFolder);
    }
    public boolean isCurrentSelectedFolder(){
        return artist.equals(GlobalVariables.currentSelectedFolder);
    }
    @Override
    public int hashCode() {
        long result=-1;
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + numOfTracks;
        return (int)result;
    }

    //        return songsForArtist;
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Artist artistObj = (Artist) obj;

        if (!Objects.equals(artist, artistObj.artist)) return false;
        return Objects.equals(numOfTracks, artistObj.numOfTracks);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeInt(numOfTracks);
        dest.writeTypedList(songsForArtist);
    }

    protected Artist(Parcel in) {
        artist = in.readString();
        numOfTracks = in.readInt();
        songsForArtist = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
}
