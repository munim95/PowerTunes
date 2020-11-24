package com.rigid.powertunes.recyclerviewhelpers;

import com.rigid.powertunes.songmodels.Album;
import com.rigid.powertunes.songmodels.Artist;
import com.rigid.powertunes.songmodels.Folder;
import com.rigid.powertunes.songmodels.Genre;
import com.rigid.powertunes.songmodels.Playlist;
import com.rigid.powertunes.songmodels.Song;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

public class RecyclerViewFoldersDiffCallback extends DiffUtil.Callback {
    private final List<?> mOldList;
    private final List<?> mNewList;
    public RecyclerViewFoldersDiffCallback(List<?> oldList, List<?> newList) {
        this.mOldList = oldList;
        this.mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        final Object _old= mOldList.get(oldItemPosition);
        final Object _new = mNewList.get(newItemPosition);
        boolean condition=false;
        if(_old instanceof Folder){
            condition=((Folder)_old).folderName.equals(((Folder)_new).folderName);
        }else if(_old instanceof Playlist){
            condition= ((Playlist)_old).id == ((Playlist)_new).id;
        }else if(_old instanceof Genre){
            condition= ((Genre)_old).genreName.equals(((Genre)_new).genreName);
        }else if(_old instanceof Artist){
            condition= ((Artist)_old).artist.equals(((Artist)_new).artist);
        }else if(_old instanceof Album){
            condition= ((Album)_old).albumName.equals(((Album)_new).albumName);
        }
        return condition;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Object _old= mOldList.get(oldItemPosition);
        final Object _new = mNewList.get(newItemPosition);
        boolean condition=false;
        if(_old instanceof Folder){
            condition=((Folder)_old).songsInFolders.equals(((Folder)_new).songsInFolders);
        }else if(_old instanceof Playlist){
            condition= ((Playlist)_old).getChildList().equals(((Playlist)_new).getChildList());
        }else if(_old instanceof Genre){
            condition= ((Genre)_old).getChildList().equals(((Genre)_new).getChildList());
        }else if(_old instanceof Artist){
            condition= ((Artist)_old).getChildList().equals(((Artist)_new).getChildList());
        }else if(_old instanceof Album){
            condition= ((Album)_old).getChildList().equals(((Album)_new).getChildList());
        }
        return condition;
    }
    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}
