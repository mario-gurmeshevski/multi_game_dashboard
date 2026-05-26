package com.example.educationgame.logic;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.example.educationgame.R;

public class LogicGameView extends View {

    private int COLOR_BG;
    private int COLOR_PANEL;
    private int COLOR_PANEL_BORDER;
    private int COLOR_ON;
    private int COLOR_OFF;
    private int COLOR_GATE_BG;
    private int COLOR_GATE_BORDER;
    private int COLOR_GATE_TEXT;
    private int COLOR_WIRE_ON;
    private int COLOR_WIRE_OFF;
    private int COLOR_BULB_ON;
    private int COLOR_BULB_OFF;
    private int COLOR_TEXT_ON;
    private int COLOR_TEXT_OFF;
    private int COLOR_SWITCH_ON;
    private int COLOR_SWITCH_OFF;
    private int COLOR_BULB_INNER_ON;
    private int COLOR_BULB_INNER_OFF;
    private int COLOR_BULB_HI_ON;
    private int COLOR_BULB_HI_OFF;
    private int COLOR_BULB_GLOW;
    private int COLOR_BULB_TEXT_OFF;

    private boolean inputA = false;
    private boolean inputB = false;
    private boolean output = false;
    private LogicEngine.GateType gateType = LogicEngine.GateType.AND;
    private boolean isNotGate = false;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private OnInputChangedListener listener;

    public interface OnInputChangedListener {
        void onInputAChanged(boolean value);
        void onInputBChanged(boolean value);
    }

    private void loadColors() {
        COLOR_BG             = ContextCompat.getColor(getContext(), R.color.logic_bg);
        COLOR_PANEL          = ContextCompat.getColor(getContext(), R.color.logic_panel_bg);
        COLOR_PANEL_BORDER   = ContextCompat.getColor(getContext(), R.color.logic_panel_border);
        COLOR_ON             = ContextCompat.getColor(getContext(), R.color.logic_on);
        COLOR_OFF            = ContextCompat.getColor(getContext(), R.color.logic_off);
        COLOR_GATE_BG        = ContextCompat.getColor(getContext(), R.color.logic_gate_bg);
        COLOR_GATE_BORDER    = ContextCompat.getColor(getContext(), R.color.logic_gate_border);
        COLOR_GATE_TEXT      = ContextCompat.getColor(getContext(), R.color.logic_gate_text);
        COLOR_WIRE_ON        = ContextCompat.getColor(getContext(), R.color.logic_wire_on);
        COLOR_WIRE_OFF       = ContextCompat.getColor(getContext(), R.color.logic_wire_off);
        COLOR_BULB_ON        = ContextCompat.getColor(getContext(), R.color.logic_bulb_on);
        COLOR_BULB_OFF       = ContextCompat.getColor(getContext(), R.color.logic_bulb_off);
        COLOR_TEXT_ON        = ContextCompat.getColor(getContext(), R.color.logic_on);
        COLOR_TEXT_OFF       = ContextCompat.getColor(getContext(), R.color.logic_text_off);
        COLOR_SWITCH_ON      = ContextCompat.getColor(getContext(), R.color.logic_switch_on);
        COLOR_SWITCH_OFF     = ContextCompat.getColor(getContext(), R.color.logic_switch_off);
        COLOR_BULB_INNER_ON  = ContextCompat.getColor(getContext(), R.color.logic_bulb_inner_on);
        COLOR_BULB_INNER_OFF = ContextCompat.getColor(getContext(), R.color.logic_bulb_inner_off);
        COLOR_BULB_HI_ON     = ContextCompat.getColor(getContext(), R.color.logic_bulb_hi_on);
        COLOR_BULB_HI_OFF    = ContextCompat.getColor(getContext(), R.color.logic_bulb_hi_off);
        COLOR_BULB_GLOW      = ContextCompat.getColor(getContext(), R.color.logic_bulb_glow);
        COLOR_BULB_TEXT_OFF  = ContextCompat.getColor(getContext(), R.color.logic_bulb_text_off);
    }

