package com.example.educationgame.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.educationgame.R;
import com.example.educationgame.data.enums.GameTypeEnum;
import com.example.educationgame.data.local.AppDatabase;
import com.example.educationgame.data.local.AppExecutors;
import com.example.educationgame.data.local.entity.GameEntity;
import com.example.educationgame.data.local.entity.LevelEntity;
import com.example.educationgame.data.local.entity.LevelProgressEntity;

import java.util.List;

public abstract class BaseLevelSelectActivity extends AppCompatActivity {

    protected AppDatabase db;
    protected int levelCount;
    protected int[] levelIds;
    protected int[] levelStars;

    private ProgressBar progressCircle;
    private TextView txtPercent;
    private TextView[] levelStatusViews;
    private LinearLayout[] levelCards;
    private final ImageView[] sidebarStars = new ImageView[3];

    private final ActivityResultLauncher<Intent> levelPlayLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    seedAndLoad();
                }
            });

    protected abstract int getLayoutResId();
    protected abstract GameTypeEnum getGameType();
    protected abstract int getLevelCount();
    protected abstract int[] getLevelCardIds();
    protected abstract int[] getLevelStatusIds();
    protected abstract String getGameTitle();
    protected abstract String getGameDescription();
    protected abstract String getLevelDescription(int levelNumber);
    protected abstract int getTotalPossibleStars();
    protected abstract Intent buildPlayIntent(int index, int levelNum, int levelId);

    protected void onLevelsSeeded(int gameId) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(getLayoutResId());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = AppDatabase.getInstance(this);
        levelCount = getLevelCount();
        levelIds = new int[levelCount];
        levelStars = new int[levelCount];
        levelStatusViews = new TextView[levelCount];
        levelCards = new LinearLayout[levelCount];

        bindViews();
        seedAndLoad();
    }

    private void bindViews() {
        progressCircle = findViewById(R.id.progressCircle);
        txtPercent = findViewById(R.id.txtPercent);

        sidebarStars[0] = findViewById(R.id.sidebarStar1);
        sidebarStars[1] = findViewById(R.id.sidebarStar2);
        sidebarStars[2] = findViewById(R.id.sidebarStar3);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        int[] cardIds = getLevelCardIds();
        int[] statusIds = getLevelStatusIds();
        for (int i = 0; i < levelCount; i++) {
            levelCards[i] = findViewById(cardIds[i]);
            levelStatusViews[i] = findViewById(statusIds[i]);
            levelStars[i] = 0;
        }
    }

    private void seedAndLoad() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            seedDatabase();

            int totalEarned = 0;
            for (int i = 0; i < levelCount; i++) {
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

            int totalPossible = getTotalPossibleStars();
            int percent = totalEarned == 0 ? 0 : (int) Math.ceil((double) totalEarned / totalPossible * 100);
            int finalTotalEarned = totalEarned;

            runOnUiThread(() -> updateUI(percent, finalTotalEarned, totalPossible));
        });
    }

    private void seedDatabase() {
        List<GameEntity> existing = db.gameDao().getGamesByType(getGameType());
        int gameId;
        if (existing.isEmpty()) {
            GameEntity game = new GameEntity();
            game.setType(getGameType());
            game.setTitle(getGameTitle());
            game.setDescription(getGameDescription());
            gameId = (int) db.gameDao().insert(game);
        } else {
            gameId = existing.get(0).getId();
        }

        List<LevelEntity> levels = db.levelDao().getLevelsByGameId(gameId);
        if (levels.isEmpty()) {
            for (int i = 1; i <= levelCount; i++) {
                LevelEntity level = new LevelEntity();
                level.setLevelNumber(i);
                level.setName("Level " + i);
                level.setDescription(getLevelDescription(i));
                level.setGameId(gameId);
                levelIds[i - 1] = (int) db.levelDao().insert(level);
            }
        } else {
            for (LevelEntity level : levels) {
                int idx = level.getLevelNumber() - 1;
                if (idx >= 0 && idx < levelCount) {
                    levelIds[idx] = level.getId();
                }
            }
            onLevelsSeeded(gameId);
        }
    }

    private void updateUI(int percent, int totalEarned, int totalPossible) {
        progressCircle.setProgress(percent);
        txtPercent.setText(getString(R.string.percent_format, percent));
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
        for (int i = 0; i < levelCount; i++) {
            boolean unlocked = isLevelUnlocked(i);
            TextView statusView = levelStatusViews[i];

            if (levelStars[i] > 0) {
                statusView.setText(starsToString(levelStars[i]));
                statusView.setTextColor(getColor(R.color.star_color));
                setCardClickListener(i, true);
            } else if (unlocked) {
                statusView.setText(R.string.level_play);
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
            Intent intent = buildPlayIntent(index, levelNum, levelIds[index]);
            levelPlayLauncher.launch(intent);
        });
    }

    private String starsToString(int count) {
        switch (count) {
            case 3: return "★★★";
            case 2: return "★★☆";
            case 1: return "★☆☆";
            default: return "";
        }
    }
}
