package com.example.educationgame.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.educationgame.data.enums.GameTypeEnum;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
    tableName = "levels",
    foreignKeys = @ForeignKey(
        entity = GameEntity.class,
        parentColumns = "id",
        childColumns = "gameId",
        onDelete = CASCADE
    )
)
public class LevelEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int levelNumber;

    private String name;

    private String description;

    private int gameId;

    public LevelEntity() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLevelNumber() { return levelNumber; }
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
}
