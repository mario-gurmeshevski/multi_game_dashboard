package com.example.educationgame.logic;

import android.content.Intent;

import com.example.educationgame.R;
import com.example.educationgame.common.BaseLevelSelectActivity;
import com.example.educationgame.data.enums.GameTypeEnum;

public class LogicActivity extends BaseLevelSelectActivity {

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
    protected int getLevelGridContainerId() {
        return R.id.levelGridContainer;
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
