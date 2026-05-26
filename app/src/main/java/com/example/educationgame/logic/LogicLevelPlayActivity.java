package com.example.educationgame.logic;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educationgame.R;
import com.example.educationgame.data.local.AppDatabase;
import com.example.educationgame.data.local.AppExecutors;
import com.example.educationgame.data.local.entity.LevelProgressEntity;

import java.util.Date;

public class LogicLevelPlayActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL_NUMBER = "extra_level_number";
    public static final String EXTRA_LEVEL_ID     = "extra_level_id";

    // UI
    private TextView timerText;
    private TextView star1, star2, star3;
    private TextView instructionText;
    private TextView levelTitle;
    private Button checkButton;
    private Button hintButton;
    private HorizontalScrollView componentToolbar;
    private LogicGameView gameView;
    private LogicWireView wireView;
    private LogicBuildView buildView;

    // Timer
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTimeMs;
    private boolean timerRunning = false;
    private boolean completed    = false;
    private long elapsedSeconds  = 0;

    // Hint
    private PopupWindow hintPopup;
    private boolean hintUnlocked = false;
    private static final int HINT_UNLOCK_SECONDS = 30;

    // Level
    private LogicLevelConfig currentLevel;
    private ComponentState   componentState;
    private int levelId;
    private AppDatabase db;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!timerRunning) return;
            elapsedSeconds = (System.currentTimeMillis() - startTimeMs) / 1000;
            int minutes = (int) (elapsedSeconds / 60);
            int seconds = (int) (elapsedSeconds % 60);
            timerText.setText(getString(R.string.timer_format, minutes, seconds));
            updateStars((int) elapsedSeconds);

            if (!hintUnlocked && elapsedSeconds >= HINT_UNLOCK_SECONDS) {
                hintUnlocked = true;
                hintButton.setEnabled(true);
                hintButton.setAlpha(1.0f);
            }

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logic_play);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);

        int levelNumber = getIntent().getIntExtra(EXTRA_LEVEL_NUMBER, 1);
        levelId         = getIntent().getIntExtra(EXTRA_LEVEL_ID, -1);

        bindViews();
        setupLevel(levelNumber);
    }

    private void bindViews() {
        timerText        = findViewById(R.id.timerText);
        star1            = findViewById(R.id.star1);
        star2            = findViewById(R.id.star2);
        star3            = findViewById(R.id.star3);
        instructionText  = findViewById(R.id.instructionText);
        levelTitle       = findViewById(R.id.levelTitle);
        checkButton      = findViewById(R.id.checkButton);
        hintButton       = findViewById(R.id.hintButton);
        componentToolbar = findViewById(R.id.componentToolbar);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button taskInfoButton = findViewById(R.id.taskInfoButton);
        taskInfoButton.setText("?");

        FrameLayout canvas = findViewById(R.id.gameCanvas);
        int levelNumber    = getIntent().getIntExtra(EXTRA_LEVEL_NUMBER, 1);

        if (levelNumber >= 7) {
            taskInfoButton.setVisibility(View.VISIBLE);
            taskInfoButton.setOnClickListener(v -> {
                LevelTaskDialog.show(this, levelNumber, () -> {});
            });
        }

        if (levelNumber <= 3) {
            gameView = new LogicGameView(this);
            canvas.addView(gameView);
        } else if (levelNumber <= 6) {
            wireView = new LogicWireView(this);
            canvas.addView(wireView);
        } else {
            buildView = new LogicBuildView(this);
            canvas.addView(buildView);
            componentToolbar.setVisibility(View.VISIBLE);
            setupToolbarChips();
        }
    }
    private void setupToolbarChips() {
        LinearLayout chipButton = findViewById(R.id.chipButton);
        LinearLayout chipAnd    = findViewById(R.id.chipAnd);
        LinearLayout chipOr     = findViewById(R.id.chipOr);
        LinearLayout chipNot    = findViewById(R.id.chipNot);

        chipButton.setOnClickListener(v -> buildView.addComponentToCanvas("BUTTON"));
        chipAnd.setOnClickListener(v    -> buildView.addComponentToCanvas("AND"));
        chipOr.setOnClickListener(v     -> buildView.addComponentToCanvas("OR"));
        chipNot.setOnClickListener(v    -> buildView.addComponentToCanvas("NOT"));
        Button deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setOnClickListener(v -> {
            buildView.deleteSelectedComponent();
        });
    }

    private void setupLevel(int levelNumber) {
        currentLevel = LogicLevels.getLevel(levelNumber);
        if (currentLevel == null) return;

        levelTitle.setText(getString(R.string.level_title, levelNumber));
        instructionText.setText("");
        componentState = new ComponentState(currentLevel.getGateType());

        if (levelNumber <= 3) {
            if (currentLevel.getGateType() == LogicEngine.GateType.NOT) {
                componentState.setInputB(true);
            }
            gameView.setGateType(currentLevel.getGateType());
            gameView.setOnInputChangedListener(new LogicGameView.OnInputChangedListener() {
                @Override
                public void onInputAChanged(boolean value) {
                    componentState.setInputA(value);
                    checkButton.setEnabled(true);
                }
                @Override
                public void onInputBChanged(boolean value) {
                    componentState.setInputB(value);
                    checkButton.setEnabled(true);
                }
            });
        } else if (levelNumber <= 6) {
            wireView.setupLevel(levelNumber);
            wireView.setOnCircuitChangedListener(bulbOn -> {
                checkButton.setEnabled(true);
            });
            checkButton.setEnabled(true);
        } else {
            // Нивоа 7-9 — прикажи popup прво
            buildView.setupLevel(levelNumber);
            checkButton.setEnabled(true);
            showTaskPopup(levelNumber);
            return; // Тајмерот почнува дури по popup
        }

        checkButton.setEnabled(false);
        checkButton.setOnClickListener(v -> onCheckPressed());
        hintButton.setOnClickListener(v -> onHintPressed());

        startTimer();
    }

    private void showTaskPopup(int levelNumber) {
        checkButton.setOnClickListener(v -> onCheckPressed());
        hintButton.setOnClickListener(v -> onHintPressed());

        LevelTaskDialog.show(this, levelNumber, () -> {
            // Popup исчезна — почни тајмер
            startTimer();
        });
    }

    // ── Тајмер ───────────────────────────────────────────────

    private void startTimer() {
        startTimeMs  = System.currentTimeMillis();
        timerRunning = true;
        hintUnlocked = false;
        hintButton.setAlpha(0.4f);
        hintButton.setEnabled(false);
        timerHandler.post(timerRunnable);
    }

    private void updateStars(int elapsed) {
        int three = currentLevel.getThreeStarSeconds();
        int two   = currentLevel.getTwoStarSeconds();

        if (elapsed < three) {
            setStarColors(0xFFFFD700, 0xFFFFD700, 0xFFFFD700);
            timerText.setTextColor(0xFFFFFFFF);
        } else if (elapsed < two) {
            setStarColors(0xFFFFD700, 0xFFFFD700, 0xFF444466);
            timerText.setTextColor(0xFFF6C90E);
        } else {
            setStarColors(0xFFFFD700, 0xFF444466, 0xFF444466);
            timerText.setTextColor(0xFFE05A5A);
        }
    }

    private void setStarColors(int c1, int c2, int c3) {
        star1.setTextColor(c1);
        star2.setTextColor(c2);
        star3.setTextColor(c3);
    }

    // ── Gameplay ─────────────────────────────────────────────

    private void onCheckPressed() {
        if (completed) return;

        int levelNumber = getIntent().getIntExtra(EXTRA_LEVEL_NUMBER, 1);

        if (levelNumber <= 3) {
            boolean output = gameView.getOutput();
            if (output) {
                completeLevel();
            } else {
                Toast.makeText(this, "Not quite! Check your inputs.", Toast.LENGTH_SHORT).show();
            }
        } else if (levelNumber <= 6) {
            String gateError = wireView.getUnconnectedGateMessage();
            if (gateError != null) {
                Toast.makeText(this, gateError, Toast.LENGTH_LONG).show();
                return;
            }
            boolean output = wireView.getBulbState();
            if (output) {
                completeLevel();
            } else {
                Toast.makeText(this, "Wrong connections! The bulb is not ON.", Toast.LENGTH_LONG).show();
            }
        } else {
            String error = buildView.validate();
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                return;
            }
            completeLevel();
        }
    }

    private void completeLevel() {
        completed    = true;
        timerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        int stars = getStars((int) elapsedSeconds);
        saveProgress(stars);
    }

    private void onHintPressed() {
        if (!hintUnlocked) return;
        int levelNumber = getIntent().getIntExtra(EXTRA_LEVEL_NUMBER, 1);
        String hintText;

        if (levelNumber <= 3) {
            switch (currentLevel.getGateType()) {
                case AND: hintText = "AND: Both inputs must be TRUE!"; break;
                case OR:  hintText = "OR: At least one input must be TRUE!"; break;
                case NOT: hintText = "NOT: Button A must be FALSE!"; break;
                default:  hintText = "Think carefully!";
            }
        } else if (levelNumber <= 6) {
            switch (levelNumber) {
                case 4: hintText = "Connect both buttons to AND, then AND→NOT→OR→NOT2→Bulb!"; break;
                case 5: hintText = "Use OR first, then NOT, then AND gates to reach the Bulb!"; break;
                case 6: hintText = "Pass buttons through NOT gates first, then AND, then OR→AND→Bulb!"; break;
                default: hintText = "Think carefully!";
            }
        } else {
            switch (levelNumber) {
                case 7: hintText = "Try: Button→AND→NOT→AND2→Bulb. Use 4 gates total!"; break;
                case 8: hintText = "Try: 2 Buttons→AND→NOT, another Button→OR→AND→NOT2→AND3→Bulb!"; break;
                case 9: hintText = "Use 2 AND, 2 NOT, 1 OR minimum. Chain them carefully!"; break;
                default: hintText = "Think carefully!";
            }
        }
        showHintPopup(hintText);
    }

    private void showHintPopup(String text) {
        if (hintPopup != null && hintPopup.isShowing()) {
            hintPopup.dismiss();
            return;
        }

        View popupView = LayoutInflater.from(this).inflate(R.layout.tooltip_hint, null);
        TextView tooltipText = popupView.findViewById(R.id.tooltipText);
        tooltipText.setText(text);

        hintPopup = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        hintPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        hintPopup.setElevation(10f);
        hintPopup.showAsDropDown(hintButton, 0, -280);

        timerHandler.postDelayed(() -> {
            if (hintPopup != null && hintPopup.isShowing()) hintPopup.dismiss();
        }, 3000);
    }

    // ── Зачувување ───────────────────────────────────────────

    private void saveProgress(int stars) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            LevelProgressEntity progress = new LevelProgressEntity();
            progress.setLevelId(levelId);
            progress.setScore(stars);
            progress.setFinished(true);
            progress.setCompletionTime((int) elapsedSeconds);
            progress.setFinishedAt(new Date());
            db.levelProgressDao().insert(progress);
            runOnUiThread(() -> showCompleteDialog(stars));
        });
    }

    private void showCompleteDialog(int stars) {
        int levelNumber = getIntent().getIntExtra(EXTRA_LEVEL_NUMBER, 1);
        LevelCompleteDialog.show(this, stars, levelNumber,
                LogicLevels.getLevelCount(),
                new LevelCompleteDialog.OnDialogActionListener() {
                    @Override
                    public void onNextLevel() {
                        int next = levelNumber + 1;
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            int nextLevelId = -1;
                            try {
                                java.util.List<com.example.educationgame.data.local.entity.GameEntity> games =
                                        db.gameDao().getGamesByType(
                                                com.example.educationgame.data.enums.GameTypeEnum.LOGIC);
                                if (!games.isEmpty()) {
                                    int gameId = games.get(0).getId();
                                    java.util.List<com.example.educationgame.data.local.entity.LevelEntity> levels =
                                            db.levelDao().getLevelsByGameId(gameId);
                                    for (com.example.educationgame.data.local.entity.LevelEntity level : levels) {
                                        if (level.getLevelNumber() == next) {
                                            nextLevelId = level.getId();
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) { e.printStackTrace(); }

                            final int finalNextLevelId = nextLevelId;
                            runOnUiThread(() -> {
                                Intent intent = new Intent(LogicLevelPlayActivity.this,
                                        LogicLevelPlayActivity.class);
                                intent.putExtra(EXTRA_LEVEL_NUMBER, next);
                                intent.putExtra(EXTRA_LEVEL_ID, finalNextLevelId);
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

    private int getStars(int seconds) {
        if (seconds <= currentLevel.getThreeStarSeconds()) return 3;
        if (seconds <= currentLevel.getTwoStarSeconds())   return 2;
        return 1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }
}