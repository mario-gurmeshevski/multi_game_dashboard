package com.example.educationgame.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import com.example.educationgame.R;

public class LogicBuildView extends BaseCircuitView {

    private int colorBuildCanvas;
    private int colorSelection;

    @Override
    protected void loadExtraColors() {
        colorBuildCanvas = ContextCompat.getColor(getContext(), R.color.logic_build_canvas);
        colorSelection   = ContextCompat.getColor(getContext(), R.color.logic_selection);
    }

    public LogicBuildView(Context context) { super(context); }
    public LogicBuildView(Context context, AttributeSet attrs) { super(context, attrs); }

    private Component selectedComponent = null;

    private Component dragComponent = null;
    private float dragOffsetX, dragOffsetY;

    private int requiredGates = 0;
    private boolean requireAnd = false;
    private boolean requireNot = false;
    private boolean requireOr  = false;

    public void deleteSelectedComponent() {
        if (selectedComponent != null && !selectedComponent.id.equals("bulb")) {
            wires.removeIf(w ->
                    w.from.componentId.equals(selectedComponent.id) ||
                            w.to.componentId.equals(selectedComponent.id));
            allPorts.removeIf(p -> p.componentId.equals(selectedComponent.id));
            components.remove(selectedComponent);
            selectedComponent = null;
            evaluateCircuit();
            invalidate();
        }
    }

    public void setupLevel(int levelNumber) {
        components.clear();
        wires.clear();
        allPorts.clear();

        switch (levelNumber) {
            case 7:
                requiredGates = 4;
                requireAnd = true;
                requireNot = true;
                requireOr  = false;
                break;
            case 8:
                requiredGates = 5;
                requireAnd = true;
                requireNot = true;
                requireOr  = true;
                break;
            case 9:
                requiredGates = 6;
                requireAnd = true;
                requireNot = true;
                requireOr  = true;
                break;
        }

        post(() -> {
            float w = getWidth();
            float h = getHeight();
            float bulbSize = Math.min(w, h) * 0.28f;
            Component bulb = makeBulb("bulb",
                    w * 0.82f, h / 2f - bulbSize / 2f, bulbSize);
            addComponent(bulb);
            invalidate();
        });
    }

    public void addComponentToCanvas(String type) {
        float w = getWidth();
        float h = getHeight();
        float gw = w * 0.14f;
        float gh = h * 0.28f;

        float cx = w * 0.3f + (float)(Math.random() * w * 0.2f);
        float cy = h * 0.2f + (float)(Math.random() * h * 0.5f);

        Component c;
        if (type.equals("BUTTON")) {
            c = makeButton("btn_" + System.currentTimeMillis(),
                    "BUTTON_A", cx, cy, gw, gh, true);
        } else {
            c = makeGate("gate_" + System.currentTimeMillis(),
                    type, cx, cy, gw, gh);
        }
        addComponent(c);
        invalidate();
    }

    private void addComponent(Component c) {
        components.add(c);
        allPorts.addAll(c.ports);
    }

