package com.example.educationgame.data.scheduler;

import com.example.educationgame.data.enums.SchedulingAlgorithm;
import com.example.educationgame.data.scheduler.model.LevelConfig;
import com.example.educationgame.data.scheduler.model.ProcessDef;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SchedulerLevels {

    public static final Map<Integer, LevelConfig> LEVELS = new LinkedHashMap<>();

    static {
        LEVELS.put(1, new LevelConfig(SchedulingAlgorithm.FCFS, Arrays.asList(
                new ProcessDef("Chrome", 0, 3),
                new ProcessDef("Spotify", 3, 5),
                new ProcessDef("Clock", 5, 2),
                new ProcessDef("Notes", 8, 4)
        ), 45, 75));

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
                new ProcessDef("Docker", 2, 8),
                new ProcessDef("Git", 5, 2),
                new ProcessDef("CLion", 7, 3),
                new ProcessDef("WebStorm", 10, 4)
        )));

        LEVELS.put(5, new LevelConfig(SchedulingAlgorithm.LJF, Arrays.asList(
                new ProcessDef("CS2", 0, 3),
                new ProcessDef("League", 2, 6),
                new ProcessDef("Dota 2", 4, 4),
                new ProcessDef("CoD", 7, 5)
        )));

        LEVELS.put(6, new LevelConfig(SchedulingAlgorithm.LJF, Arrays.asList(
                new ProcessDef("Word", 0, 2),
                new ProcessDef("Excel", 1, 5),
                new ProcessDef("OneNote", 4, 8),
                new ProcessDef("Outlook", 6, 3),
                new ProcessDef("OneDrive", 9, 6)
        )));

        LEVELS.put(7, new LevelConfig(SchedulingAlgorithm.PRIORITY, Arrays.asList(
                new ProcessDef("Chrome", 0, 4, 3),
                new ProcessDef("Spotify", 3, 3, 1),
                new ProcessDef("Dropbox", 5, 6, 4),
                new ProcessDef("Figma", 8, 2, 2)
        )));

        LEVELS.put(8, new LevelConfig(SchedulingAlgorithm.PRIORITY, Arrays.asList(
                new ProcessDef("Blender", 0, 5, 2),
                new ProcessDef("Godot", 2, 3, 4),
                new ProcessDef("LocalSend", 5, 4, 1),
                new ProcessDef("Xcode", 7, 2, 3),
                new ProcessDef("Vim", 10, 6, 5)
        )));

        LEVELS.put(9, new LevelConfig(SchedulingAlgorithm.FCFS, Arrays.asList(
                new ProcessDef("DataGrip", 0, 2),
                new ProcessDef("VS", 2, 4),
                new ProcessDef("RubyMine", 4, 6),
                new ProcessDef("GoLand", 6, 1),
                new ProcessDef("PyCharm", 8, 3),
                new ProcessDef("PhpStorm", 11, 5)
        )));

        LEVELS.put(10, new LevelConfig(SchedulingAlgorithm.ROUND_ROBIN, Arrays.asList(
                new ProcessDef("Safari", 0, 4),
                new ProcessDef("Terminal", 2, 3),
                new ProcessDef("Calendar", 3, 5),
                new ProcessDef("Reminders", 5, 2)
        ), 2));

        LEVELS.put(11, new LevelConfig(SchedulingAlgorithm.ROUND_ROBIN, Arrays.asList(
                new ProcessDef("Sketch", 0, 5),
                new ProcessDef("Kafka", 2, 3),
                new ProcessDef("Redis", 5, 6),
                new ProcessDef("Nginx", 7, 2),
                new ProcessDef("Mongo", 10, 4)
        ), 2));

        LEVELS.put(12, new LevelConfig(SchedulingAlgorithm.ROUND_ROBIN, Arrays.asList(
                new ProcessDef("Postgres", 0, 6),
                new ProcessDef("RabbitMQ", 1, 4),
                new ProcessDef("Grafana", 3, 3),
                new ProcessDef("Jenkins", 6, 5),
                new ProcessDef("Ansible", 9, 2)
        ), 2));
    }

    public static int getLevelCount() {
        return LEVELS.size();
    }

    public static LevelConfig getLevel(int levelNumber) {
        return LEVELS.get(levelNumber);
    }
}
