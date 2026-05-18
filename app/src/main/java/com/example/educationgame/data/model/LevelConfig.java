package com.example.educationgame.data.model;

import com.example.educationgame.data.enums.SchedulingAlgorithm;

import java.util.List;

public class LevelConfig {

    private final SchedulingAlgorithm algorithm;
    private final List<ProcessDef> processes;

    public LevelConfig(SchedulingAlgorithm algorithm, List<ProcessDef> processes) {
        this.algorithm = algorithm;
        this.processes = processes;
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
}
