package com.example.educationgame.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.educationgame.data.enums.GameTypeEnum;

@Entity(tableName = "games")
public class GameEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private GameTypeEnum type;

    private String title;

    private String description;

    public GameEntity() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public GameTypeEnum getType() { return type; }
    public void setType(GameTypeEnum type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
