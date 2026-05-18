package com.example.educationgame.data;

import com.example.educationgame.data.enums.GameTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelStarConfig {

    public static class Thresholds {
        public final int threeStarSeconds;
        public final int twoStarSeconds;

        public Thresholds(int threeStarSeconds, int twoStarSeconds) {
            this.threeStarSeconds = threeStarSeconds;
            this.twoStarSeconds = twoStarSeconds;
        }
    }

    private static final int DEFAULT_THREE_STAR_SECONDS = 90;
    private static final int DEFAULT_TWO_STAR_SECONDS = 180;
    private static final int DEFAULT_LEVEL_COUNT = 9;

    private static final Map<GameTypeEnum, List<Thresholds>> CONFIGS = new HashMap<>();

    static {
        CONFIGS.put(GameTypeEnum.SCHEDULER, buildDefaultThresholds());
        CONFIGS.put(GameTypeEnum.LOGIC, buildDefaultThresholds());
    }

    private static List<Thresholds> buildDefaultThresholds() {
        List<Thresholds> list = new ArrayList<>();
        for (int i = 0; i < DEFAULT_LEVEL_COUNT; i++) {
            list.add(new Thresholds(DEFAULT_THREE_STAR_SECONDS, DEFAULT_TWO_STAR_SECONDS));
        }
        return list;
    }

    public static Thresholds getThresholds(GameTypeEnum gameType, int levelNumber) {
        List<Thresholds> thresholds = CONFIGS.get(gameType);
        if (thresholds == null || levelNumber < 1 || levelNumber > thresholds.size()) {
            return new Thresholds(DEFAULT_THREE_STAR_SECONDS, DEFAULT_TWO_STAR_SECONDS);
        }
        return thresholds.get(levelNumber - 1);
    }

    public static int getStars(GameTypeEnum gameType, int levelNumber, int timeSeconds) {
        Thresholds t = getThresholds(gameType, levelNumber);
        if (timeSeconds <= t.threeStarSeconds) return 3;
        if (timeSeconds <= t.twoStarSeconds) return 2;
        return 1;
    }

    public static int getTotalPossibleStars(GameTypeEnum gameType) {
        List<Thresholds> thresholds = CONFIGS.get(gameType);
        if (thresholds == null) return DEFAULT_LEVEL_COUNT * 3;
        return thresholds.size() * 3;
    }

    public static int getLevelCount(GameTypeEnum gameType) {
        List<Thresholds> thresholds = CONFIGS.get(gameType);
        if (thresholds == null) return DEFAULT_LEVEL_COUNT;
        return thresholds.size();
    }

    public static void setThresholds(GameTypeEnum gameType, int levelNumber, int threeStarSeconds, int twoStarSeconds) {
        List<Thresholds> thresholds = CONFIGS.get(gameType);
        if (thresholds != null && levelNumber >= 1 && levelNumber <= thresholds.size()) {
            thresholds.set(levelNumber - 1, new Thresholds(threeStarSeconds, twoStarSeconds));
        }
    }
}
