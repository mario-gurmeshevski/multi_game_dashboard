package com.example.educationgame.scheduler;

import android.content.Intent;

import com.example.educationgame.R;
import com.example.educationgame.common.BaseLevelSelectActivity;
import com.example.educationgame.data.enums.GameTypeEnum;
import com.example.educationgame.data.local.entity.LevelEntity;
import com.example.educationgame.data.scheduler.SchedulerLevels;

public class SchedulerActivity extends BaseLevelSelectActivity {

    private static final int[] LEVEL_CARD_IDS = {
            R.id.levelCard1, R.id.levelCard2, R.id.levelCard3,
            R.id.levelCard4, R.id.levelCard5, R.id.levelCard6,
            R.id.levelCard7, R.id.levelCard8, R.id.levelCard9,
            R.id.levelCard10, R.id.levelCard11, R.id.levelCard12
    };

    private static final int[] LEVEL_STATUS_IDS = {
            R.id.levelStatus1, R.id.levelStatus2, R.id.levelStatus3,
            R.id.levelStatus4, R.id.levelStatus5, R.id.levelStatus6,
            R.id.levelStatus7, R.id.levelStatus8, R.id.levelStatus9,
            R.id.levelStatus10, R.id.levelStatus11, R.id.levelStatus12
    };

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
    protected int[] getLevelCardIds() {
        return LEVEL_CARD_IDS;
    }

    @Override
    protected int[] getLevelStatusIds() {
        return LEVEL_STATUS_IDS;
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
    protected Intent buildPlayIntent(int index, int levelNum, int levelId) {
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
