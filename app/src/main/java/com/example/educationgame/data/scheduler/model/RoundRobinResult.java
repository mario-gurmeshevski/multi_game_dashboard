package com.example.educationgame.data.scheduler.model;

import java.util.List;

public class RoundRobinResult {

    private final List<ProcessInfo> order;
    private final int[] startTimes;
    private final int[] endTimes;
    private final int totalSlots;

    public RoundRobinResult(List<ProcessInfo> order, int[] startTimes, int[] endTimes, int totalSlots) {
        this.order = order;
        this.startTimes = startTimes;
        this.endTimes = endTimes;
        this.totalSlots = totalSlots;
    }

    public List<ProcessInfo> getOrder() {
        return order;
    }

    public int[] getStartTimes() {
        return startTimes;
    }

    public int[] getEndTimes() {
        return endTimes;
    }

    public int getTotalSlots() {
        return totalSlots;
    }
}
