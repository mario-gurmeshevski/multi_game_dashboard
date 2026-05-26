package com.example.educationgame.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LogicBuildView extends View {

    // ── Бои ──────────────────────────────────────────────────
    private static final int COLOR_BG          = 0xFF12102a;
    private static final int COLOR_PORT        = 0xFF7c6fcd;
    private static final int COLOR_PORT_HOVER  = 0xFF1de9b6;
    private static final int COLOR_WIRE_ON     = 0xFF1de9b6;
    private static final int COLOR_WIRE_OFF    = 0xFF3a5a80;
    private static final int COLOR_WIRE_DRAG   = 0xAA7c6fcd;
    private static final int COLOR_GATE_BG     = 0xFF1e1145;
    private static final int COLOR_GATE_BORDER = 0xFF7c6fcd;
    private static final int COLOR_GATE_TEXT   = 0xFFc0b0ff;
    private static final int COLOR_BTN_BG      = 0xFF0d2137;
    private static final int COLOR_BTN_ON      = 0xFF1de9b6;
    private static final int COLOR_BTN_OFF     = 0xFF3a5a80;
    private static final int COLOR_BULB_ON     = 0xFFFFD700;
    private static final int COLOR_BULB_OFF    = 0xFF2a2255;
    private boolean deleteMode = false;
    private Component selectedComponent = null;


    public void deleteSelectedComponent() {
        if (selectedComponent != null && !selectedComponent.id.equals("bulb")) {
            // Избриши ги жиците поврзани со оваа компонента
            wires.removeIf(w ->
                    w.from.componentId.equals(selectedComponent.id) ||
                            w.to.componentId.equals(selectedComponent.id));
            // Избриши ги портовите
            allPorts.removeIf(p -> p.componentId.equals(selectedComponent.id));
            // Избриши ја компонентата
            components.remove(selectedComponent);
            selectedComponent = null;
            evaluateCircuit();
            invalidate();
        }
    }

    public void setSelectedComponent(Component c) {
        selectedComponent = c;
    }

    public Component getSelectedComponent() {
        return selectedComponent;
    }

    // ── Модел ─────────────────────────────────────────────────
    public static class Port {
        public String id, componentId;
        public float x, y;
        public boolean isOutput;

        public Port(String id, String componentId, float x, float y, boolean isOutput) {
            this.id = id; this.componentId = componentId;
            this.x = x; this.y = y; this.isOutput = isOutput;
        }
    }

    public static class Component {
        public String id, type;
        public float x, y, w, h;
        public boolean value = false;
        public List<Port> ports = new ArrayList<>();
        public boolean dragging = false;

        public Component(String id, String type, float x, float y, float w, float h) {
            this.id = id; this.type = type;
            this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }

    public static class Wire {
        public Port from, to;
        public boolean active = false;
        public Wire(Port from, Port to) { this.from = from; this.to = to; }
    }

    // ── Состојба ─────────────────────────────────────────────
    private final List<Component> components = new ArrayList<>();
    private final List<Wire> wires = new ArrayList<>();
    private final List<Port> allPorts = new ArrayList<>();

    // Drag жица
    private Port dragFromPort = null;
    private float dragX, dragY;
    private Port hoveredPort = null;

    // Drag компонента
    private Component dragComponent = null;
    private float dragOffsetX, dragOffsetY;
    private long touchDownTime;
    private float touchDownX, touchDownY;

    // Level config
    private int requiredGates = 0;
    private boolean requireAnd = false;
    private boolean requireNot = false;
    private boolean requireOr  = false;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnCircuitChangedListener listener;

    public interface OnCircuitChangedListener {
        void onCircuitChanged(boolean bulbOn);
    }

    public LogicBuildView(Context context) { super(context); }
    public LogicBuildView(Context context, AttributeSet attrs) { super(context, attrs); }

    public void setOnCircuitChangedListener(OnCircuitChangedListener l) { this.listener = l; }

    // ── Setup ─────────────────────────────────────────────────

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
            // Постави ја сијалицата на десната страна
            float bulbSize = Math.min(w, h) * 0.28f;
            Component bulb = makeBulb("bulb",
                    w * 0.82f, h / 2f - bulbSize / 2f, bulbSize);
            addComponent(bulb);
            invalidate();
        });
    }

    // Додај компонента од toolbar
    public void addComponentToCanvas(String type) {
        float w = getWidth();
        float h = getHeight();
        float gw = w * 0.14f;
        float gh = h * 0.28f;

        // Постави ја на средината
        float cx = w * 0.3f + (float)(Math.random() * w * 0.2f);
        float cy = h * 0.2f + (float)(Math.random() * h * 0.5f);

        Component c;
        switch (type) {
            case "BUTTON":
                c = makeButton("btn_" + System.currentTimeMillis(),
                        "BUTTON_A", cx, cy, gw, gh, true);
                break;
            default:
                c = makeGate("gate_" + System.currentTimeMillis(),
                        type, cx, cy, gw, gh);
                break;
        }
        addComponent(c);
        invalidate();
    }

    private void addComponent(Component c) {
        components.add(c);
        for (Port p : c.ports) allPorts.add(p);
    }

    // ── Helper методи ─────────────────────────────────────────

    private Component makeButton(String id, String type,
                                 float x, float y, float w, float h, boolean value) {
        Component c = new Component(id, type, x, y, w, h);
        c.value = value;
        Port out = new Port(id + "_out", id, x + w, y + h / 2f, true);
        c.ports.add(out);
        return c;
    }

    private Component makeGate(String id, String type,
                               float x, float y, float w, float h) {
        Component c = new Component(id, type, x, y, w, h);
        if (type.equals("NOT")) {
            Port in  = new Port(id + "_in",  id, x,     y + h / 2f, false);
            Port out = new Port(id + "_out", id, x + w, y + h / 2f, true);
            c.ports.add(in); c.ports.add(out);
        } else {
            Port in1 = new Port(id + "_in1", id, x, y + h * 0.30f, false);
            Port in2 = new Port(id + "_in2", id, x, y + h * 0.70f, false);
            Port out = new Port(id + "_out", id, x + w, y + h / 2f, true);
            c.ports.add(in1); c.ports.add(in2); c.ports.add(out);
        }
        return c;
    }

    private Component makeBulb(String id, float x, float y, float size) {
        Component c = new Component(id, "BULB", x, y, size, size);
        Port in = new Port(id + "_in", id, x, y + size / 2f, false);
        c.ports.add(in);
        return c;
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

    // ── Touch ─────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownTime = System.currentTimeMillis();
                touchDownX = x;
                touchDownY = y;

                // Провери дали е порт
                Port port = findPort(x, y, 35f);
                if (port != null && port.isOutput) {
                    dragFromPort = port;
                    dragX = x; dragY = y;
                    return true;
                }

                // Провери дали е компонента (не сијалица)
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
                    // Ажурирај жици поврзани со оваа компонента
                    for (Wire wire : wires) {
                        if (wire.from.componentId.equals(dragComponent.id) ||
                                wire.to.componentId.equals(dragComponent.id)) {
                            // Жиците автоматски се ажурираат бидејќи користат port референци
                        }
                    }
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
                    // Селектирај компонентата
                    selectedComponent = dragComponent;
                    invalidate();
                }
                dragComponent = null;
                break;
        }
        return true;
    }

    private Port findPort(float x, float y, float radius) {
        Port closest = null;
        float minDist = radius;
        for (Port p : allPorts) {
            float dist = (float) Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2));
            if (dist < minDist) { minDist = dist; closest = p; }
        }
        return closest;
    }

    private Component findComponent(float x, float y) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component c = components.get(i);
            if (x >= c.x && x <= c.x + c.w && y >= c.y && y <= c.y + c.h) return c;
        }
        return null;
    }

    private Component findComponentById(String id) {
        for (Component c : components) if (c.id.equals(id)) return c;
        return null;
    }

    // ── Евалуација ────────────────────────────────────────────

    private void evaluateCircuit() {
        for (Component c : components) {
            if (!c.type.startsWith("BUTTON")) c.value = false;
        }

        for (int pass = 0; pass < 10; pass++) {
            for (Wire wire : wires) {
                Component from = findComponentById(wire.from.componentId);
                Component to   = findComponentById(wire.to.componentId);
                if (from == null || to == null) continue;

                if (to.type.equals("AND")) {
                    to.value = getInputValue(to, "in1") && getInputValue(to, "in2");
                } else if (to.type.equals("OR")) {
                    to.value = getInputValue(to, "in1") || getInputValue(to, "in2");
                } else if (to.type.equals("NOT")) {
                    to.value = !from.value;
                } else if (to.type.equals("BULB")) {
                    to.value = from.value;
                }
                wire.active = from.value;
            }
        }

        Component bulb = findComponentById("bulb");
        boolean bulbOn = bulb != null && bulb.value;
        if (listener != null) listener.onCircuitChanged(bulbOn);
        invalidate();
    }

    private boolean getInputValue(Component gate, String suffix) {
        for (Wire wire : wires) {
            if (wire.to.componentId.equals(gate.id) && wire.to.id.contains(suffix)) {
                Component from = findComponentById(wire.from.componentId);
                return from != null && from.value;
            }
        }
        return false;
    }

    // ── Валидација ────────────────────────────────────────────

    public String validate() {
        // Провери дали сите гејти се поврзани
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

        // Провери број на гејти
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

        // Провери сијалица
        Component bulb = findComponentById("bulb");
        if (bulb == null || !bulb.value)
            return "The bulb is not ON! Check your connections.";

        return null; // Се е точно
    }

    public boolean getBulbState() {
        Component bulb = findComponentById("bulb");
        return bulb != null && bulb.value;
    }

    // ── Draw ──────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(COLOR_BG);
        drawGrid(canvas);
        drawWires(canvas);
        for (Component c : components) drawComponent(canvas, c);
        drawDragWire(canvas);
        drawPorts(canvas);
    }

    private void drawGrid(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1a1835);
        float step = 24f;
        for (float x = 0; x < getWidth(); x += step) {
            for (float y = 0; y < getHeight(); y += step) {
                canvas.drawCircle(x, y, 1f, paint);
            }
        }
    }

    private void drawWires(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setPathEffect(null);
        for (Wire wire : wires) {
            paint.setColor(wire.active ? COLOR_WIRE_ON : COLOR_WIRE_OFF);
            Path path = new Path();
            float midX = (wire.from.x + wire.to.x) / 2f;
            path.moveTo(wire.from.x, wire.from.y);
            path.cubicTo(midX, wire.from.y, midX, wire.to.y, wire.to.x, wire.to.y);
            canvas.drawPath(path, paint);
        }
    }

    private void drawDragWire(Canvas canvas) {
        if (dragFromPort == null) return;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(COLOR_WIRE_DRAG);
        paint.setPathEffect(new android.graphics.DashPathEffect(new float[]{12f, 8f}, 0));
        Path path = new Path();
        float midX = (dragFromPort.x + dragX) / 2f;
        path.moveTo(dragFromPort.x, dragFromPort.y);
        path.cubicTo(midX, dragFromPort.y, midX, dragY, dragX, dragY);
        canvas.drawPath(path, paint);
        paint.setPathEffect(null);
    }

    private void drawPorts(Canvas canvas) {
        for (Port p : allPorts) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(p == hoveredPort || p == dragFromPort ? COLOR_PORT_HOVER : COLOR_PORT);
            canvas.drawCircle(p.x, p.y, 10f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(0xFF12102a);
            canvas.drawCircle(p.x, p.y, 10f, paint);
        }
    }

    private void drawComponent(Canvas canvas, Component c) {
        switch (c.type) {
            case "BUTTON_A": drawButton(canvas, c, "Button"); break;
            case "AND": case "OR": case "NOT": drawGate(canvas, c); break;
            case "BULB": drawBulb(canvas, c); break;
        }
    }

    private void drawButton(Canvas canvas, Component c, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_BTN_BG);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 14, 14, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(c.value ? COLOR_BTN_ON : COLOR_BTN_OFF);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 14, 14, paint);

        float cx = c.x + c.w * 0.25f, cy = c.y + c.h * 0.45f;
        float r = Math.min(c.w, c.h) * 0.16f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(c.value ? 0xFF0b3d30 : 0xFF111827);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(c.value ? COLOR_BTN_ON : COLOR_BTN_OFF);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(c.value ? COLOR_BTN_ON : COLOR_BTN_OFF);
        canvas.drawCircle(cx, cy, r * 0.5f, paint);

        paint.setTextSize(c.h * 0.20f);
        paint.setColor(c.value ? COLOR_BTN_ON : 0xFF4a6a8a);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, c.x + c.w * 0.65f, c.y + c.h * 0.42f, paint);
        canvas.drawText(c.value ? "TRUE" : "FALSE", c.x + c.w * 0.65f, c.y + c.h * 0.68f, paint);
        // Во drawGate — замени го stroke делот
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(c == selectedComponent ? 4f : 2.5f);
        paint.setColor(c == selectedComponent ? 0xFFFF5555 :
                (c.value ? COLOR_WIRE_ON : COLOR_GATE_BORDER));
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 16, 16, paint);
    }

    private void drawGate(Canvas canvas, Component c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_GATE_BG);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 16, 16, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(c.value ? COLOR_WIRE_ON : COLOR_GATE_BORDER);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 16, 16, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(c.h * 0.25f);
        paint.setColor(COLOR_GATE_TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(c.type, c.x + c.w / 2f, c.y + c.h / 2f + c.h * 0.08f, paint);
        // Во drawGate — замени го stroke делот
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(c == selectedComponent ? 4f : 2.5f);
        paint.setColor(c == selectedComponent ? 0xFFFF5555 :
                (c.value ? COLOR_WIRE_ON : COLOR_GATE_BORDER));
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 16, 16, paint);
    }

    private void drawBulb(Canvas canvas, Component c) {
        float cx = c.x + c.w / 2f, cy = c.y + c.h / 2f, r = c.w / 2f;
        if (c.value) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x44FFD700);
            canvas.drawCircle(cx, cy, r * 1.4f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(c.value ? 0xFF1a2a1a : 0xFF111122);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(c.value ? COLOR_BULB_ON : COLOR_BULB_OFF);
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(c.value ? 0xFF2a4a1a : 0xFF1a1830);
        canvas.drawCircle(cx, cy - r * 0.1f, r * 0.55f, paint);
        paint.setTextSize(r * 0.38f);
        paint.setColor(c.value ? COLOR_BULB_ON : 0xFF555577);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(c.value ? "ON" : "OFF", cx, cy + r + r * 0.4f, paint);
    }
}