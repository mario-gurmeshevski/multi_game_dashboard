package com.example.educationgame.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class LogicGameView extends View {

    // ── Бои ──────────────────────────────────────────────────
    private static final int COLOR_BG           = 0xFF12102a;
    private static final int COLOR_PANEL        = 0xFF1d3557;
    private static final int COLOR_PANEL_BORDER = 0xFF4a90d9;
    private static final int COLOR_ON           = 0xFF1de9b6;
    private static final int COLOR_OFF          = 0xFF3a5a80;
    private static final int COLOR_GATE_BG      = 0xFF1e1145;
    private static final int COLOR_GATE_BORDER  = 0xFF7c6fcd;
    private static final int COLOR_GATE_TEXT    = 0xFFc0b0ff;
    private static final int COLOR_WIRE_ON      = 0xFF1de9b6;
    private static final int COLOR_WIRE_OFF     = 0xFF3a5a80;
    private static final int COLOR_BULB_ON      = 0xFFFFD700;
    private static final int COLOR_BULB_OFF     = 0xFF2a2255;
    private static final int COLOR_TEXT_ON      = 0xFF1de9b6;
    private static final int COLOR_TEXT_OFF     = 0xFF6a8aaa;
    private static final int COLOR_WHITE        = 0xFFE0E0E0;

    // ── Состојба ─────────────────────────────────────────────
    private boolean inputA = false;
    private boolean inputB = false;
    private boolean output = false;
    private LogicEngine.GateType gateType = LogicEngine.GateType.AND;
    private boolean isNotGate = false;

    // ── Paint ─────────────────────────────────────────────────
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ── Listener ─────────────────────────────────────────────
    private OnInputChangedListener listener;

    public interface OnInputChangedListener {
        void onInputAChanged(boolean value);
        void onInputBChanged(boolean value);
    }

    public LogicGameView(Context context) {
        super(context);
    }

    public LogicGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // ── Јавни методи ─────────────────────────────────────────

    public void setGateType(LogicEngine.GateType type) {
        this.gateType = type;
        this.isNotGate = (type == LogicEngine.GateType.NOT);
        this.inputA = false;
        this.inputB = false;
        if (isNotGate) {
            inputA = true; // почнуваме со TRUE за да биде NOT true = FALSE
            inputB = true;
        }
        recalcOutput();
        invalidate();
    }

    public void setOnInputChangedListener(OnInputChangedListener l) {
        this.listener = l;
    }

    public boolean getOutput() {
        return output;
    }

    // ── Touch ─────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;

        float w = getWidth();
        float h = getHeight();
        float x = event.getX();
        float y = event.getY();

        // Button A hitbox
        float aLeft = w * 0.05f, aRight = w * 0.22f;
        float aTop  = isNotGate ? h * 0.35f : h * 0.15f;
        float aBot  = isNotGate ? h * 0.65f : h * 0.45f;

        // Button B hitbox (само за AND/OR)
        float bLeft = w * 0.05f, bRight = w * 0.22f;
        float bTop  = h * 0.55f, bBot = h * 0.85f;

        if (x >= aLeft && x <= aRight && y >= aTop && y <= aBot) {
            inputA = !inputA;
            recalcOutput();
            invalidate();
            if (listener != null) listener.onInputAChanged(inputA);
            return true;
        }

        if (!isNotGate && x >= bLeft && x <= bRight && y >= bTop && y <= bBot) {
            inputB = !inputB;
            recalcOutput();
            invalidate();
            if (listener != null) listener.onInputBChanged(inputB);
            return true;
        }

        return true;
    }

    private void recalcOutput() {
        output = LogicEngine.evaluate(gateType, inputA, inputB);
    }

    // ── Draw ──────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();

        canvas.drawColor(COLOR_BG);

        if (isNotGate) {
            drawNotLayout(canvas, w, h);
        } else {
            drawAndOrLayout(canvas, w, h);
        }
    }

    // ── AND / OR layout ───────────────────────────────────────

    private void drawAndOrLayout(Canvas canvas, float w, float h) {
        // Позиции
        float btnW = w * 0.17f, btnH = h * 0.28f;
        float aX = w * 0.05f, aY = h * 0.15f;
        float bX = w * 0.05f, bY = h * 0.55f;

        float gateX = w * 0.38f, gateY = h * 0.25f;
        float gateW = w * 0.18f, gateH = h * 0.50f;

        float bulbCX = w * 0.80f, bulbCY = h * 0.50f;
        float bulbR  = Math.min(w, h) * 0.13f;

        // Жици
        drawWire(canvas,
                aX + btnW, aY + btnH / 2f,
                gateX, gateY + gateH * 0.30f,
                inputA);

        drawWire(canvas,
                bX + btnW, bY + btnH / 2f,
                gateX, gateY + gateH * 0.70f,
                inputB);

        drawWire(canvas,
                gateX + gateW, gateY + gateH / 2f,
                bulbCX - bulbR, bulbCY,
                output);

        // Копчиња
        drawButton(canvas, aX, aY, btnW, btnH, "Button A", inputA);
        drawButton(canvas, bX, bY, btnW, btnH, "Button B", inputB);

        // Гејт
        drawGate(canvas, gateX, gateY, gateW, gateH);

        // Сијалица
        drawBulb(canvas, bulbCX, bulbCY, bulbR);
    }

    // ── NOT layout ────────────────────────────────────────────

    private void drawNotLayout(Canvas canvas, float w, float h) {
        float btnW = w * 0.17f, btnH = h * 0.28f;
        float aX = w * 0.05f, aY = h * 0.35f;

        float gateX = w * 0.38f, gateY = h * 0.25f;
        float gateW = w * 0.18f, gateH = h * 0.50f;

        float bulbCX = w * 0.80f, bulbCY = h * 0.50f;
        float bulbR  = Math.min(w, h) * 0.13f;

        drawWire(canvas,
                aX + btnW, aY + btnH / 2f,
                gateX, gateY + gateH / 2f,
                inputA);

        drawWire(canvas,
                gateX + gateW, gateY + gateH / 2f,
                bulbCX - bulbR, bulbCY,
                output);

        drawButton(canvas, aX, aY, btnW, btnH, "Button A", inputA);
        drawGate(canvas, gateX, gateY, gateW, gateH);
        drawBulb(canvas, bulbCX, bulbCY, bulbR);
    }

    // ── Помошни Draw методи ───────────────────────────────────

    private void drawButton(Canvas canvas, float x, float y,
                            float w, float h, String label, boolean on) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_PANEL);
        RectF rect = new RectF(x, y, x + w, y + h);
        canvas.drawRoundRect(rect, 16, 16, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(on ? COLOR_PANEL_BORDER : COLOR_OFF);
        canvas.drawRoundRect(rect, 16, 16, paint);

        // LED круг
        float cx = x + w * 0.28f, cy = y + h * 0.42f, r = Math.min(w, h) * 0.18f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(on ? 0xFF0b3d30 : 0xFF111827);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(on ? COLOR_ON : COLOR_OFF);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(on ? COLOR_ON : COLOR_OFF);
        canvas.drawCircle(cx, cy, r * 0.5f, paint);

        // Текст
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(h * 0.18f);
        paint.setColor(on ? COLOR_TEXT_ON : COLOR_TEXT_OFF);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, x + w * 0.65f, y + h * 0.42f, paint);
        canvas.drawText(on ? "TRUE" : "FALSE", x + w * 0.65f, y + h * 0.68f, paint);
    }

    private void drawGate(Canvas canvas, float x, float y, float w, float h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_GATE_BG);
        RectF rect = new RectF(x, y, x + w, y + h);
        canvas.drawRoundRect(rect, 20, 20, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(COLOR_GATE_BORDER);
        canvas.drawRoundRect(rect, 20, 20, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(h * 0.22f);
        paint.setColor(COLOR_GATE_TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(gateType.name(), x + w / 2f, y + h / 2f + h * 0.08f, paint);
    }

    private void drawBulb(Canvas canvas, float cx, float cy, float r) {
        // Надворешен круг
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(output ? 0xFF1a2a1a : 0xFF111122);
        canvas.drawCircle(cx, cy, r, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(output ? COLOR_BULB_ON : COLOR_BULB_OFF);
        canvas.drawCircle(cx, cy, r, paint);

        // Сијалица форма
        float bR = r * 0.55f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(output ? 0xFF2a4a1a : 0xFF1a1830);
        canvas.drawCircle(cx, cy - r * 0.1f, bR, paint);

        // Светлина ако е ON
        if (output) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x44FFD700);
            canvas.drawCircle(cx, cy, r * 1.2f, paint);
        }

        // ON/OFF текст
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(r * 0.4f);
        paint.setColor(output ? COLOR_BULB_ON : 0xFF555577);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(output ? "ON" : "OFF", cx, cy + r * 1.5f, paint);
    }

    private void drawWire(Canvas canvas,
                          float x1, float y1, float x2, float y2, boolean on) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(on ? COLOR_WIRE_ON : COLOR_WIRE_OFF);

        if (!on) {
            paint.setPathEffect(new android.graphics.DashPathEffect(
                    new float[]{15f, 10f}, 0));
        } else {
            paint.setPathEffect(null);
        }

        float midX = (x1 + x2) / 2f;
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(x1, y1);
        path.cubicTo(midX, y1, midX, y2, x2, y2);
        canvas.drawPath(path, paint);

        paint.setPathEffect(null);
    }
}