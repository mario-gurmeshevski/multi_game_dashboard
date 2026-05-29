package com.example.educationgame.scheduler;

import android.content.Intent;

import com.example.educationgame.R;
import com.example.educationgame.common.BaseLevelSelectActivity;
import com.example.educationgame.data.enums.GameTypeEnum;
import com.example.educationgame.data.local.entity.LevelEntity;
import com.example.educationgame.data.scheduler.SchedulerLevels;

public class SchedulerActivity extends BaseLevelSelectActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_scheduler;
    }

    @Override
    protected GameTypeEnum getGameType() {
        return GameTypeEnum.SCHEDULER;
    }

    @Override
    protected int getLevelCount() {
        return SchedulerLevels.getLevelCount();
    }

    @Override
    protected int getLevelGridContainerId() {
        return R.id.levelGridContainer;
    }

    @Override
    protected String getGameTitle() {
        return "OS Scheduler";
    }

    @Override
    protected String getGameDescription() {
        return "Learn CPU scheduling algorithms";
    }

    @Override
    protected String getLevelDescription(int levelNumber) {
        return "Scheduler Level " + levelNumber;
    }

    @Override
    protected int getTotalPossibleStars() {
        return com.example.educationgame.data.scheduler.LevelStarConfig
                .getTotalPossibleStars(GameTypeEnum.SCHEDULER);
    }

    @Override
    protected Intent buildPlayIntent(int levelNum, int levelId) {
        Intent intent = new Intent(this, LevelSchedulerPlayActivity.class);
        intent.putExtra(LevelSchedulerPlayActivity.EXTRA_LEVEL_NUMBER, levelNum);
        intent.putExtra(LevelSchedulerPlayActivity.EXTRA_LEVEL_ID, levelId);
        intent.putExtra(LevelSchedulerPlayActivity.EXTRA_GAME_TYPE, GameTypeEnum.SCHEDULER.name());
        return intent;
    }

    @Override
    protected void onLevelsSeeded(int gameId) {
        for (int i = 0; i < levelCount; i++) {
            if (levelIds[i] == 0) {
                LevelEntity level = new LevelEntity();
                level.setLevelNumber(i + 1);
                level.setName("Level " + (i + 1));
                level.setDescription("Scheduler Level " + (i + 1));
                level.setGameId(gameId);
                levelIds[i] = (int) db.levelDao().insert(level);
            }
        }
    }
}
