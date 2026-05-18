package com.example.educationgame.data.local;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {

    private static volatile AppExecutors INSTANCE;

    private final ExecutorService diskIO;

    private AppExecutors() {
        diskIO = Executors.newSingleThreadExecutor();
    }

    public static AppExecutors getInstance() {
        if (INSTANCE == null) {
            synchronized (AppExecutors.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppExecutors();
                }
            }
        }
        return INSTANCE;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }
}
