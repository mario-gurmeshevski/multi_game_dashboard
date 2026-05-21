package com.example.educationgame.scheduler;

import com.example.educationgame.data.enums.SchedulingAlgorithm;
import com.example.educationgame.data.scheduler.model.ProcessInfo;
import com.example.educationgame.data.scheduler.model.RoundRobinResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SchedulerSolver {

    public static List<ProcessInfo> computeCorrectOrder(List<ProcessInfo> allProcesses, SchedulingAlgorithm algorithm) {
        List<ProcessInfo> remaining = new ArrayList<>(allProcesses);
        List<ProcessInfo> result = new ArrayList<>();
        int currentTime = 0;

        while (!remaining.isEmpty()) {
            final int time = currentTime;
            List<ProcessInfo> arrived = new ArrayList<>();
            for (ProcessInfo p : remaining) {
                if (p.getArrivalTime() <= time) {
                    arrived.add(p);
                }
            }

            if (arrived.isEmpty()) {
                int minArrival = Integer.MAX_VALUE;
                for (ProcessInfo p : remaining) {
                    minArrival = Math.min(minArrival, p.getArrivalTime());
                }
                currentTime = minArrival;
                continue;
            }

            ProcessInfo selected;
            switch (algorithm) {
                case SJF:
                    selected = Collections.min(arrived, Comparator.comparingInt(ProcessInfo::getBurstTime).thenComparingInt(ProcessInfo::getArrivalTime));
                    break;
                case LJF:
                    selected = Collections.max(arrived, Comparator.comparingInt(ProcessInfo::getBurstTime).thenComparingInt(ProcessInfo::getArrivalTime));
                    break;
                case PRIORITY:
                    selected = Collections.min(arrived, Comparator.comparingInt(ProcessInfo::getPriority).thenComparingInt(ProcessInfo::getArrivalTime));
                    break;
                default:
                    selected = Collections.min(arrived, Comparator.comparingInt(ProcessInfo::getArrivalTime).thenComparingInt(ProcessInfo::getBurstTime));
                    break;
            }

            result.add(selected);
            remaining.remove(selected);
            currentTime += selected.getBurstTime();
        }

        return result;
    }

    public static RoundRobinResult computeRoundRobinSchedule(List<ProcessInfo> allProcesses, int timeQuantum) {
        int n = allProcesses.size();
        int[] remainingBurst = new int[n];
        for (int i = 0; i < n; i++) {
            remainingBurst[i] = allProcesses.get(i).getBurstTime();
        }

        List<Integer> indicesByArrival = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            indicesByArrival.add(i);
        }
        Collections.sort(indicesByArrival, (a, b) ->
                Integer.compare(allProcesses.get(a).getArrivalTime(), allProcesses.get(b).getArrivalTime()));

        LinkedList<Integer> readyQueue = new LinkedList<>();
        List<ProcessInfo> schedule = new ArrayList<>();
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        boolean[] enqueued = new boolean[n];

        int currentTime = 0;

        for (int idx : indicesByArrival) {
            if (allProcesses.get(idx).getArrivalTime() <= currentTime && remainingBurst[idx] > 0) {
                readyQueue.add(idx);
                enqueued[idx] = true;
            }
        }

        while (true) {
            if (readyQueue.isEmpty()) {
                boolean anyRemaining = false;
                for (int i = 0; i < n; i++) {
                    if (remainingBurst[i] > 0) {
                        anyRemaining = true;
                        break;
                    }
                }
                if (!anyRemaining) break;

                int minArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (remainingBurst[i] > 0 && !enqueued[i]) {
                        minArrival = Math.min(minArrival, allProcesses.get(i).getArrivalTime());
                    }
                }
                if (minArrival == Integer.MAX_VALUE) break;
                currentTime = minArrival;

                for (int idx : indicesByArrival) {
                    if (!enqueued[idx] && remainingBurst[idx] > 0 &&
                            allProcesses.get(idx).getArrivalTime() <= currentTime) {
                        readyQueue.add(idx);
                        enqueued[idx] = true;
                    }
                }
                continue;
            }

            int processIdx = readyQueue.poll();
            ProcessInfo process = allProcesses.get(processIdx);
            int runTime = Math.min(timeQuantum, remainingBurst[processIdx]);
            int startTime = currentTime;
            int endTime = currentTime + runTime;

            schedule.add(process);
            starts.add(startTime);
            ends.add(endTime);

            remainingBurst[processIdx] -= runTime;
            currentTime = endTime;

            for (int idx : indicesByArrival) {
                if (!enqueued[idx] && remainingBurst[idx] > 0 &&
                        allProcesses.get(idx).getArrivalTime() <= currentTime) {
                    readyQueue.add(idx);
                    enqueued[idx] = true;
                }
            }

            if (remainingBurst[processIdx] > 0) {
                readyQueue.add(processIdx);
            }
        }

        int[] startTimes = new int[starts.size()];
        int[] endTimes = new int[ends.size()];
        for (int i = 0; i < starts.size(); i++) {
            startTimes[i] = starts.get(i);
            endTimes[i] = ends.get(i);
        }

        return new RoundRobinResult(schedule, startTimes, endTimes, schedule.size());
    }
}