    private void updatePortPositions(Component c) {
        for (Port p : c.ports) {
            if (p.isOutput) {
                p.x = c.x + c.w;
                p.y = c.y + c.h / 2f;
            } else if (p.id.contains("in1")) {
                p.x = c.x;
                p.y = c.y + c.h * 0.30f;
            } else if (p.id.contains("in2")) {
                p.x = c.x;
                p.y = c.y + c.h * 0.70f;
            } else {
                p.x = c.x;
                p.y = c.y + c.h / 2f;
            }
        }
        if (c.type.equals("BULB")) {
            for (Port p : c.ports) {
                p.x = c.x;
                p.y = c.y + c.h / 2f;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Port port = findPort(x, y, 35f);
                if (port != null && port.isOutput) {
                    dragFromPort = port;
                    dragX = x; dragY = y;
                    return true;
                }

                Component comp = findComponent(x, y);
                if (comp != null && !comp.id.equals("bulb")) {
                    dragComponent = comp;
                    dragOffsetX = x - comp.x;
                    dragOffsetY = y - comp.y;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (dragFromPort != null) {
                    dragX = x; dragY = y;
                    hoveredPort = findPort(x, y, 35f);
                    invalidate();
                } else if (dragComponent != null) {
                    dragComponent.x = x - dragOffsetX;
                    dragComponent.y = y - dragOffsetY;
                    updatePortPositions(dragComponent);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (dragFromPort != null) {
                    Port target = findPort(x, y, 35f);
                    if (target != null && !target.isOutput
                            && !target.componentId.equals(dragFromPort.componentId)) {
                        boolean isButtonToBulb = dragFromPort.componentId.startsWith("btn")
                                && target.componentId.equals("bulb");
                        if (!isButtonToBulb) {
                            wires.removeIf(w -> w.to == target);
                            wires.add(new Wire(dragFromPort, target));
                            evaluateCircuit();
                        }
                    }
                    dragFromPort = null;
                    hoveredPort = null;
                    invalidate();
                } else if (dragComponent != null) {
                    selectedComponent = dragComponent;
                    invalidate();
                }
                dragComponent = null;
                break;
        }
        return true;
    }

    private Component findComponent(float x, float y) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (x >= c.x && x <= c.x + c.w && y >= c.y && y <= c.y + c.h) return c;
        }
        return null;
    }

    public String validate() {
        for (Component c : components) {
            if (c.type.equals("AND") || c.type.equals("OR") || c.type.equals("NOT")) {
                boolean hasIn = false, hasOut = false;
                for (Wire w : wires) {
                    if (w.to.componentId.equals(c.id))   hasIn  = true;
                    if (w.from.componentId.equals(c.id)) hasOut = true;
                }
                if (!hasIn)  return c.type + " gate has no input connected!";
                if (!hasOut) return c.type + " gate has no output connected!";
            }
        }

        int gateCount = 0, andCount = 0, orCount = 0, notCount = 0;
        for (Component c : components) {
            if (c.type.equals("AND")) { gateCount++; andCount++; }
            if (c.type.equals("OR"))  { gateCount++; orCount++;  }
            if (c.type.equals("NOT")) { gateCount++; notCount++; }
        }

        if (gateCount < requiredGates)
            return "Use at least " + requiredGates + " gates! You have " + gateCount + ".";
        if (requireAnd && andCount == 0)
            return "You must use at least one AND gate!";
        if (requireNot && notCount == 0)
            return "You must use at least one NOT gate!";
        if (requireOr && orCount == 0)
            return "You must use at least one OR gate!";

        Component bulb = findComponentById("bulb");
        if (bulb == null || !bulb.value)
            return "The bulb is not ON! Check your connections.";

        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(colorBg);
        drawGrid(canvas);
        drawWires(canvas);
        for (Component c : components) drawComponent(canvas, c);
        drawDragWire(canvas);
        drawPorts(canvas);
    }

    private void drawGrid(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colorBuildCanvas);
        float step = 24f;
        for (float x = 0; x < getWidth(); x += step) {
            for (float y = 0; y < getHeight(); y += step) {
                canvas.drawCircle(x, y, 1f, paint);
            }
        }
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

        float cx = c.x + c.w * 0.25f, cy = c.y + c.h * 0.45f;
        float r = Math.min(c.w, c.h) * 0.16f;
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

        paint.setTextSize(c.h * 0.20f);
        paint.setColor(c.value ? colorBtnOn : colorSwitchTextOff);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Button", c.x + c.w * 0.65f, c.y + c.h * 0.42f, paint);
        canvas.drawText(c.value ? "TRUE" : "FALSE", c.x + c.w * 0.65f, c.y + c.h * 0.68f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(c == selectedComponent ? 4f : 2.5f);
        paint.setColor(c == selectedComponent ? colorSelection :
                (c.value ? colorWireOn : colorGateBorder));
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 16, 16, paint);
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
        paint.setTextSize(c.h * 0.25f);
        paint.setColor(colorGateText);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(c.type, c.x + c.w / 2f, c.y + c.h / 2f + c.h * 0.08f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(c == selectedComponent ? 4f : 2.5f);
        paint.setColor(c == selectedComponent ? colorSelection :
                (c.value ? colorWireOn : colorGateBorder));
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 16, 16, paint);
    }
}
