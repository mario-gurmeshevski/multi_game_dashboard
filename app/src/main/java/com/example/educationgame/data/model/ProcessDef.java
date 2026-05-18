package com.example.educationgame.data.model;

public class ProcessDef {

    private final String name;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;

    public ProcessDef(String name, int arrivalTime, int burstTime) {
        this(name, arrivalTime, burstTime, 0);
    }

    public ProcessDef(String name, int arrivalTime, int burstTime, int priority) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }
}
