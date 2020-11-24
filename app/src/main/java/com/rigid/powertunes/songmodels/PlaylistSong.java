package com.rigid.powertunes.songmodels;

import android.os.Parcel;
import android.os.Parcelable;

public class PlaylistSong extends Song  {
    public final int playlistId;

    public PlaylistSong(long _id, String _genre, String _title, String _artistName, String _albumName, long _duration, int _trackNumber, long _dateAdded, final int playlistId, String data, String contentUri,byte[] imageBytes) {
        super(data, imageBytes, null, -1);
        this.playlistId = playlistId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PlaylistSong that = (PlaylistSong) o;

        return playlistId != that.playlistId;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + playlistId;
        return result;
    }

    @Override
    public String toString() {
        return super.toString() +
                "PlaylistSong{" +
                "playlistId=" + playlistId +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.playlistId);
    }

    protected PlaylistSong(Parcel in) {
        super(in);
        this.playlistId = in.readInt();
    }

    public static final Parcelable.Creator<PlaylistSong> CREATOR = new Parcelable.Creator<PlaylistSong>() {
        public PlaylistSong createFromParcel(Parcel source) {
            return new PlaylistSong(source);
        }

        public PlaylistSong[] newArray(int size) {
            return new PlaylistSong[size];
        }
    };
}
