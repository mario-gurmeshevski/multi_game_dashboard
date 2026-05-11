package com.example.educationgame.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.educationgame.data.local.entity.LevelEntity;

import java.util.List;

@Dao
public interface LevelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LevelEntity level);

    @Update
    void update(LevelEntity level);

    @Delete
    void delete(LevelEntity level);

    @Query("SELECT * FROM levels")
    List<LevelEntity> getAllLevels();

    @Query("SELECT * FROM levels WHERE id = :id")
    LevelEntity getLevelById(int id);

    @Query("SELECT * FROM levels WHERE gameId = :gameId")
    List<LevelEntity> getLevelsByGameId(int gameId);
}
