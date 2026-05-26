package com.example.educationgame.logic;

public class LogicLevelConfig {
    private final int levelNumber;
    private final LogicEngine.GateType gateType;
    private final String description;
    private final int threeStarSeconds;
    private final int twoStarSeconds;

    public LogicLevelConfig(int levelNumber, LogicEngine.GateType gateType,
                            String description, int threeStarSeconds, int twoStarSeconds) {
        this.levelNumber = levelNumber;
        this.gateType = gateType;
        this.description = description;
        this.threeStarSeconds = threeStarSeconds;
        this.twoStarSeconds = twoStarSeconds;
    }


    public LogicEngine.GateType getGateType() { return gateType; }
    public String getDescription()   { return description; }
    public int getThreeStarSeconds() { return threeStarSeconds; }
    public int getTwoStarSeconds()   { return twoStarSeconds; }
}