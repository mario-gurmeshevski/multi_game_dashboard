package com.example.educationgame.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.educationgame.data.local.entity.LevelProgressEntity;

import java.util.List;

@Dao
public interface LevelProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LevelProgressEntity progress);

    @Update
    void update(LevelProgressEntity progress);

    @Delete
    void delete(LevelProgressEntity progress);

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    List<LevelProgressEntity> getProgressByLevelId(int levelId);
}
