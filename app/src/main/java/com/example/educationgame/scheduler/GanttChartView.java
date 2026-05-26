package com.example.educationgame.scheduler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.educationgame.R;
import com.example.educationgame.data.scheduler.model.ProcessInfo;

import java.util.ArrayList;
import java.util.List;

public class GanttChartView extends View {

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private List<ProcessInfo> processes = new ArrayList<>();
    private int[] startTimes = new int[0];
    private int[] endTimes = new int[0];
    private int totalTime = 1;

    public GanttChartView(Context context) {
        super(context);
        init();
    }

    public GanttChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setColor(getContext().getColor(R.color.gantt_text));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        timePaint.setColor(getContext().getColor(R.color.gantt_time_text));
        linePaint.setColor(getContext().getColor(R.color.gantt_grid_line));

        linePaint.setStrokeWidth(1f);
    }

    public void setSchedule(List<ProcessInfo> processes, int[] startTimes, int[] endTimes) {
        this.processes = new ArrayList<>(processes);
        this.startTimes = startTimes != null ? startTimes.clone() : new int[0];
        this.endTimes = endTimes != null ? endTimes.clone() : new int[0];
        this.totalTime = 1;
        for (int t : this.endTimes) {
            if (t > this.totalTime) this.totalTime = t;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        float h = getHeight() - getPaddingTop() - getPaddingBottom();

        if (processes.isEmpty() || startTimes.length == 0) return;

        float barHeight = h * 0.45f;
        float barTop = getPaddingTop() + h * 0.05f;
        float barBottom = barTop + barHeight;
        float unitW = w / totalTime;

        float fontSize = Math.min(barHeight * 0.45f, 32f);
        textPaint.setTextSize(fontSize);
        timePaint.setTextSize(fontSize * 0.7f);

        float cornerRadius = Math.min(8f, barHeight * 0.15f);

        for (int i = 0; i < processes.size() && i < startTimes.length && i < endTimes.length; i++) {
            float left = getPaddingLeft() + startTimes[i] * unitW;
            float right = getPaddingLeft() + endTimes[i] * unitW;

            barPaint.setColor(processes.get(i).getColor());
            rect.set(left, barTop, right, barBottom);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint);

            float cx = (left + right) / 2f;
            float cy = (barTop + barBottom) / 2f;
            canvas.drawText(processes.get(i).getName(), cx, cy + fontSize * 0.35f, textPaint);
        }

        float tickLen = Math.max(12f, h * 0.1f);
        float tickGap = 4f;
        for (int t = 0; t <= totalTime; t++) {
            float x = getPaddingLeft() + t * unitW;
            canvas.drawLine(x, barBottom + tickGap, x, barBottom + tickGap + tickLen, linePaint);
            canvas.drawText(String.valueOf(t), x, barBottom + tickGap + tickLen + timePaint.getTextSize(), timePaint);
        }
    }
}
