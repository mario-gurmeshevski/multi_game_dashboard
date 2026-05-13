package com.example.educationgame.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
    tableName = "level_progress",
    foreignKeys = {
        @ForeignKey(
            entity = LevelEntity.class,
            parentColumns = "id",
            childColumns = "levelId",
            onDelete = CASCADE
        )
    },
    indices = {@Index(value = "levelId")}
)
public class LevelProgressEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "levelId")
    private int levelId;

    @ColumnInfo(name = "isFinished")
    private boolean isFinished = false;

    @ColumnInfo(name = "score")
    private Integer score;

    @ColumnInfo(name = "completionTime")
    private Integer completionTime;

    @ColumnInfo(name = "attempts")
    private int attempts = 1;

    @ColumnInfo(name = "finishedAt")
    private Date finishedAt;

    public LevelProgressEntity() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLevelId() { return levelId; }
    public void setLevelId(int levelId) { this.levelId = levelId; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getCompletionTime() { return completionTime; }
    public void setCompletionTime(Integer completionTime) { this.completionTime = completionTime; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public Date getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Date finishedAt) { this.finishedAt = finishedAt; }
}
