package com.example.educationgame.logic;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LogicLevels {

    public static final Map<Integer, LogicLevelConfig> LEVELS = new LinkedHashMap<>();

    static {
        // ===== ГРУПА 1: TAP/TOGGLE (Нивоа 1-3) =====
        LEVELS.put(1, new LogicLevelConfig(
                1,
                LogicEngine.GateType.AND,
                "Both inputs must be TRUE",
                30, 60
        ));

        LEVELS.put(2, new LogicLevelConfig(
                2,
                LogicEngine.GateType.OR,
                "At least one input must be TRUE",
                30, 60
        ));

        LEVELS.put(3, new LogicLevelConfig(
                3,
                LogicEngine.GateType.NOT,
                "Flip the input to TRUE",
                20, 45
        ));

        // ===== ГРУПА 2: DRAG & DROP (Нивоа 4-6) =====
        LEVELS.put(4, new LogicLevelConfig(
                4,
                LogicEngine.GateType.AND,
                "Connect the wires correctly",
                120, 240
        ));

        LEVELS.put(5, new LogicLevelConfig(
                5,
                LogicEngine.GateType.OR,
                "Connect the wires correctly",
                150, 300
        ));

        LEVELS.put(6, new LogicLevelConfig(
                6,
                LogicEngine.GateType.NOT,
                "Connect the wires correctly",
                180, 360
        ));

        // ===== ГРУПА 3: ПОСТАВИ КОМПОНЕНТИ (Нивоа 7-9) =====
        LEVELS.put(7, new LogicLevelConfig(
                7,
                LogicEngine.GateType.AND,
                "Build a circuit with exactly 4 gates — AND and NOT required",
                180, 360
        ));

        LEVELS.put(8, new LogicLevelConfig(
                8,
                LogicEngine.GateType.OR,
                "Build a circuit with exactly 5 gates — AND, NOT and OR required",
                240, 480
        ));

        LEVELS.put(9, new LogicLevelConfig(
                9,
                LogicEngine.GateType.AND,
                "Build a complex circuit with exactly 6 gates",
                300, 600
        ));
    }

    public static int getLevelCount() {
        return LEVELS.size();
    }

    public static LogicLevelConfig getLevel(int levelNumber) {
        return LEVELS.get(levelNumber);
    }
}