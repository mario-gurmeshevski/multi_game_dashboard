package com.example.educationgame.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.educationgame.data.local.entity.LevelEntity;

import java.util.List;

@Dao
public interface LevelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LevelEntity level);

    @Query("SELECT * FROM levels WHERE gameId = :gameId")
    List<LevelEntity> getLevelsByGameId(int gameId);
}
