package com.example.educationgame.data.model;

public class ProcessInfo {

    private final String name;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;
    private final int color;

    public ProcessInfo(String name, int arrivalTime, int burstTime, int priority, int color) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.color = color;
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

    public int getColor() {
        return color;
    }
}
