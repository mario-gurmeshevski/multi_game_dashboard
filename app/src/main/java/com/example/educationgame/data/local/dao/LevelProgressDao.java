package com.example.educationgame.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.educationgame.data.local.entity.LevelProgressEntity;

@Dao
public interface LevelProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LevelProgressEntity progress);

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId ORDER BY score DESC LIMIT 1")
    LevelProgressEntity getBestProgressByLevelId(int levelId);

    @Query("SELECT MIN(completionTime) FROM level_progress WHERE levelId = :levelId AND isFinished = 1")
    Integer getBestTimeByLevelId(int levelId);
}
