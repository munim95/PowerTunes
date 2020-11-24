package com.rigid.powertunes.songmodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.rigid.powertunes.GlobalVariables;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Folder implements Parcelable {
    public final String folderName;
    public List<Song> songsInFolders;

    public Folder(){
        folderName ="";
    }

    public Folder(String folderName){
        this.folderName =folderName;
        songsInFolders=new ArrayList<>();
    }

    public void addSongsToFolder(Song song){
        if(song!=null){
            songsInFolders.add(song);
        }
    }

    public List<Song> getSongsInFolders(){
        return songsInFolders;
    }

    public void setIsPlaying(boolean playing){
        GlobalVariables.currentSelectedFolder=folderName;
        if(playing){
            GlobalVariables.currentPlayingFolder=folderName;
        }else{
            GlobalVariables.currentPlayingFolder="";
        }
    }
    public boolean isCurrentPlayingFolder(){
        return folderName.equals(GlobalVariables.currentPlayingFolder);
    }
    public boolean isCurrentSelectedFolder(){
        return folderName.equals(GlobalVariables.currentSelectedFolder);
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public int hashCode() {
        long result=-1;
        result = 31 * result + (folderName != null ? folderName.hashCode() : 0);
        return (int)result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Folder folder = (Folder) obj;
        return (Objects.equals(folderName,folder.folderName));
    }

    protected Folder(Parcel in) {
        folderName = in.readString();
        songsInFolders = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(folderName);
        dest.writeTypedList(songsInFolders);
    }
}
