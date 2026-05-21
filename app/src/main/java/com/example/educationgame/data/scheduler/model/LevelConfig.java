package com.example.educationgame.data.scheduler.model;

import com.example.educationgame.data.enums.SchedulingAlgorithm;

import java.util.List;

public class LevelConfig {

    private final SchedulingAlgorithm algorithm;
    private final List<ProcessDef> processes;
    private final int timeQuantum;
    private final int threeStarSeconds;
    private final int twoStarSeconds;

    public LevelConfig(SchedulingAlgorithm algorithm, List<ProcessDef> processes) {
        this(algorithm, processes, 0, 0, 0);
    }

    public LevelConfig(SchedulingAlgorithm algorithm, List<ProcessDef> processes, int timeQuantum) {
        this(algorithm, processes, timeQuantum, 0, 0);
    }

    public LevelConfig(SchedulingAlgorithm algorithm, List<ProcessDef> processes,
                       int threeStarSeconds, int twoStarSeconds) {
        this(algorithm, processes, 0, threeStarSeconds, twoStarSeconds);
    }

    public LevelConfig(SchedulingAlgorithm algorithm, List<ProcessDef> processes,
                       int timeQuantum, int threeStarSeconds, int twoStarSeconds) {
        this.algorithm = algorithm;
        this.processes = processes;
        this.timeQuantum = timeQuantum;
        this.threeStarSeconds = threeStarSeconds;
        this.twoStarSeconds = twoStarSeconds;
    }

    public SchedulingAlgorithm getAlgorithm() {
        return algorithm;
    }

    public List<ProcessDef> getProcesses() {
        return processes;
    }

    public int getProcessCount() {
        return processes.size();
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }

    public int getThreeStarSeconds() {
        return threeStarSeconds;
    }

    public int getTwoStarSeconds() {
        return twoStarSeconds;
    }

    public boolean hasStarThresholds() {
        return threeStarSeconds > 0 && twoStarSeconds > 0;
    }
}
