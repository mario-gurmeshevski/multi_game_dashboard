package com.example.educationgame.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LogicWireView extends BaseCircuitView {

    public LogicWireView(Context context) { super(context); }
    public LogicWireView(Context context, AttributeSet attrs) { super(context, attrs); }

    public String getUnconnectedGateMessage() {
        for (Component c : components) {
            if (c.type.equals("AND") || c.type.equals("OR") || c.type.equals("NOT")) {
                boolean hasInput  = false;
                boolean hasOutput = false;
                for (Wire wire : wires) {
                    if (wire.to.componentId.equals(c.id))   hasInput  = true;
                    if (wire.from.componentId.equals(c.id)) hasOutput = true;
                }
                if (!hasInput && !hasOutput) {
                    return "Connect all gates! " + c.type + " gate is not connected.";
                }
                if (!hasInput) {
                    return "Connect all gates! " + c.type + " gate has no input.";
                }
                if (!hasOutput) {
                    return "Connect all gates! " + c.type + " gate has no output.";
                }
            }
        }
        return null;
    }

    public void setupLevel(int levelNumber) {
        components.clear();
        wires.clear();
        allPorts.clear();
        invalidate();

        post(() -> {
            float w = getWidth();
            float h = getHeight();
            buildLevel(levelNumber, w, h);
            invalidate();
        });
    }

    private void buildLevel(int levelNumber, float w, float h) {
        switch (levelNumber) {
            case 4: buildLevel4(w, h); break;
            case 5: buildLevel5(w, h); break;
            case 6: buildLevel6(w, h); break;
        }
    }

    private void buildLevel4(float w, float h) {
        float bw = w * 0.17f;
        float bh = h * 0.17f;
        float gw = w * 0.14f;
        float gh = h * 0.17f;
        float gap = h * 0.06f;

        Component btnA = makeButton("btnA", "BUTTON_A", w*0.02f, h*0.02f,               bw, bh, true);
        Component btnB = makeButton("btnB", "BUTTON_B", w*0.02f, h*0.02f+bh+gap,        bw, bh, false);
        Component btnC = makeButton("btnC", "BUTTON_C", w*0.02f, h*0.02f+2*(bh+gap),    bw, bh, true);
        Component btnD = makeButton("btnD", "BUTTON_D", w*0.02f, h*0.02f+3*(bh+gap),    bw, bh, true);

        float col2x = w * 0.26f;
        float c2y1  = h * 0.02f;
        float c2y2  = c2y1 + gh*1.5f + gap;
        float c2y3  = c2y2 + gh + gap;
        Component and1 = makeGate("and1", "AND", col2x, c2y1, gw, gh*1.5f);
        Component not1 = makeGate("not1", "NOT", col2x, c2y2, gw, gh);
        Component and2 = makeGate("and2", "AND", col2x, c2y3, gw, gh*1.5f);

        float col3x = w * 0.47f;
        float or1y  = h * 0.25f;
        Component or1 = makeGate("or1", "OR", col3x, or1y, gw, gh*1.8f);

        float col4x = w * 0.67f;
        Component not2 = makeGate("not2", "NOT", col4x, or1y + gh*0.4f, gw, gh);

        float bulbSize = Math.min(w, h) * 0.22f;
        Component bulb = makeBulb(w*0.86f, h/2f - bulbSize/2f, bulbSize);

        addAll(btnA, btnB, btnC, btnD, and1, not1, and2, or1, not2, bulb);
    }

    private void buildLevel5(float w, float h) {
        float bw = w * 0.17f;
        float bh = h * 0.17f;
        float gw = w * 0.13f;
        float gh = h * 0.16f;
        float gap = h * 0.06f;

        Component btnA = makeButton("btnA", "BUTTON_A", w*0.02f, h*0.02f,            bw, bh, false);
        Component btnB = makeButton("btnB", "BUTTON_B", w*0.02f, h*0.02f+bh+gap,     bw, bh, false);
        Component btnC = makeButton("btnC", "BUTTON_C", w*0.02f, h*0.02f+2*(bh+gap), bw, bh, true);
        Component btnD = makeButton("btnD", "BUTTON_D", w*0.02f, h*0.02f+3*(bh+gap), bw, bh, true);

        float col2x = w * 0.25f;
        float c2y1  = h * 0.02f;
        float c2y2  = c2y1 + gh*1.3f + gap;
        float c2y3  = c2y2 + gh + gap;
        float c2y4  = c2y3 + gh*1.3f + gap;
        Component or1  = makeGate("or1",  "OR",  col2x, c2y1, gw, gh*1.3f);
        Component not1 = makeGate("not1", "NOT", col2x, c2y2, gw, gh);
        Component and1 = makeGate("and1", "AND", col2x, c2y3, gw, gh*1.3f);
        Component and2 = makeGate("and2", "AND", col2x, c2y4, gw, gh);

        float col3x = w * 0.45f;
        float c3y1  = h * 0.02f;
        float c3y2  = c3y1 + gh*1.3f + gap;
        float c3y3  = c3y2 + gh*1.3f + gap;
        Component and3 = makeGate("and3", "AND", col3x, c3y1, gw, gh*1.3f);
        Component or2  = makeGate("or2",  "OR",  col3x, c3y2, gw, gh*1.3f);
        Component not2 = makeGate("not2", "NOT", col3x, c3y3, gw, gh);

        float col4x = w * 0.65f;
        Component and4 = makeGate("and4", "AND", col4x, h/2f - gh*1.3f/2f, gw, gh*1.3f);

        float bulbSize = Math.min(w, h) * 0.20f;
        Component bulb = makeBulb(w*0.86f, h/2f - bulbSize/2f, bulbSize);

        addAll(btnA, btnB, btnC, btnD, or1, not1, and1, and2, and3, or2, not2, and4, bulb);
    }

    private void buildLevel6(float w, float h) {
        float bw = w * 0.16f;
        float bh = h * 0.22f;
        float gw = w * 0.13f;
        float gh = h * 0.18f;
        float gap = h * 0.04f;

        Component btnA = makeButton("btnA", "BUTTON_A", w*0.02f, gap,            bw, bh, false);
        Component btnB = makeButton("btnB", "BUTTON_B", w*0.02f, gap+bh+gap,     bw, bh, true);
        Component btnC = makeButton("btnC", "BUTTON_C", w*0.02f, gap+2*(bh+gap), bw, bh, false);

        float col2x = w * 0.23f;
        Component and1 = makeGate("and1", "AND", col2x, gap,                gw, gh*1.3f);
        Component not1 = makeGate("not1", "NOT", col2x, gap+gh*1.3f+gap,   gw, gh);
        Component and2 = makeGate("and2", "AND", col2x, gap+gh*1.3f+gap+gh+gap, gw, gh*1.3f);
        Component not2 = makeGate("not2", "NOT", col2x, gap+gh*1.3f+gap+gh+gap+gh*1.3f+gap, gw, gh);

        float col3x = w * 0.43f;
        Component not3 = makeGate("not3", "NOT", col3x, gap,              gw, gh);
        Component or1  = makeGate("or1",  "OR",  col3x, gap+gh+gap,      gw, gh*1.3f);
        Component and3 = makeGate("and3", "AND", col3x, gap+gh+gap+gh*1.3f+gap, gw, gh*1.3f);
        Component not4 = makeGate("not4", "NOT", col3x, gap+gh+gap+gh*1.3f+gap+gh*1.3f+gap, gw, gh);

        float col4x = w * 0.63f;
        Component or2  = makeGate("or2",  "OR",  col4x, gap,              gw, gh*1.3f);
        Component or3  = makeGate("or3",  "OR",  col4x, gap+gh*1.3f+gap+gh*1.3f+gap, gw, gh*1.3f);

        float col5x = w * 0.78f;
        Component and4 = makeGate("and4", "AND", col5x, h/2f - gh*1.3f/2f, gw, gh*1.3f);

        float bulbSize = Math.min(w, h) * 0.18f;
        Component bulb = makeBulb(w*0.92f, h/2f - bulbSize/2f, bulbSize);

        addAll(btnA, btnB, btnC, and1, not1, and2, not2, not3, or1, and3, not4, or2, or3, and4, bulb);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragFromPort = findPort(x, y, 40f);
                if (dragFromPort != null && dragFromPort.isOutput) {
                    dragX = x;
                    dragY = y;
                } else {
                    dragFromPort = null;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (dragFromPort != null) {
                    dragX = x;
                    dragY = y;
                    hoveredPort = findPort(x, y, 40f);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (dragFromPort != null) {
                    Port target = findPort(x, y, 40f);
                    if (target != null && !target.isOutput
                            && !target.componentId.equals(dragFromPort.componentId)) {

                        boolean isButtonToBulb = dragFromPort.componentId.startsWith("btn")
                                && target.componentId.equals("bulb");
                        boolean isButtonToButton = dragFromPort.componentId.startsWith("btn")
                                && target.componentId.startsWith("btn");

                        if (!isButtonToBulb && !isButtonToButton) {
                            wires.removeIf(w -> w.to == target);
                            Wire wire = new Wire(dragFromPort, target);
                            wires.add(wire);
                            evaluateCircuit();
                        }
                    }
                    dragFromPort = null;
                    hoveredPort  = null;
                    invalidate();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(colorBg);
        drawWires(canvas);
        for (Component c : components) drawComponent(canvas, c);
        drawDragWire(canvas);
        drawPorts(canvas);
    }

    @Override
    protected void drawButton(Canvas canvas, Component c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorBtnBg);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 14, 14, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(c.value ? colorBtnOn : colorBtnOff);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 14, 14, paint);

        float cx = c.x + c.w * 0.25f;
        float cy = c.y + c.h * 0.45f;
        float r  = Math.min(c.w, c.h) * 0.16f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(c.value ? colorSwitchOn : colorSwitchOff);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(c.value ? colorBtnOn : colorBtnOff);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(c.value ? colorBtnOn : colorBtnOff);
        canvas.drawCircle(cx, cy, r * 0.5f, paint);

        String label;
        switch (c.type) {
            case "BUTTON_A": label = "Btn A"; break;
            case "BUTTON_B": label = "Btn B"; break;
            case "BUTTON_C": label = "Btn C"; break;
            case "BUTTON_D": label = "Btn D"; break;
            default: label = "Btn";
        }
        paint.setTextSize(c.h * 0.22f);
        paint.setColor(c.value ? colorBtnOn : colorSwitchTextOff);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, c.x + c.w * 0.65f, c.y + c.h * 0.42f, paint);
        canvas.drawText(c.value ? "T" : "F", c.x + c.w * 0.65f, c.y + c.h * 0.72f, paint);
    }

    @Override
    protected void drawGate(Canvas canvas, Component c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorGateBg);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 16, 16, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(c.value ? colorWireOn : colorGateBorder);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 16, 16, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(c.h * 0.22f);
        paint.setColor(colorGateText);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(c.type, c.x + c.w/2f, c.y + c.h/2f + c.h*0.08f, paint);
    }
}
