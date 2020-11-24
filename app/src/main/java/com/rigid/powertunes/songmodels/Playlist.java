package com.rigid.powertunes.songmodels;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaMetadataCompat;


import com.rigid.powertunes.GlobalVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

public class Playlist implements Parcelable {
    public final int id;
    public final String name;
    private List<PlaylistSong> songsForPlaylist;

    public Playlist(final int id, final String name) {
        this.id = id;
        this.name = name;
        songsForPlaylist=new ArrayList<>();

    }

    public Playlist() {
        this.id = -1;
        this.name = "";
    }

    @NonNull
    public String getInfoString(@NonNull Context context) {
        return "";
    }

    public void setIsPlaying(boolean playing){
        GlobalVariables.currentSelectedFolder=name;
        if(playing){
            GlobalVariables.currentPlayingFolder=name;
        }else{
            GlobalVariables.currentPlayingFolder="";
        }
    }
    public boolean isCurrentPlayingFolder(){
        return name.equals(GlobalVariables.currentPlayingFolder);
    }
    public boolean isCurrentSelectedFolder(){
        return name.equals(GlobalVariables.currentSelectedFolder);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Playlist playlist = (Playlist) o;

        if (id != playlist.id) return false;
        return name != null ? name.equals(playlist.name) : playlist.name == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeTypedList(songsForPlaylist);
    }

    protected Playlist(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        songsForPlaylist = in.createTypedArrayList(PlaylistSong.CREATOR);
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        public Playlist createFromParcel(Parcel source) {
            return new Playlist(source);
        }

        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public void addSongsForPlaylist(PlaylistSong song){
        Collections.sort(songsForPlaylist, (o1, o2) -> o1.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                .compareTo(o2.metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE)));
        songsForPlaylist.add(song);
    }

    public List<PlaylistSong> getChildList() {
        return songsForPlaylist;
    }

}
