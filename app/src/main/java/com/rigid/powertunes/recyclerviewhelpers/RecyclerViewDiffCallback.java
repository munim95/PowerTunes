package com.rigid.powertunes.recyclerviewhelpers;


import android.support.v4.media.MediaMetadataCompat;

import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

public class RecyclerViewDiffCallback extends DiffUtil.Callback {
    private final ArrayList<Song> mOldSongList;
    private final ArrayList<Song> mNewSongList;

    public RecyclerViewDiffCallback(ArrayList<Song> oldSongList, ArrayList<Song> newSongList) {
        this.mOldSongList = oldSongList;
        this.mNewSongList = newSongList;
    }

    @Override
    public int getOldListSize() {
        return mOldSongList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewSongList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return Long.parseLong(mOldSongList.get(oldItemPosition).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) ==
                Long.parseLong(mNewSongList.get(newItemPosition).metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Song oldSong= mOldSongList.get(oldItemPosition);
        final Song newSong = mNewSongList.get(newItemPosition);

        return oldSong.data.equals(newSong.data);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}
