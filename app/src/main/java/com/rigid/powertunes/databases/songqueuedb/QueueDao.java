package com.rigid.powertunes.databases.songqueuedb;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface QueueDao {
    @Insert
    void insertSingleSong(SongEntity songEntity);
    @Insert
    void insertMultipleSongs(List<SongEntity> songEntities);

    @Query ("SELECT * FROM SongEntity WHERE id = :id")
    SongEntity fetchOneSongById(long id);

    @Update
    void updateSongFromQueue(SongEntity songEntity);
    @Delete
    void deleteSongFromQueue(SongEntity songEntity);

}
