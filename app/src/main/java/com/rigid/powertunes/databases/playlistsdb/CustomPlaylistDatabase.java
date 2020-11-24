package com.rigid.powertunes.databases.playlistsdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CustomPlaylistEntity.class}, version = 1, exportSchema = false)
public abstract class CustomPlaylistDatabase extends RoomDatabase {
    public abstract CustomPlaylistDao playlistDaoAccess();

}
