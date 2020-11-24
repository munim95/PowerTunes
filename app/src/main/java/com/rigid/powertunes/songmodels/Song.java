package com.rigid.powertunes.songmodels;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaMetadataCompat;

import java.util.Objects;

/**
 * Created by MunimsMac on 03/01/2018.
 */

public class Song implements Parcelable {

    //columns
    public final String data;
    public byte[] imageBytes;
    public final MediaMetadataCompat metadataCompat;
    public final long _id;


    //empty song
    public Song() {
        this.data="";
        this.imageBytes=null;
        this.metadataCompat=null;
        this._id=-1;
    }

    public Song(String _data, byte[] imageBytes, MediaMetadataCompat metadataCompat, long _id) {
        this.data=_data;
        this.imageBytes = imageBytes;
        this.metadataCompat=metadataCompat;
        this._id=_id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Song song = (Song) obj;
        if (_id != song._id) return false;
        return Objects.equals(data, song.data);
    }

    @Override
    public int hashCode() {
        long result = _id;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return (int)result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) +
                ", title='" + metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE) + '\'' +
                ", duration=" + metadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) +
                ", dateAdded=" + metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DATE) +
                ", data='" + data + '\'' +
                ", albumName='" + metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM) + '\'' +
                ", artistName='" + metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) + '\'' +
                ", genre='" + metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_GENRE) + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.data);
        dest.writeLong(this._id);
        dest.writeParcelable(this.metadataCompat,flags);
    }

    protected Song(Parcel in) {
        this.data = in.readString();
        this._id=in.readLong();
        this.metadataCompat = in.readParcelable(MediaMetadataCompat.class.getClassLoader());
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };


}
