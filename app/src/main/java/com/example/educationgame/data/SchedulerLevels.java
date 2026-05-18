package com.example.educationgame.data;

import com.example.educationgame.data.enums.SchedulingAlgorithm;
import com.example.educationgame.data.model.LevelConfig;
import com.example.educationgame.data.model.ProcessDef;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SchedulerLevels {

    public static final Map<Integer, LevelConfig> LEVELS = new LinkedHashMap<>();

    static {
        LEVELS.put(1, new LevelConfig(SchedulingAlgorithm.FCFS, Arrays.asList(
                new ProcessDef("Chrome", 0, 3),
                new ProcessDef("Spotify", 1, 5),
                new ProcessDef("Clock", 2, 2),
                new ProcessDef("Notes", 3, 4)
        )));

        LEVELS.put(2, new LevelConfig(SchedulingAlgorithm.FCFS, Arrays.asList(
                new ProcessDef("Firefox", 0, 6),
                new ProcessDef("Mail", 2, 2),
                new ProcessDef("Discord", 4, 8),
                new ProcessDef("Steam", 5, 3),
                new ProcessDef("Origin", 7, 4)
        )));

        LEVELS.put(3, new LevelConfig(SchedulingAlgorithm.SJF, Arrays.asList(
                new ProcessDef("Slack", 0, 7),
                new ProcessDef("Zoom", 2, 4),
                new ProcessDef("Maps", 4, 1),
                new ProcessDef("Notion", 5, 4)
        )));

        LEVELS.put(4, new LevelConfig(SchedulingAlgorithm.SJF, Arrays.asList(
                new ProcessDef("VSCode", 0, 6),
                new ProcessDef("Docker", 1, 8),
                new ProcessDef("Git", 2, 2),
                new ProcessDef("CLion", 3, 3),
                new ProcessDef("WebStorm", 5, 4)
        )));

        LEVELS.put(5, new LevelConfig(SchedulingAlgorithm.LJF, Arrays.asList(
                new ProcessDef("CS2", 0, 3),
                new ProcessDef("League", 1, 6),
                new ProcessDef("Dota 2", 2, 4),
                new ProcessDef("CoD", 3, 5)
        )));

        LEVELS.put(6, new LevelConfig(SchedulingAlgorithm.LJF, Arrays.asList(
                new ProcessDef("Word", 0, 2),
                new ProcessDef("Excel", 0, 5),
                new ProcessDef("OneNote", 1, 8),
                new ProcessDef("Outlook", 2, 3),
                new ProcessDef("OneDrive", 3, 6)
        )));

        LEVELS.put(7, new LevelConfig(SchedulingAlgorithm.PRIORITY, Arrays.asList(
                new ProcessDef("Chrome", 0, 4, 3),
                new ProcessDef("Spotify", 1, 3, 1),
                new ProcessDef("Dropbox", 2, 6, 4),
                new ProcessDef("Figma", 3, 2, 2)
        )));

        LEVELS.put(8, new LevelConfig(SchedulingAlgorithm.PRIORITY, Arrays.asList(
                new ProcessDef("Blender", 0, 5, 2),
                new ProcessDef("Godot", 1, 3, 4),
                new ProcessDef("LocalSend", 2, 4, 1),
                new ProcessDef("Xcode", 3, 2, 3),
                new ProcessDef("Vim", 4, 6, 5)
        )));

        LEVELS.put(9, new LevelConfig(SchedulingAlgorithm.FCFS, Arrays.asList(
                new ProcessDef("DataGrip", 0, 2),
                new ProcessDef("VS", 1, 4),
                new ProcessDef("RubyMine", 2, 6),
                new ProcessDef("GoLand", 3, 1),
                new ProcessDef("PyCharm", 4, 3),
                new ProcessDef("PhpStorm", 5, 5)
        )));
    }

    public static int getLevelCount() {
        return LEVELS.size();
    }

    public static LevelConfig getLevel(int levelNumber) {
        return LEVELS.get(levelNumber);
    }
}
