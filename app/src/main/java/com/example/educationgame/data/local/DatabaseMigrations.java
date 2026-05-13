package com.example.educationgame.data.local;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class DatabaseMigrations {

    private DatabaseMigrations() {}

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_levels_gameId` ON `levels` (`gameId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_level_progress_levelId` ON `level_progress` (`levelId`)");
        }
    };
}
