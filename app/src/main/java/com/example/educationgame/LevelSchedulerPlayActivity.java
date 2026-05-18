package com.example.educationgame;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educationgame.data.LevelStarConfig;
import com.example.educationgame.data.ProcessColorGenerator;
import com.example.educationgame.data.SchedulerLevels;
import com.example.educationgame.data.enums.GameTypeEnum;
import com.example.educationgame.data.enums.SchedulingAlgorithm;
import com.example.educationgame.data.local.AppDatabase;
import com.example.educationgame.data.local.AppExecutors;
import com.example.educationgame.data.local.entity.LevelProgressEntity;
import com.example.educationgame.data.model.LevelConfig;
import com.example.educationgame.data.model.ProcessDef;
import com.example.educationgame.data.model.ProcessInfo;
import com.example.educationgame.scheduler.GanttChartView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class LevelSchedulerPlayActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL_NUMBER = "level_number";
    public static final String EXTRA_LEVEL_ID = "level_id";
    public static final String EXTRA_GAME_TYPE = "game_type";

    private static final String DRAG_TAG_PROCESS = "process_";
    private int levelNumber;
    private int levelId;
    private GameTypeEnum gameType;
    private SchedulingAlgorithm algorithm;

    private GanttChartView ganttChart;
    private TextView timerText;
    private TextView errorText;

    private MaterialButton submitButton;

    private LinearLayout poolContainer;
    private LinearLayout slotsContainer;

    private final List<ProcessInfo> allProcesses = new ArrayList<>();
    private ProcessInfo[] slotProcesses;
    private int totalSlots;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTimeMs;
    private boolean timerRunning = false;
    private boolean completed = false;
    private float dragStartX, dragStartY;
    private boolean isDragging = false;
    private int touchSlop;

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
        setupGame(config);
    }

    private void setupGame(LevelConfig config) {
        LinearLayout gameContainer = findViewById(R.id.gameContainer);
        gameContainer.setVisibility(View.VISIBLE);

        timerText = findViewById(R.id.timerText);
        errorText = findViewById(R.id.errorText);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> onSubmit());
        poolContainer = findViewById(R.id.poolContainer);
        slotsContainer = findViewById(R.id.slotsContainer);
        TextView instructionText = findViewById(R.id.instructionText);

        instructionText.setText(getInstructionText(algorithm));

        ganttChart = findViewById(R.id.ganttChart);

        int[] colors = ProcessColorGenerator.generate(this, config.getProcessCount());
        List<ProcessDef> defs = config.getProcesses();
        for (int i = 0; i < defs.size(); i++) {
            ProcessDef d = defs.get(i);
            allProcesses.add(new ProcessInfo(d.getName(), d.getArrivalTime(), d.getBurstTime(), d.getPriority(), colors[i]));
        }

        totalSlots = allProcesses.size();
        slotProcesses = new ProcessInfo[totalSlots];
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        List<ProcessInfo> shuffled = new ArrayList<>(allProcesses);
        Collections.shuffle(shuffled);

        buildTargetSlots();
        buildPoolSquares(shuffled);
        setupDropZones();
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
            default:
                return getString(R.string.fcfs_instruction);
        }
    }

    private void buildTargetSlots() {
        slotsContainer.removeAllViews();
        for (int i = 0; i < totalSlots; i++) {
            View slotView = LayoutInflater.from(this).inflate(R.layout.item_target_slot, slotsContainer, false);
            TextView slotLabel = slotView.findViewById(R.id.slotLabel);
            slotLabel.setText(getOrdinal(i));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);
            lp.setMargins(margin, 0, margin, 0);
            slotView.setLayoutParams(lp);
            slotView.setTag(R.id.tag_slot_index, i);
            slotsContainer.addView(slotView);
        }
    }

    private String getOrdinal(int index) {
        int n = index + 1;
        if (n == 1) return "1st";
        if (n == 2) return "2nd";
        if (n == 3) return "3rd";
        return n + "th";
    }

    private void buildPoolSquares(List<ProcessInfo> processes) {
        poolContainer.removeAllViews();
        for (int i = 0; i < processes.size(); i++) {
            View square = createProcessSquare(processes.get(i), i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);
            lp.setMargins(margin, 0, margin, 0);
            square.setLayoutParams(lp);
            poolContainer.addView(square);
        }
    }

    private View createProcessSquare(ProcessInfo process, int index) {
        View square = LayoutInflater.from(this).inflate(R.layout.item_process_square, (ViewGroup) null, false);
        bindProcessSquare(square, process);
        square.setTag(R.id.tag_process_info, process);
        square.setTag(R.id.tag_pool_index, index);
        square.setOnTouchListener((v, event) -> {
            if (completed) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragStartX = event.getRawX();
                    dragStartY = event.getRawY();
                    isDragging = false;
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).start();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) {
                        float dx = Math.abs(event.getRawX() - dragStartX);
                        float dy = Math.abs(event.getRawY() - dragStartY);
                        if (dx > touchSlop / 2f || dy > touchSlop / 2f) {
                            isDragging = true;
                            v.animate().scaleX(1f).scaleY(1f).setDuration(0).start();
                            String dragData = DRAG_TAG_PROCESS + index;
                            ClipData clip = ClipData.newPlainText(dragData, dragData);
                            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                            v.startDragAndDrop(clip, shadow, v, 0);
                            v.setVisibility(View.INVISIBLE);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!isDragging) {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    }
                    isDragging = false;
                    return false;
            }
            return false;
        });
        return square;
    }

    private void bindProcessSquare(View square, ProcessInfo process) {
        TextView name = square.findViewById(R.id.processName);
        TextView arrival = square.findViewById(R.id.processArrival);
        TextView burst = square.findViewById(R.id.processBurst);
        View colorBar = square.findViewById(R.id.processColorBar);
        TextView priority = square.findViewById(R.id.processPriority);

        name.setText(process.getName());
        arrival.setText(getString(R.string.process_arrival, process.getArrivalTime()));
        burst.setText(getString(R.string.process_burst, process.getBurstTime()));
        colorBar.setBackgroundColor(process.getColor());

        if (algorithm == SchedulingAlgorithm.PRIORITY) {
            priority.setVisibility(View.VISIBLE);
            priority.setText(getString(R.string.process_priority, process.getPriority()));
        } else {
            priority.setVisibility(View.GONE);
        }
    }

    private void setupDropZones() {
        for (int i = 0; i < slotsContainer.getChildCount(); i++) {
            View slotView = slotsContainer.getChildAt(i);
            int slotIndex = i;
            slotView.setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        if (!completed) {
                            v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).start();
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        return true;
                    case DragEvent.ACTION_DROP:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        if (completed) return true;
                        handleDropOnSlot(slotIndex, event);
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        View dragView = (View) event.getLocalState();
                        if (dragView != null && dragView.getVisibility() != View.VISIBLE) {
                            dragView.setVisibility(View.VISIBLE);
                        }
                        return true;
                }
                return true;
            });
        }

        HorizontalScrollView poolScrollView = findViewById(R.id.poolScrollView);
        poolScrollView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    if (completed) return true;
                    handleDropOnPool(event);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    View dragView = (View) event.getLocalState();
                    if (dragView != null && dragView.getVisibility() != View.VISIBLE) {
                        dragView.setVisibility(View.VISIBLE);
                    }
                    return true;
            }
            return true;
        });
    }

    private void handleDropOnSlot(int slotIndex, DragEvent event) {
        View dragView = (View) event.getLocalState();
        if (dragView == null) return;

        ProcessInfo draggedProcess = (ProcessInfo) dragView.getTag(R.id.tag_process_info);
        if (draggedProcess == null) return;

        Integer poolIndex = (Integer) dragView.getTag(R.id.tag_pool_index);
        Integer fromSlot = (Integer) dragView.getTag(R.id.tag_slot_index);

        if (fromSlot != null) {
            ProcessInfo existingInTarget = slotProcesses[slotIndex];
            slotProcesses[slotIndex] = draggedProcess;
            slotProcesses[fromSlot] = existingInTarget;

            if (existingInTarget != null) {
                putProcessInSlot(fromSlot, existingInTarget);
            } else {
                clearSlot(fromSlot);
            }
            putProcessInSlot(slotIndex, draggedProcess);
            dragView.setVisibility(View.GONE);
        } else if (poolIndex != null) {
            ProcessInfo existingInTarget = slotProcesses[slotIndex];
            slotProcesses[slotIndex] = draggedProcess;

            if (existingInTarget != null) {
                returnProcessToPool(existingInTarget);
            }

            poolContainer.removeView(dragView);
            putProcessInSlot(slotIndex, draggedProcess);
        }

        updateGanttAndStats();
        errorText.setVisibility(View.GONE);
        checkAllSlotsFilled();
    }

    private void handleDropOnPool(DragEvent event) {
        View dragView = (View) event.getLocalState();
        if (dragView == null) return;

        Integer fromSlot = (Integer) dragView.getTag(R.id.tag_slot_index);
        if (fromSlot == null) {
            dragView.setVisibility(View.VISIBLE);
            return;
        }

        ProcessInfo process = slotProcesses[fromSlot];
        if (process == null) {
            dragView.setVisibility(View.VISIBLE);
            return;
        }

        slotProcesses[fromSlot] = null;
        clearSlot(fromSlot);
        returnProcessToPool(process);
        submitButton.setVisibility(View.GONE);

        updateGanttAndStats();
        errorText.setVisibility(View.GONE);
    }

    private void putProcessInSlot(int slotIndex, ProcessInfo process) {
        ViewGroup slotView = (ViewGroup) slotsContainer.getChildAt(slotIndex);
        if (slotView == null) return;

        TextView slotLabel = slotView.findViewById(R.id.slotLabel);
        slotLabel.setVisibility(View.GONE);

        View existingSquare = slotView.findViewWithTag("placed_square");
        if (existingSquare != null) {
            slotView.removeView(existingSquare);
        }

        View square = LayoutInflater.from(this).inflate(R.layout.item_process_square, (ViewGroup) null, false);
        bindProcessSquare(square, process);
        square.setTag("placed_square");
        square.setTag(R.id.tag_process_info, process);
        square.setTag(R.id.tag_slot_index, slotIndex);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        square.setLayoutParams(lp);
        square.setOnTouchListener((v, event) -> {
            if (completed) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dragStartX = event.getRawX();
                    dragStartY = event.getRawY();
                    isDragging = false;
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).start();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!isDragging) {
                        float dx = Math.abs(event.getRawX() - dragStartX);
                        float dy = Math.abs(event.getRawY() - dragStartY);
                        if (dx > touchSlop / 2f || dy > touchSlop / 2f) {
                            isDragging = true;
                            v.animate().scaleX(1f).scaleY(1f).setDuration(0).start();
                            String dragData = DRAG_TAG_PROCESS + "_slot_" + slotIndex;
                            ClipData clip = ClipData.newPlainText(dragData, dragData);
                            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                            v.startDragAndDrop(clip, shadow, v, 0);
                            v.setVisibility(View.INVISIBLE);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!isDragging) {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    }
                    isDragging = false;
                    return false;
            }
            return false;
        });

        slotView.addView(square);
    }

    private void clearSlot(int slotIndex) {
        ViewGroup slotView = (ViewGroup) slotsContainer.getChildAt(slotIndex);
        if (slotView == null) return;

        View placedSquare = slotView.findViewWithTag("placed_square");
        if (placedSquare != null) {
            slotView.removeView(placedSquare);
        }
        TextView slotLabel = slotView.findViewById(R.id.slotLabel);
        slotLabel.setVisibility(View.VISIBLE);
    }

    private void returnProcessToPool(ProcessInfo process) {
        View square = createProcessSquare(process, poolContainer.getChildCount());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        int margin = (int) (4 * getResources().getDisplayMetrics().density);
        lp.setMargins(margin, 0, margin, 0);
        square.setLayoutParams(lp);
        poolContainer.addView(square);
    }

    private void checkAllSlotsFilled() {
        for (int i = 0; i < totalSlots; i++) {
            if (slotProcesses[i] == null) {
                submitButton.setVisibility(View.GONE);
                return;
            }
        }

        if (!isOrderCorrect()) {
            errorText.setText(getWrongOrderMessage());
            errorText.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.GONE);
            return;
        }

        errorText.setVisibility(View.GONE);
        submitButton.setVisibility(View.VISIBLE);
    }

    private boolean isOrderCorrect() {
        List<ProcessInfo> correctOrder = computeCorrectOrder();
        for (int i = 0; i < totalSlots; i++) {
            if (slotProcesses[i] != correctOrder.get(i)) {
                return false;
            }
        }
        return true;
    }

    private List<ProcessInfo> computeCorrectOrder() {
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

    private String getWrongOrderMessage() {
        switch (algorithm) {
            case SJF:
                return getString(R.string.wrong_order_sjf);
            case LJF:
                return getString(R.string.wrong_order_ljf);
            case PRIORITY:
                return getString(R.string.wrong_order_priority);
            default:
                return getString(R.string.wrong_order);
        }
    }

    private void onSubmit() {
        if (completed) return;
        completed = true;
        timerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        submitButton.setVisibility(View.GONE);

        long elapsedSec = (System.currentTimeMillis() - startTimeMs) / 1000;
        int seconds = (int) elapsedSec;
        int stars = LevelStarConfig.getStars(gameType, levelNumber, seconds);
        saveProgress(stars, seconds);
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
        int currentTime = 0;

        for (int i = 0; i < n; i++) {
            ProcessInfo p = ordered.get(i);
            startTimes[i] = Math.max(currentTime, p.getArrivalTime());
            endTimes[i] = startTimes[i] + p.getBurstTime();
            currentTime = endTimes[i];
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

            runOnUiThread(() -> {
                Intent result = new Intent();
                setResult(RESULT_OK, result);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }
}
