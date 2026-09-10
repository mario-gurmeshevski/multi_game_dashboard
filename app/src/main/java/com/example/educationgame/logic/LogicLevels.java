package com.example.educationgame.logic;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LogicLevels {

    public static final Map<Integer, LogicLevelConfig> LEVELS = new LinkedHashMap<>();

    static {
        LEVELS.put(1, new LogicLevelConfig(
                LogicEngine.GateType.AND,
                "Both inputs must be TRUE",
                30, 60, 2
        ));

        LEVELS.put(2, new LogicLevelConfig(
                LogicEngine.GateType.OR,
                "At least one of three inputs must be TRUE",
                30, 60, 3
        ));

        LEVELS.put(3, new LogicLevelConfig(
                LogicEngine.GateType.NOT,
                "Flip the input to TRUE",
                20, 45, 2
        ));

        LEVELS.put(4, new LogicLevelConfig(
                LogicEngine.GateType.AND,
                "Connect the wires correctly",
                120, 240
        ));

        LEVELS.put(5, new LogicLevelConfig(
                LogicEngine.GateType.OR,
                "Connect the wires correctly",
                150, 300
        ));

        LEVELS.put(6, new LogicLevelConfig(
                LogicEngine.GateType.NOT,
                "Connect the wires correctly",
                180, 360
        ));

        LEVELS.put(7, new LogicLevelConfig(
                LogicEngine.GateType.AND,
                "Build a circuit with exactly 4 gates — AND and NOT required",
                180, 360
        ));

        LEVELS.put(8, new LogicLevelConfig(
                LogicEngine.GateType.OR,
                "Build a circuit with exactly 5 gates — AND, NOT and OR required",
                240, 480
        ));

        LEVELS.put(9, new LogicLevelConfig(
                LogicEngine.GateType.AND,
                "Build a complex circuit with exactly 6 gates",
                300, 600
        ));

        LEVELS.put(10, new LogicLevelConfig(
                LogicEngine.GateType.OR,
                "Build a circuit with exactly 6 gates — OR and NOT required, AND forbidden",
                300, 600
        ));

        LEVELS.put(11, new LogicLevelConfig(
                LogicEngine.GateType.AND,
                "Build a circuit with exactly 7 gates — at least 2 AND and 2 NOT required",
                330, 660
        ));

        LEVELS.put(12, new LogicLevelConfig(
                LogicEngine.GateType.OR,
                "Build a circuit with exactly 7 gates — at least 2 OR and 2 NOT required",
                330, 660
        ));

        LEVELS.put(13, new LogicLevelConfig(
                LogicEngine.GateType.AND,
                "Build a circuit with exactly 8 gates — AND, OR and NOT all required",
                360, 720
        ));

        LEVELS.put(14, new LogicLevelConfig(
                LogicEngine.GateType.OR,
                "Build a circuit with exactly 9 gates — AND, OR and NOT all required, minimum 2 of each",
                420, 840
        ));

        LEVELS.put(15, new LogicLevelConfig(
                LogicEngine.GateType.AND,
                "Final challenge: build a circuit with exactly 10 gates using AND, OR and NOT",
                480, 960
        ));
    }

    public static int getLevelCount() {
        return LEVELS.size();
    }

    public static LogicLevelConfig getLevel(int levelNumber) {
        return LEVELS.get(levelNumber);
    }
}