    public LogicGameView(Context context) {
        super(context);
        loadColors();
    }

    public LogicGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadColors();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadColors();
        invalidate();
    }

    public void setGateType(LogicEngine.GateType type) {
        this.gateType = type;
        this.isNotGate = (type == LogicEngine.GateType.NOT);
        this.inputA = false;
        this.inputB = false;
        if (isNotGate) {
            inputA = true;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;

        float w = getWidth();
        float h = getHeight();
        float x = event.getX();
        float y = event.getY();

        float aLeft = w * 0.05f, aRight = w * 0.22f;
        float aTop  = isNotGate ? h * 0.35f : h * 0.15f;
        float aBot  = isNotGate ? h * 0.65f : h * 0.45f;

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

    private void drawAndOrLayout(Canvas canvas, float w, float h) {
        float btnW = w * 0.17f, btnH = h * 0.28f;
        float aX = w * 0.05f, aY = h * 0.15f;
        float bX = w * 0.05f, bY = h * 0.55f;

        float gateX = w * 0.38f, gateY = h * 0.25f;
        float gateW = w * 0.18f, gateH = h * 0.50f;

        float bulbCX = w * 0.80f, bulbCY = h * 0.50f;
        float bulbR  = Math.min(w, h) * 0.13f;

        drawWire(canvas, aX + btnW, aY + btnH / 2f, gateX, gateY + gateH * 0.30f, inputA);
        drawWire(canvas, bX + btnW, bY + btnH / 2f, gateX, gateY + gateH * 0.70f, inputB);
        drawWire(canvas, gateX + gateW, gateY + gateH / 2f, bulbCX - bulbR, bulbCY, output);

        drawButton(canvas, aX, aY, btnW, btnH, "Button A", inputA);
        drawButton(canvas, bX, bY, btnW, btnH, "Button B", inputB);
        drawGate(canvas, gateX, gateY, gateW, gateH);
        drawBulb(canvas, bulbCX, bulbCY, bulbR);
    }

    private void drawNotLayout(Canvas canvas, float w, float h) {
        float btnW = w * 0.17f, btnH = h * 0.28f;
        float aX = w * 0.05f, aY = h * 0.35f;

        float gateX = w * 0.38f, gateY = h * 0.25f;
        float gateW = w * 0.18f, gateH = h * 0.50f;

        float bulbCX = w * 0.80f, bulbCY = h * 0.50f;
        float bulbR  = Math.min(w, h) * 0.13f;

        drawWire(canvas, aX + btnW, aY + btnH / 2f, gateX, gateY + gateH / 2f, inputA);
        drawWire(canvas, gateX + gateW, gateY + gateH / 2f, bulbCX - bulbR, bulbCY, output);

        drawButton(canvas, aX, aY, btnW, btnH, "Button A", inputA);
        drawGate(canvas, gateX, gateY, gateW, gateH);
        drawBulb(canvas, bulbCX, bulbCY, bulbR);
    }

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

        float cx = x + w * 0.28f, cy = y + h * 0.42f, r = Math.min(w, h) * 0.18f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(on ? COLOR_SWITCH_ON : COLOR_SWITCH_OFF);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(on ? COLOR_ON : COLOR_OFF);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(on ? COLOR_ON : COLOR_OFF);
        canvas.drawCircle(cx, cy, r * 0.5f, paint);

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
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(output ? COLOR_BULB_INNER_ON : COLOR_BULB_INNER_OFF);
        canvas.drawCircle(cx, cy, r, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(output ? COLOR_BULB_ON : COLOR_BULB_OFF);
        canvas.drawCircle(cx, cy, r, paint);

        float bR = r * 0.55f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(output ? COLOR_BULB_HI_ON : COLOR_BULB_HI_OFF);
        canvas.drawCircle(cx, cy - r * 0.1f, bR, paint);

        if (output) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(COLOR_BULB_GLOW);
            canvas.drawCircle(cx, cy, r * 1.2f, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(r * 0.4f);
        paint.setColor(output ? COLOR_BULB_ON : COLOR_BULB_TEXT_OFF);
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
