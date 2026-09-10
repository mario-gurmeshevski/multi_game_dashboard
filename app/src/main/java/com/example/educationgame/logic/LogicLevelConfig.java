package com.example.educationgame.logic;

public class LogicLevelConfig {
    private final LogicEngine.GateType gateType;
    private final String description;
    private final int threeStarSeconds;
    private final int twoStarSeconds;
    private final int inputCount;

    public LogicLevelConfig(LogicEngine.GateType gateType,
                            String description, int threeStarSeconds, int twoStarSeconds) {
        this(gateType, description, threeStarSeconds, twoStarSeconds, 2);
    }

    public LogicLevelConfig(LogicEngine.GateType gateType,
                            String description, int threeStarSeconds, int twoStarSeconds,
                            int inputCount) {
        this.gateType = gateType;
        this.description = description;
        this.threeStarSeconds = threeStarSeconds;
        this.twoStarSeconds = twoStarSeconds;
        this.inputCount = inputCount;
    }

    public LogicEngine.GateType getGateType() { return gateType; }
    public String getDescription()   { return description; }
    public int getThreeStarSeconds() { return threeStarSeconds; }
    public int getTwoStarSeconds()   { return twoStarSeconds; }
    public int getInputCount()       { return inputCount; }
}