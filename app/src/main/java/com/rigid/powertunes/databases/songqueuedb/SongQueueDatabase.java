package com.rigid.powertunes.databases.songqueuedb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SongEntity.class}, version = 1, exportSchema = false)
public abstract class SongQueueDatabase extends RoomDatabase {
    public abstract QueueDao queueDaoAccess();
}
