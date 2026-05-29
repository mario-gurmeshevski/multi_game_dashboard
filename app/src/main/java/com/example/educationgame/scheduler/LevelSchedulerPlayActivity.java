package com.example.educationgame.scheduler;

import com.example.educationgame.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educationgame.data.scheduler.LevelStarConfig;
import com.example.educationgame.common.LevelCompleteDialog;
import com.example.educationgame.data.scheduler.ProcessColorGenerator;
import com.example.educationgame.data.scheduler.SchedulerLevels;
import com.example.educationgame.data.enums.GameTypeEnum;
import com.example.educationgame.data.enums.SchedulingAlgorithm;
import com.example.educationgame.data.local.AppDatabase;
import com.example.educationgame.data.local.AppExecutors;
import com.example.educationgame.data.local.entity.LevelProgressEntity;
import com.example.educationgame.data.scheduler.model.LevelConfig;
import com.example.educationgame.data.scheduler.model.ProcessDef;
import com.example.educationgame.data.scheduler.model.ProcessInfo;
import com.example.educationgame.data.scheduler.model.RoundRobinResult;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class LevelSchedulerPlayActivity extends AppCompatActivity implements SchedulerDragController.Callback {

    public static final String EXTRA_LEVEL_NUMBER = "level_number";
    public static final String EXTRA_LEVEL_ID = "level_id";
    public static final String EXTRA_GAME_TYPE = "game_type";

    private int levelNumber;
    private int levelId;
    private GameTypeEnum gameType;
    private SchedulingAlgorithm algorithm;
    private boolean isRoundRobin;
    private int timeQuantum;

    private GanttChartView ganttChart;
    private TextView timerText;
    private MaterialButton submitButton;

    private final List<ProcessInfo> allProcesses = new ArrayList<>();
    private ProcessInfo[] slotProcesses;
    private int totalSlots;

    private List<ProcessInfo> correctOrder;
    private int[] correctStartTimes;
    private int[] correctEndTimes;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTimeMs;
    private boolean timerRunning = false;
    private boolean completed = false;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!timerRunning) return;
            long elapsed = (System.currentTimeMillis() - startTimeMs) / 1000;
            int minutes = (int) (elapsed / 60);
            int seconds = (int) (elapsed % 60);
            timerText.setText(getString(R.string.timer_format, minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level_scheduler_play);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        levelNumber = getIntent().getIntExtra(EXTRA_LEVEL_NUMBER, 1);
        levelId = getIntent().getIntExtra(EXTRA_LEVEL_ID, -1);
        String gameTypeStr = getIntent().getStringExtra(EXTRA_GAME_TYPE);
        gameType = GameTypeEnum.valueOf(gameTypeStr != null ? gameTypeStr : "SCHEDULER");

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        TextView levelTitle = findViewById(R.id.levelTitle);
        levelTitle.setText(getString(R.string.level_title, levelNumber));

        LevelConfig config = SchedulerLevels.getLevel(levelNumber);
        if (config == null) {
            finish();
            return;
        }

        algorithm = config.getAlgorithm();
        isRoundRobin = algorithm == SchedulingAlgorithm.ROUND_ROBIN;
        timeQuantum = config.getTimeQuantum();
        setupGame(config);
    }

    private void setupGame(LevelConfig config) {
        LinearLayout gameContainer = findViewById(R.id.gameContainer);
        gameContainer.setVisibility(View.VISIBLE);

        timerText = findViewById(R.id.timerText);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> onSubmit());
        LinearLayout poolContainer = findViewById(R.id.poolContainer);
        LinearLayout slotsContainer = findViewById(R.id.slotsContainer);
        TextView instructionText = findViewById(R.id.instructionText);

        instructionText.setText(getInstructionText(algorithm));

        ganttChart = findViewById(R.id.ganttChart);

        int[] colors = ProcessColorGenerator.generate(this, config.getProcessCount());
        List<ProcessDef> defs = config.getProcesses();
        for (int i = 0; i < defs.size(); i++) {
            ProcessDef d = defs.get(i);
            allProcesses.add(new ProcessInfo(d.getName(), d.getArrivalTime(), d.getBurstTime(), d.getPriority(), colors[i]));
        }

        if (isRoundRobin) {
            RoundRobinResult rrResult = SchedulerSolver.computeRoundRobinSchedule(allProcesses, timeQuantum);
            correctOrder = rrResult.getOrder();
            correctStartTimes = rrResult.getStartTimes();
            correctEndTimes = rrResult.getEndTimes();
            totalSlots = rrResult.getTotalSlots();
        } else {
            correctOrder = SchedulerSolver.computeCorrectOrder(allProcesses, algorithm);
            totalSlots = allProcesses.size();
            correctStartTimes = new int[0];
            correctEndTimes = new int[0];
        }

        slotProcesses = new ProcessInfo[totalSlots];

        ProcessSquareFactory viewFactory = new ProcessSquareFactory(this, algorithm);
        SchedulerDragController dragController = new SchedulerDragController(
                slotsContainer, poolContainer, viewFactory, isRoundRobin,
                slotProcesses, correctStartTimes, correctEndTimes,
                android.view.ViewConfiguration.get(this).getScaledTouchSlop(),
                timeQuantum, this
        );

        List<ProcessInfo> shuffled = new ArrayList<>(allProcesses);
        Collections.shuffle(shuffled);

        viewFactory.buildTargetSlots(slotsContainer, totalSlots, isRoundRobin, correctStartTimes, correctEndTimes);
        viewFactory.buildPoolSquares(poolContainer, dragController, shuffled);
        dragController.setupDropZones(findViewById(R.id.poolScrollView));
        updateGanttAndStats();

        startTimeMs = System.currentTimeMillis();
        timerRunning = true;
        timerHandler.post(timerRunnable);
    }

    private String getInstructionText(SchedulingAlgorithm alg) {
        switch (alg) {
            case SJF:
                return getString(R.string.sjf_instruction);
            case LJF:
                return getString(R.string.ljf_instruction);
            case PRIORITY:
                return getString(R.string.priority_instruction);
            case ROUND_ROBIN:
                return getString(R.string.rr_instruction, timeQuantum);
            default:
                return getString(R.string.fcfs_instruction);
        }
    }

    @Override
    public void onSlotDropCompleted() {
        updateGanttAndStats();
    }

    @Override
    public void onReturnToPool() {
        updateGanttAndStats();
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void onSubmit() {
        if (completed) return;

        for (int i = 0; i < totalSlots; i++) {
            if (slotProcesses[i] == null) {
                showToast("Fill all slots first!");
                return;
            }
        }

        if (!isOrderCorrect()) {
            showToast("Wrong order!");
            return;
        }

        completed = true;
        timerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        submitButton.setVisibility(View.GONE);

        long elapsedSec = (System.currentTimeMillis() - startTimeMs) / 1000;
        int seconds = (int) elapsedSec;
        int stars = LevelStarConfig.getStars(gameType, levelNumber, seconds);
        saveProgress(stars, seconds);
    }

    private boolean isOrderCorrect() {
        for (int i = 0; i < totalSlots; i++) {
            if (slotProcesses[i] != correctOrder.get(i)) {
                return false;
            }
        }
        return true;
    }

    private String getWrongOrderMessage() {
        switch (algorithm) {
            case SJF:
                return getString(R.string.wrong_order_sjf);
            case LJF:
                return getString(R.string.wrong_order_ljf);
            case PRIORITY:
                return getString(R.string.wrong_order_priority);
            case ROUND_ROBIN:
                return getString(R.string.wrong_order_rr);
            default:
                return getString(R.string.wrong_order);
        }
    }

    private void updateGanttAndStats() {
        List<ProcessInfo> ordered = new ArrayList<>();
        for (int i = 0; i < totalSlots; i++) {
            if (slotProcesses[i] != null) {
                ordered.add(slotProcesses[i]);
            }
        }

        if (ordered.isEmpty()) {
            ganttChart.setSchedule(new ArrayList<>(), new int[0], new int[0]);
            return;
        }

        int n = ordered.size();
        int[] startTimes = new int[n];
        int[] endTimes = new int[n];

        if (isRoundRobin) {
            int j = 0;
            for (int i = 0; i < totalSlots; i++) {
                if (slotProcesses[i] != null) {
                    startTimes[j] = correctStartTimes[i];
                    endTimes[j] = correctEndTimes[i];
                    j++;
                }
            }
        } else {
            int currentTime = 0;
            for (int i = 0; i < n; i++) {
                ProcessInfo p = ordered.get(i);
                startTimes[i] = Math.max(currentTime, p.getArrivalTime());
                endTimes[i] = startTimes[i] + p.getBurstTime();
                currentTime = endTimes[i];
            }
        }

        ganttChart.setSchedule(ordered, startTimes, endTimes);
    }

    private void saveProgress(int stars, int seconds) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            LevelProgressEntity progress = new LevelProgressEntity();
            progress.setLevelId(levelId);
            progress.setFinished(true);
            progress.setScore(stars);
            progress.setCompletionTime(seconds);
            progress.setAttempts(1);
            progress.setFinishedAt(new Date());
            AppDatabase.getInstance(this).levelProgressDao().insert(progress);
            runOnUiThread(() -> showCompleteDialog(stars, seconds));
        });
    }

    private void showCompleteDialog(int stars, int seconds) {
        LevelCompleteDialog.show(this, stars, levelNumber,
                SchedulerLevels.getLevelCount(), seconds,
                new LevelCompleteDialog.OnDialogActionListener() {
                    @Override
                    public void onNextLevel() {
                        int next = levelNumber + 1;
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            int nextLevelId = -1;
                            try {
                                java.util.List<com.example.educationgame.data.local.entity.GameEntity> games =
                                        AppDatabase.getInstance(LevelSchedulerPlayActivity.this)
                                                .gameDao().getGamesByType(gameType);
                                if (!games.isEmpty()) {
                                    int gameId = games.get(0).getId();
                                    java.util.List<com.example.educationgame.data.local.entity.LevelEntity> levels =
                                            AppDatabase.getInstance(LevelSchedulerPlayActivity.this)
                                                    .levelDao().getLevelsByGameId(gameId);
                                    for (com.example.educationgame.data.local.entity.LevelEntity level : levels) {
                                        if (level.getLevelNumber() == next) {
                                            nextLevelId = level.getId();
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) { e.printStackTrace(); }

                            int finalNextLevelId = nextLevelId;
                            runOnUiThread(() -> {
                                Intent intent = new Intent(LevelSchedulerPlayActivity.this,
                                        LevelSchedulerPlayActivity.class);
                                intent.putExtra(EXTRA_LEVEL_NUMBER, next);
                                intent.putExtra(EXTRA_LEVEL_ID, finalNextLevelId);
                                intent.putExtra(EXTRA_GAME_TYPE, gameType.name());
                                setResult(RESULT_OK);
                                startActivity(intent);
                                finish();
                            });
                        });
                    }

                    @Override
                    public void onRetry() {
                        setResult(RESULT_OK);
                        recreate();
                    }

                    @Override
                    public void onBack() {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }
}
