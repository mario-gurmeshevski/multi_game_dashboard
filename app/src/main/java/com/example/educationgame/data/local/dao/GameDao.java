package com.example.educationgame.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.educationgame.data.enums.GameTypeEnum;
import com.example.educationgame.data.local.entity.GameEntity;

import java.util.List;

@Dao
public interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GameEntity game);

    @Query("SELECT * FROM games WHERE type = :type")
    List<GameEntity> getGamesByType(GameTypeEnum type);
}
