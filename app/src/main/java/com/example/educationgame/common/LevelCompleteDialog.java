package com.example.educationgame.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.educationgame.R;

public class LevelCompleteDialog {

    public interface OnDialogActionListener {
        void onNextLevel();
        void onRetry();
        void onBack();
    }

    public static void show(Context context, int stars, int levelNumber,
                            int totalLevels, OnDialogActionListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_level_complete, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView s1 = view.findViewById(R.id.dialogStar1);
        TextView s2 = view.findViewById(R.id.dialogStar2);
        TextView s3 = view.findViewById(R.id.dialogStar3);

        int activeColor   = 0xFFFFD700;
        int inactiveColor = 0xFF444466;

        s1.setTextColor(stars >= 1 ? activeColor : inactiveColor);
        s2.setTextColor(stars >= 2 ? activeColor : inactiveColor);
        s3.setTextColor(stars >= 3 ? activeColor : inactiveColor);

        TextView title = view.findViewById(R.id.dialogTitle);
        if (stars == 3)      title.setText(R.string.perfect);
        else if (stars == 2) title.setText(R.string.great_job);
        else                 title.setText(R.string.level_complete);

        Button btnNext  = view.findViewById(R.id.btnNextLevel);
        Button btnRetry = view.findViewById(R.id.btnRetry);
        Button btnBack  = view.findViewById(R.id.btnBack);

        if (levelNumber >= totalLevels) {
            btnNext.setEnabled(false);
            btnNext.setAlpha(0.4f);
        }

        btnNext.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onNextLevel();
        });
        btnRetry.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onRetry();
        });
        btnBack.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onBack();
        });

        dialog.show();
    }
}
