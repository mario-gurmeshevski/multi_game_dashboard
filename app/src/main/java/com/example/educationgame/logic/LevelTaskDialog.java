package com.example.educationgame.logic;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.educationgame.R;

public class LevelTaskDialog {

    public interface OnDismissListener {
        void onDismissed();
    }

    public static void show(Context context, int levelNumber, boolean isInfoOnly, OnDismissListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_level_task, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85f),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
        }

        TextView taskTitle       = view.findViewById(R.id.taskTitle);
        TextView taskDescription = view.findViewById(R.id.taskDescription);
        LinearLayout rulesLayout = view.findViewById(R.id.rulesLayout);
        TextView countdownText   = view.findViewById(R.id.countdownText);
        TextView timerHintText   = view.findViewById(R.id.timerHintText);
        Button btnGotIt          = view.findViewById(R.id.btnGotIt);

        switch (levelNumber) {
            case 7:
                taskTitle.setText(R.string.level_7_task);
                taskDescription.setText(R.string.level_7_text);
                addRule(context, rulesLayout, "✅ Must use exactly 4 gates");
                addRule(context, rulesLayout, "✅ Must use at least 1 AND gate");
                addRule(context, rulesLayout, "✅ Must use at least 1 NOT gate");
                addRule(context, rulesLayout, "✅ All gates must be connected");
                addRule(context, rulesLayout, "✅ Bulb must be ON");
                break;
            case 8:
                taskTitle.setText(R.string.level_8_task);
                taskDescription.setText(R.string.level_8_text);
                addRule(context, rulesLayout, "✅ Must use exactly 5 gates");
                addRule(context, rulesLayout, "✅ Must use at least 1 AND gate");
                addRule(context, rulesLayout, "✅ Must use at least 1 NOT gate");
                addRule(context, rulesLayout, "✅ Must use at least 1 OR gate");
                addRule(context, rulesLayout, "✅ All gates must be connected");
                addRule(context, rulesLayout, "✅ Bulb must be ON");
                break;
            case 9:
                taskTitle.setText(R.string.level_9_task);
                taskDescription.setText(R.string.level_9_text);
                addRule(context, rulesLayout, "✅ Must use exactly 6 gates");
                addRule(context, rulesLayout, "✅ Must use at least 2 AND gates");
                addRule(context, rulesLayout, "✅ Must use at least 2 NOT gates");
                addRule(context, rulesLayout, "✅ Must use at least 1 OR gate");
                addRule(context, rulesLayout, "✅ All gates must be connected");
                addRule(context, rulesLayout, "✅ Bulb must be ON");
                break;
            case 10:
                taskTitle.setText("Level 10");
                taskDescription.setText("Build a circuit without using AND gates.");
                addRule(context, rulesLayout, "✅ Must use exactly 6 gates");
                addRule(context, rulesLayout, "✅ AND gates are forbidden");
                addRule(context, rulesLayout, "✅ Must use at least 1 OR gate");
                addRule(context, rulesLayout, "✅ Must use at least 1 NOT gate");
                addRule(context, rulesLayout, "✅ Must have 1 button that is ON");
                addRule(context, rulesLayout, "✅ Must have 1 button that is OFF");
                addRule(context, rulesLayout, "✅ All gates must be connected");
                addRule(context, rulesLayout, "✅ Bulb must be ON");
                break;
            case 11:
                taskTitle.setText("Level 11");
                taskDescription.setText("Build a more complex circuit.");
                addRule(context, rulesLayout, "✅ Must use exactly 7 gates");
                addRule(context, rulesLayout, "✅ Must use at least 2 AND gates");
                addRule(context, rulesLayout, "✅ Must use at least 2 NOT gates");
                addRule(context, rulesLayout, "✅ Must have at least 2 buttons that are ON");
                addRule(context, rulesLayout, "✅ All gates must be connected");
                addRule(context, rulesLayout, "✅ Bulb must be ON");
                break;
            case 12:
                taskTitle.setText("Level 12");
                taskDescription.setText("Build a more complex circuit.");
                addRule(context, rulesLayout, "✅ Must use exactly 7 gates");
                addRule(context, rulesLayout, "✅ Must use at least 2 OR gates");
                addRule(context, rulesLayout, "✅ Must use at least 2 NOT gates");
                addRule(context, rulesLayout, "✅ Must have 1 button that is ON");
                addRule(context, rulesLayout, "✅ Must have 1 button that is OFF");
                addRule(context, rulesLayout, "✅ All gates must be connected");
                addRule(context, rulesLayout, "✅ Bulb must be ON");
                break;
            case 13:
                taskTitle.setText("Level 13");
                taskDescription.setText("Combine all gate types.");
                addRule(context, rulesLayout, "✅ Must use exactly 8 gates");
                addRule(context, rulesLayout, "✅ Must use at least 1 AND gate");
                addRule(context, rulesLayout, "✅ Must use at least 1 OR gate");
                addRule(context, rulesLayout, "✅ Must use at least 1 NOT gate");
                addRule(context, rulesLayout, "✅ Must have at least 2 buttons that are ON");
                addRule(context, rulesLayout, "✅ Must have 1 button that is OFF");
                addRule(context, rulesLayout, "✅ All gates must be connected");
                addRule(context, rulesLayout, "✅ Bulb must be ON");
                break;
            case 14:
                taskTitle.setText("Level 14");
                taskDescription.setText("Advanced circuit design.");
                addRule(context, rulesLayout, "✅ Must use exactly 9 gates");
                addRule(context, rulesLayout, "✅ Must use at least 2 AND gates");
                addRule(context, rulesLayout, "✅ Must use at least 2 OR gates");
                addRule(context, rulesLayout, "✅ Must use at least 2 NOT gates");
                addRule(context, rulesLayout, "✅ Must have at least 2 buttons that are ON");
                addRule(context, rulesLayout, "✅ All gates must be connected");
                addRule(context, rulesLayout, "✅ Bulb must be ON");
                break;
            case 15:
                taskTitle.setText("Level 15 — Final Challenge");
                taskDescription.setText("Put everything you've learned together.");
                addRule(context, rulesLayout, "✅ Must use exactly 10 gates");
                addRule(context, rulesLayout, "✅ Must use AND, OR and NOT gates");
                addRule(context, rulesLayout, "✅ Must have at least 2 buttons that are ON");
                addRule(context, rulesLayout, "✅ Must have at least 2 buttons that are OFF");
                addRule(context, rulesLayout, "✅ All gates must be connected");
                addRule(context, rulesLayout, "✅ Bulb must be ON");
                break;
        }

        if (isInfoOnly) {
            countdownText.setVisibility(View.GONE);
            timerHintText.setVisibility(View.GONE);
            btnGotIt.setText(R.string.close);
            btnGotIt.setOnClickListener(v -> {
                dialog.dismiss();
                listener.onDismissed();
            });
        } else {
            final int[] countdown = {10};
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable countdownRunnable = new Runnable() {
                @Override
                public void run() {
                    countdown[0]--;
                    countdownText.setText(String.valueOf(countdown[0]));
                    if (countdown[0] <= 0) {
                        dialog.dismiss();
                        listener.onDismissed();
                    } else {
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.postDelayed(countdownRunnable, 1000);

            btnGotIt.setOnClickListener(v -> {
                handler.removeCallbacks(countdownRunnable);
                dialog.dismiss();
                listener.onDismissed();
            });
        }

        dialog.show();
    }

    private static void addRule(Context context, LinearLayout layout, String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(11f);
        tv.setTextColor(context.getColor(R.color.task_desc_text));
        tv.setPadding(0, 4, 0, 4);
        layout.addView(tv);
    }
}