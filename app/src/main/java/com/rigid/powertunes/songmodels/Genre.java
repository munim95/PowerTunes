package com.rigid.powertunes.songmodels;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaMetadataCompat;

import com.rigid.powertunes.GlobalVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Genre implements Parcelable {
    //columns
//    public final long audioId;
    public final String genreName;
//    public final int count;

    private List<Song> songsForGenre;

    public Genre() {
//        this.audioId=-1;
        this.genreName = "";
//        this.count = -1;
    }

    @Override
    public int hashCode() {
        long result=-1;
        result = 31 * result + (genreName != null ? genreName.hashCode() : 0);
        return (int)result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Genre genre = (Genre) obj;
        return Objects.equals(genreName, genre.genreName);
    }

    public Genre(String _genreName) {
        this.genreName = _genreName;

        songsForGenre=new ArrayList<>();
    }

    public void addSongsForGenre(Song song){
        if(song!=null){
            songsForGenre.add(song);
        }
    }
    public void setIsPlaying(boolean playing){
        GlobalVariables.currentSelectedFolder=genreName;
        if(playing){
            GlobalVariables.currentPlayingFolder=genreName;
        }else{
            GlobalVariables.currentPlayingFolder="";
        }
    }
    public boolean isCurrentPlayingFolder(){
        return genreName.equals(GlobalVariables.currentPlayingFolder);
    }
    public boolean isCurrentSelectedFolder(){
        return genreName.equals(GlobalVariables.currentSelectedFolder);
    }
    public List<Song> getChildList() {
        Collections.sort(songsForGenre, (o1, o2) ->
                o1.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                        .compareTo(o2.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)));
        return songsForGenre;
    }
    //    }
    @Override
    public int describeContents() {
        return 0;
    }

    //        return songsForAlbum;
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(genreName);
        dest.writeTypedList(songsForGenre);
    }

    protected Genre(Parcel in) {
        genreName = in.readString();
        songsForGenre = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        @Override
        public Genre createFromParcel(Parcel in) {
            return new Genre(in);
        }

        @Override
        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };
}
