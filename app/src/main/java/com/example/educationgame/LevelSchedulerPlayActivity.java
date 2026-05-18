package com.example.educationgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.example.educationgame.data.local.entity.LevelProgressEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Date;
import java.util.Locale;

public class LevelSchedulerPlayActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL_NUMBER = "level_number";
    public static final String EXTRA_LEVEL_ID = "level_id";
    public static final String EXTRA_GAME_TYPE = "game_type";

    private int levelNumber;
    private int levelId;
    private GameTypeEnum gameType;

    private TextInputEditText timeInput;
    private TextView errorText;

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
        levelTitle.setText("Level " + levelNumber);

        LevelStarConfig.Thresholds thresholds = LevelStarConfig.getThresholds(gameType, levelNumber);

        TextView threeStarLabel = findViewById(R.id.thresholdThreeStar);
        threeStarLabel.setText(String.format(Locale.getDefault(),
                "\u2605\u2605\u2605  \u2264 %s", formatSeconds(thresholds.threeStarSeconds)));

        TextView twoStarLabel = findViewById(R.id.thresholdTwoStar);
        twoStarLabel.setText(String.format(Locale.getDefault(),
                "\u2605\u2605\u2606  \u2264 %s", formatSeconds(thresholds.twoStarSeconds)));

        TextView oneStarLabel = findViewById(R.id.thresholdOneStar);
        oneStarLabel.setText("\u2605\u2606\u2606  anything else");

        timeInput = findViewById(R.id.timeInput);
        errorText = findViewById(R.id.errorText);

        MaterialButton submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> onSubmit());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void onSubmit() {
        String timeText = null;
        if (timeInput.getText() != null) {
            timeText = timeInput.getText().toString().trim();
        }

        int seconds = parseTime(timeText);
        if (seconds < 0) {
            errorText.setText(R.string.dialog_invalid_time);
            errorText.setVisibility(android.view.View.VISIBLE);
            return;
        }

        int stars = LevelStarConfig.getStars(gameType, levelNumber, seconds);

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

    private int parseTime(@Nullable String timeText) {
        if (timeText == null || timeText.isEmpty()) return -1;

        String[] parts = timeText.split(":");
        try {
            if (parts.length == 2) {
                int minutes = Integer.parseInt(parts[0].trim());
                int secs = Integer.parseInt(parts[1].trim());
                if (secs < 0 || secs > 59 || minutes < 0) return -1;
                return minutes * 60 + secs;
            } else if (parts.length == 1) {
                int secs = Integer.parseInt(parts[0].trim());
                if (secs < 0) return -1;
                return secs;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    private String formatSeconds(int totalSecs) {
        int m = totalSecs / 60;
        int s = totalSecs % 60;
        return String.format(Locale.getDefault(), "%d:%02d", m, s);
    }
}
