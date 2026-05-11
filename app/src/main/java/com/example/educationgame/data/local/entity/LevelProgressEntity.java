package com.example.educationgame.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
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
        ),
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "userId",
            onDelete = CASCADE
        )
    }
)
public class LevelProgressEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int levelId;

    private int userId;

    private boolean isFinished = false;

    private Integer score;

    private Integer completionTime;

    private int attempts = 1;

    private Date finishedAt;

    public LevelProgressEntity() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLevelId() { return levelId; }
    public void setLevelId(int levelId) { this.levelId = levelId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

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
