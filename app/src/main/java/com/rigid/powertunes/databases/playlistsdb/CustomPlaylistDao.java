package com.rigid.powertunes.databases.playlistsdb;

import com.rigid.powertunes.databases.songqueuedb.SongEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CustomPlaylistDao {
    @Insert
    void insertSingleSong(CustomPlaylistEntity playlistEntity);
    @Insert
    void insertMultipleSongs(List<CustomPlaylistEntity> playlistEntities);

    @Query("SELECT * FROM CustomPlaylistEntity WHERE playlistId = :id")
    CustomPlaylistEntity fetchPlaylistById(int id);

    @Update
    void updateSongInPlaylist(CustomPlaylistEntity playlistEntity);
    @Delete
    void deleteSongFromPlaylist(CustomPlaylistEntity playlistEntity);

}

