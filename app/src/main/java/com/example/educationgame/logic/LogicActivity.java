package com.example.educationgame.logic;

import android.content.Intent;

import com.example.educationgame.R;
import com.example.educationgame.common.BaseLevelSelectActivity;
import com.example.educationgame.data.enums.GameTypeEnum;

public class LogicActivity extends BaseLevelSelectActivity {

    private static final int[] LEVEL_CARD_IDS = {
            R.id.levelCard1, R.id.levelCard2, R.id.levelCard3,
            R.id.levelCard4, R.id.levelCard5, R.id.levelCard6,
            R.id.levelCard7, R.id.levelCard8, R.id.levelCard9
    };

    private static final int[] LEVEL_STATUS_IDS = {
            R.id.levelStatus1, R.id.levelStatus2, R.id.levelStatus3,
            R.id.levelStatus4, R.id.levelStatus5, R.id.levelStatus6,
            R.id.levelStatus7, R.id.levelStatus8, R.id.levelStatus9
    };

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_logic;
    }

    @Override
    protected GameTypeEnum getGameType() {
        return GameTypeEnum.LOGIC;
    }

    @Override
    protected int getLevelCount() {
        return LogicLevels.getLevelCount();
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
        return "Logic Circuit";
    }

    @Override
    protected String getGameDescription() {
        return "Learn logic gates";
    }

    @Override
    protected String getLevelDescription(int levelNumber) {
        LogicLevelConfig config = LogicLevels.getLevel(levelNumber);
        return config != null ? config.getDescription() : "Logic Level " + levelNumber;
    }

    @Override
    protected int getTotalPossibleStars() {
        return levelCount * 3;
    }

    @Override
    protected Intent buildPlayIntent(int index, int levelNum, int levelId) {
        Intent intent = new Intent(this, LogicLevelPlayActivity.class);
        intent.putExtra(LogicLevelPlayActivity.EXTRA_LEVEL_NUMBER, levelNum);
        intent.putExtra(LogicLevelPlayActivity.EXTRA_LEVEL_ID, levelId);
        return intent;
    }
}
