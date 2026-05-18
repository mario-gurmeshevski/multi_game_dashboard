package com.example.educationgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educationgame.data.LevelStarConfig;
import com.example.educationgame.data.enums.GameTypeEnum;
import com.example.educationgame.data.local.AppDatabase;
import com.example.educationgame.data.local.AppExecutors;
import com.example.educationgame.data.local.entity.GameEntity;
import com.example.educationgame.data.local.entity.LevelEntity;
import com.example.educationgame.data.local.entity.LevelProgressEntity;

import java.util.List;

public class SchedulerActivity extends AppCompatActivity {

    private static final int REQUEST_LEVEL_PLAY = 100;

    private AppDatabase db;
    private int gameId = -1;
    private int[] levelIds = new int[9];
    private int[] levelStars = new int[9];

    private ProgressBar progressCircle;
    private TextView txtPercent;
    private TextView[] levelStatusViews = new TextView[9];
    private LinearLayout[] levelCards = new LinearLayout[9];
    private ImageView[] sidebarStars = new ImageView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scheduler);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        db = AppDatabase.getInstance(this);
        bindViews();
        seedAndLoad();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LEVEL_PLAY && resultCode == RESULT_OK) {
            seedAndLoad();
        }
    }

    private void bindViews() {
        progressCircle = findViewById(R.id.progressCircle);
        txtPercent = findViewById(R.id.txtPercent);

        sidebarStars[0] = findViewById(R.id.sidebarStar1);
        sidebarStars[1] = findViewById(R.id.sidebarStar2);
        sidebarStars[2] = findViewById(R.id.sidebarStar3);

        for (int i = 0; i < 9; i++) {
            int num = i + 1;
            int cardId = getResources().getIdentifier("levelCard" + num, "id", getPackageName());
            int statusId = getResources().getIdentifier("levelStatus" + num, "id", getPackageName());
            levelCards[i] = findViewById(cardId);
            levelStatusViews[i] = findViewById(statusId);
            levelStars[i] = 0;
        }
    }

    private void seedAndLoad() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            seedDatabase();

            int totalEarned = 0;
            for (int i = 0; i < 9; i++) {
                int bestStars = 0;
                if (levelIds[i] > 0) {
                    LevelProgressEntity best = db.levelProgressDao().getBestProgressByLevelId(levelIds[i]);
                    if (best != null && best.getScore() != null) {
                        bestStars = best.getScore();
                    }
                }
                levelStars[i] = bestStars;
                totalEarned += bestStars;
            }

            int totalPossible = LevelStarConfig.getTotalPossibleStars(GameTypeEnum.SCHEDULER);
            int percent = totalEarned == 0 ? 0 : (int) Math.ceil((double) totalEarned / totalPossible * 100);
            int finalTotalEarned = totalEarned;

            runOnUiThread(() -> updateUI(percent, finalTotalEarned, totalPossible));
        });
    }

    private void seedDatabase() {
        List<GameEntity> existing = db.gameDao().getGamesByType(GameTypeEnum.SCHEDULER);
        if (existing.isEmpty()) {
            GameEntity game = new GameEntity();
            game.setType(GameTypeEnum.SCHEDULER);
            game.setTitle("OS Scheduler");
            game.setDescription("Learn CPU scheduling algorithms");
            gameId = (int) db.gameDao().insert(game);
        } else {
            gameId = existing.get(0).getId();
        }

        List<LevelEntity> levels = db.levelDao().getLevelsByGameId(gameId);
        if (levels.isEmpty()) {
            for (int i = 1; i <= 9; i++) {
                LevelEntity level = new LevelEntity();
                level.setLevelNumber(i);
                level.setName("Level " + i);
                level.setDescription("Scheduler Level " + i);
                level.setGameId(gameId);
                levelIds[i - 1] = (int) db.levelDao().insert(level);
            }
        } else {
            for (LevelEntity level : levels) {
                int idx = level.getLevelNumber() - 1;
                if (idx >= 0 && idx < 9) {
                    levelIds[idx] = level.getId();
                }
            }
        }
    }

    private void updateUI(int percent, int totalEarned, int totalPossible) {
        progressCircle.setProgress(percent);
        txtPercent.setText(percent + "%");
        updateSidebarStars(totalEarned, totalPossible);
        renderLevelCards();
    }

    private void updateSidebarStars(int earned, int total) {
        float ratio = total > 0 ? (float) earned / total : 0f;
        int starsToShow = 0;
        if (ratio >= 0.99f) starsToShow = 3;
        else if (ratio >= 0.6f) starsToShow = 2;
        else if (ratio > 0f) starsToShow = 1;

        int activeColor = getColor(R.color.star_color);
        int inactiveColor = getColor(R.color.locked_text);
        for (int i = 0; i < 3; i++) {
            boolean active = i < starsToShow;
            sidebarStars[i].setAlpha(active ? 1.0f : 0.3f);
            sidebarStars[i].setColorFilter(active ? activeColor : inactiveColor);
        }
    }

    private void renderLevelCards() {
        for (int i = 0; i < 9; i++) {
            boolean unlocked = isLevelUnlocked(i);
            TextView statusView = levelStatusViews[i];

            if (levelStars[i] > 0) {
                statusView.setText(starsToString(levelStars[i]));
                statusView.setTextColor(getColor(R.color.star_color));
                setCardClickListener(i, true);
            } else if (unlocked) {
                statusView.setText("Play");
                statusView.setTextColor(getColor(R.color.game_text));
                setCardClickListener(i, true);
            } else {
                statusView.setText(R.string.level_locked);
                statusView.setTextColor(getColor(R.color.locked_text));
                levelCards[i].setOnClickListener(null);
            }
        }
    }

    private boolean isLevelUnlocked(int index) {
        if (index == 0) return true;
        return levelStars[index - 1] > 0;
    }

    private void setCardClickListener(int index, boolean unlocked) {
        if (!unlocked) {
            levelCards[index].setOnClickListener(null);
            return;
        }
        int levelNum = index + 1;
        levelCards[index].setOnClickListener(v -> {
            Intent intent = new Intent(SchedulerActivity.this, LevelSchedulerPlayActivity.class);
            intent.putExtra(LevelSchedulerPlayActivity.EXTRA_LEVEL_NUMBER, levelNum);
            intent.putExtra(LevelSchedulerPlayActivity.EXTRA_LEVEL_ID, levelIds[index]);
            intent.putExtra(LevelSchedulerPlayActivity.EXTRA_GAME_TYPE, GameTypeEnum.SCHEDULER.name());
            startActivityForResult(intent, REQUEST_LEVEL_PLAY);
        });
    }

    private String starsToString(int count) {
        switch (count) {
            case 3: return "\u2605\u2605\u2605";
            case 2: return "\u2605\u2605\u2606";
            case 1: return "\u2605\u2606\u2606";
            default: return "";
        }
    }
}
