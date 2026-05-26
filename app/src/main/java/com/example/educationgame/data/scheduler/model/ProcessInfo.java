package com.example.educationgame.data.scheduler.model;

public class ProcessInfo {

    private final String name;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;
    private final int color;
    private int remainingBurstTime;

    public ProcessInfo(String name, int arrivalTime, int burstTime, int priority, int color) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.color = color;
        this.remainingBurstTime = burstTime;
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

    public int getRemainingBurstTime() {
        return remainingBurstTime;
    }

    public void reduceBurst(int amount) {
        remainingBurstTime -= amount;
        if (remainingBurstTime < 0) remainingBurstTime = 0;
    }

    public void restoreBurst(int amount) {
        remainingBurstTime += amount;
        if (remainingBurstTime > burstTime) remainingBurstTime = burstTime;
    }


}
