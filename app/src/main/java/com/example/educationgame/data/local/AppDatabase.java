package com.example.educationgame.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.educationgame.data.local.dao.GameDao;
import com.example.educationgame.data.local.dao.LevelDao;
import com.example.educationgame.data.local.dao.LevelProgressDao;
import com.example.educationgame.data.local.dao.UserDao;
import com.example.educationgame.data.local.entity.GameEntity;
import com.example.educationgame.data.local.entity.LevelEntity;
import com.example.educationgame.data.local.entity.LevelProgressEntity;
import com.example.educationgame.data.local.entity.UserEntity;

@Database(
    entities = {
        UserEntity.class,
        GameEntity.class,
        LevelEntity.class,
        LevelProgressEntity.class
    },
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract GameDao gameDao();
    public abstract LevelDao levelDao();
    public abstract LevelProgressDao levelProgressDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "education_game_db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
