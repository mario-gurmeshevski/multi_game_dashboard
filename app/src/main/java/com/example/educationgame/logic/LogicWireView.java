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

public class LogicWireView extends View {

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

    // ── Модел ─────────────────────────────────────────────────

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
    public static class Port {
        public String id;
        public float x, y;
        public boolean isOutput;
        public String componentId;

        public Port(String id, String componentId, float x, float y, boolean isOutput) {
            this.id = id;
            this.componentId = componentId;
            this.x = x;
            this.y = y;
            this.isOutput = isOutput;
        }
    }

    public static class Component {
        public String id;
        public String type;
        public float x, y, w, h;
        public boolean value = false;
        public List<Port> ports = new ArrayList<>();

        public Component(String id, String type, float x, float y, float w, float h) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    public static class Wire {
        public Port from;
        public Port to;
        public boolean active = false;

        public Wire(Port from, Port to) {
            this.from = from;
            this.to = to;
        }
    }

    // ── Состојба ─────────────────────────────────────────────
    private final List<Component> components = new ArrayList<>();
    private final List<Wire> wires = new ArrayList<>();
    private final List<Port> allPorts = new ArrayList<>();

    private Port dragFromPort = null;
    private float dragX, dragY;
    private Port hoveredPort = null;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnCircuitChangedListener listener;

    public interface OnCircuitChangedListener {
        void onCircuitChanged(boolean bulbOn);
    }

    public LogicWireView(Context context) { super(context); }
    public LogicWireView(Context context, AttributeSet attrs) { super(context, attrs); }

    public void setOnCircuitChangedListener(OnCircuitChangedListener l) {
        this.listener = l;
    }

    // ── Helper методи ─────────────────────────────────────────

    private Component makeButton(String id, String type,
                                 float x, float y, float w, float h, boolean value) {
        Component c = new Component(id, type, x, y, w, h);
        c.value = value;
        Port out = new Port(id+"_out", id, x+w, y+h/2f, true);
        c.ports.add(out);
        return c;
    }

    private Component makeGate(String id, String type,
                               float x, float y, float w, float h) {
        Component c = new Component(id, type, x, y, w, h);
        if (type.equals("NOT")) {
            Port in  = new Port(id+"_in",  id, x,   y+h/2f, false);
            Port out = new Port(id+"_out", id, x+w, y+h/2f, true);
            c.ports.add(in);
            c.ports.add(out);
        } else {
            Port in1 = new Port(id+"_in1", id, x, y+h*0.30f, false);
            Port in2 = new Port(id+"_in2", id, x, y+h*0.70f, false);
            Port out = new Port(id+"_out", id, x+w, y+h/2f,  true);
            c.ports.add(in1);
            c.ports.add(in2);
            c.ports.add(out);
        }
        return c;
    }

    private Component makeBulb(String id, float x, float y, float size) {
        Component c = new Component(id, "BULB", x, y, size, size);
        Port in = new Port(id+"_in", id, x, y+size/2f, false);
        c.ports.add(in);
        return c;
    }

    private void addAll(Component... comps) {
        for (Component c : comps) {
            components.add(c);
            for (Port p : c.ports) allPorts.add(p);
        }
    }

    // ── Setup нивоа ───────────────────────────────────────────

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

        // Копчиња — лева колона
        Component btnA = makeButton("btnA", "BUTTON_A", w*0.02f, h*0.02f,               bw, bh, true);
        Component btnB = makeButton("btnB", "BUTTON_B", w*0.02f, h*0.02f+bh+gap,        bw, bh, false);
        Component btnC = makeButton("btnC", "BUTTON_C", w*0.02f, h*0.02f+2*(bh+gap),    bw, bh, true);
        Component btnD = makeButton("btnD", "BUTTON_D", w*0.02f, h*0.02f+3*(bh+gap),    bw, bh, true);

        // Колона 2 — AND, NOT, AND
        float col2x = w * 0.26f;
        float c2y1  = h * 0.02f;
        float c2y2  = c2y1 + gh*1.5f + gap;
        float c2y3  = c2y2 + gh + gap;
        Component and1 = makeGate("and1", "AND", col2x, c2y1, gw, gh*1.5f);
        Component not1 = makeGate("not1", "NOT", col2x, c2y2, gw, gh);
        Component and2 = makeGate("and2", "AND", col2x, c2y3, gw, gh*1.5f);

        // Колона 3 — OR
        float col3x = w * 0.47f;
        float or1y  = h * 0.25f;
        Component or1 = makeGate("or1", "OR", col3x, or1y, gw, gh*1.8f);

        // Колона 4 — NOT
        float col4x = w * 0.67f;
        Component not2 = makeGate("not2", "NOT", col4x, or1y + gh*0.4f, gw, gh);

        // Сијалица
        float bulbSize = Math.min(w, h) * 0.22f;
        Component bulb = makeBulb("bulb", w*0.86f, h/2f - bulbSize/2f, bulbSize);

        addAll(btnA, btnB, btnC, btnD, and1, not1, and2, or1, not2, bulb);
    }

    private void buildLevel5(float w, float h) {
        float bw = w * 0.17f;
        float bh = h * 0.17f;
        float gw = w * 0.13f;
        float gh = h * 0.16f;
        float gap = h * 0.06f;

        // Копчиња — лева колона
        Component btnA = makeButton("btnA", "BUTTON_A", w*0.02f, h*0.02f,            bw, bh, false);
        Component btnB = makeButton("btnB", "BUTTON_B", w*0.02f, h*0.02f+bh+gap,     bw, bh, false);
        Component btnC = makeButton("btnC", "BUTTON_C", w*0.02f, h*0.02f+2*(bh+gap), bw, bh, true);
        Component btnD = makeButton("btnD", "BUTTON_D", w*0.02f, h*0.02f+3*(bh+gap), bw, bh, true);

        // Колона 2
        float col2x = w * 0.25f;
        float c2y1  = h * 0.02f;
        float c2y2  = c2y1 + gh*1.3f + gap;
        float c2y3  = c2y2 + gh + gap;
        float c2y4  = c2y3 + gh*1.3f + gap;
        Component or1  = makeGate("or1",  "OR",  col2x, c2y1, gw, gh*1.3f);
        Component not1 = makeGate("not1", "NOT", col2x, c2y2, gw, gh);
        Component and1 = makeGate("and1", "AND", col2x, c2y3, gw, gh*1.3f);
        Component and2 = makeGate("and2", "AND", col2x, c2y4, gw, gh);

        // Колона 3
        float col3x = w * 0.45f;
        float c3y1  = h * 0.02f;
        float c3y2  = c3y1 + gh*1.3f + gap;
        float c3y3  = c3y2 + gh*1.3f + gap;
        Component and3 = makeGate("and3", "AND", col3x, c3y1, gw, gh*1.3f);
        Component or2  = makeGate("or2",  "OR",  col3x, c3y2, gw, gh*1.3f);
        Component not2 = makeGate("not2", "NOT", col3x, c3y3, gw, gh);

        // Колона 4
        float col4x = w * 0.65f;
        Component and4 = makeGate("and4", "AND", col4x, h/2f - gh*1.3f/2f, gw, gh*1.3f);

        // Сијалица
        float bulbSize = Math.min(w, h) * 0.20f;
        Component bulb = makeBulb("bulb", w*0.86f, h/2f - bulbSize/2f, bulbSize);

        addAll(btnA, btnB, btnC, btnD, or1, not1, and1, and2, and3, or2, not2, and4, bulb);
    }

    private void buildLevel6(float w, float h) {
        float bw = w * 0.16f;
        float bh = h * 0.22f;
        float gw = w * 0.13f;
        float gh = h * 0.18f;
        float gap = h * 0.04f;

        // Копчиња — лева колона
        Component btnA = makeButton("btnA", "BUTTON_A", w*0.02f, gap,            bw, bh, false);
        Component btnB = makeButton("btnB", "BUTTON_B", w*0.02f, gap+bh+gap,     bw, bh, true);
        Component btnC = makeButton("btnC", "BUTTON_C", w*0.02f, gap+2*(bh+gap), bw, bh, false);

        // Колона 2
        float col2x = w * 0.23f;
        Component and1 = makeGate("and1", "AND", col2x, gap,                gw, gh*1.3f);
        Component not1 = makeGate("not1", "NOT", col2x, gap+gh*1.3f+gap,   gw, gh);
        Component and2 = makeGate("and2", "AND", col2x, gap+gh*1.3f+gap+gh+gap, gw, gh*1.3f);
        Component not2 = makeGate("not2", "NOT", col2x, gap+gh*1.3f+gap+gh+gap+gh*1.3f+gap, gw, gh);

        // Колона 3
        float col3x = w * 0.43f;
        Component not3 = makeGate("not3", "NOT", col3x, gap,              gw, gh);
        Component or1  = makeGate("or1",  "OR",  col3x, gap+gh+gap,      gw, gh*1.3f);
        Component and3 = makeGate("and3", "AND", col3x, gap+gh+gap+gh*1.3f+gap, gw, gh*1.3f);
        Component not4 = makeGate("not4", "NOT", col3x, gap+gh+gap+gh*1.3f+gap+gh*1.3f+gap, gw, gh);

        // Колона 4
        float col4x = w * 0.63f;
        Component or2  = makeGate("or2",  "OR",  col4x, gap,              gw, gh*1.3f);
        Component or3  = makeGate("or3",  "OR",  col4x, gap+gh*1.3f+gap+gh*1.3f+gap, gw, gh*1.3f);

        // Колона 5
        float col5x = w * 0.78f;
        Component and4 = makeGate("and4", "AND", col5x, h/2f - gh*1.3f/2f, gw, gh*1.3f);

        // Сијалица
        float bulbSize = Math.min(w, h) * 0.18f;
        Component bulb = makeBulb("bulb", w*0.92f, h/2f - bulbSize/2f, bulbSize);

        addAll(btnA, btnB, btnC, and1, not1, and2, not2, not3, or1, and3, not4, or2, or3, and4, bulb);
    }

    // ── Touch ─────────────────────────────────────────────────

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

    private Port findPort(float x, float y, float radius) {
        Port closest = null;
        float minDist = radius;
        for (Port p : allPorts) {
            float dist = (float) Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2));
            if (dist < minDist) {
                minDist = dist;
                closest = p;
            }
        }
        return closest;
    }

    // ── Евалуација ────────────────────────────────────────────

    private void evaluateCircuit() {
        for (Component c : components) {
            if (!c.type.startsWith("BUTTON")) {
                c.value = false;
            }
        }

        for (int pass = 0; pass < 10; pass++) {
            for (Wire wire : wires) {
                Component fromComp = findComponent(wire.from.componentId);
                Component toComp   = findComponent(wire.to.componentId);
                if (fromComp == null || toComp == null) continue;

                boolean signal = fromComp.value;

                if (toComp.type.equals("AND")) {
                    boolean in1 = getInputValue(toComp, "in1");
                    boolean in2 = getInputValue(toComp, "in2");
                    toComp.value = in1 && in2;
                } else if (toComp.type.equals("OR")) {
                    boolean in1 = getInputValue(toComp, "in1");
                    boolean in2 = getInputValue(toComp, "in2");
                    toComp.value = in1 || in2;
                } else if (toComp.type.equals("NOT")) {
                    toComp.value = !signal;
                } else if (toComp.type.equals("BULB")) {
                    toComp.value = signal;
                }

                wire.active = fromComp.value;
            }
        }

        boolean allGatesUsed = allGatesConnected();
        Component bulb = findComponent("bulb");
        boolean bulbOn = bulb != null && bulb.value && allGatesUsed;
        if (listener != null) listener.onCircuitChanged(bulbOn);
        invalidate();
    }

    private boolean allGatesConnected() {
        for (Component c : components) {
            if (c.type.equals("AND") || c.type.equals("OR") || c.type.equals("NOT")) {
                boolean hasInput  = false;
                boolean hasOutput = false;
                for (Wire wire : wires) {
                    if (wire.to.componentId.equals(c.id))   hasInput  = true;
                    if (wire.from.componentId.equals(c.id)) hasOutput = true;
                }
                if (!hasInput || !hasOutput) return false;
            }
        }
        return true;
    }

    private boolean getInputValue(Component gate, String inputSuffix) {
        for (Wire wire : wires) {
            if (wire.to.componentId.equals(gate.id)
                    && wire.to.id.contains(inputSuffix)) {
                Component from = findComponent(wire.from.componentId);
                return from != null && from.value;
            }
        }
        return false;
    }

    private Component findComponent(String id) {
        for (Component c : components) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }

    public boolean getBulbState() {
        Component bulb = findComponent("bulb");
        return bulb != null && bulb.value;
    }

    // ── Draw ──────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(COLOR_BG);
        drawWires(canvas);
        for (Component c : components) drawComponent(canvas, c);
        drawDragWire(canvas);
        drawPorts(canvas);
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
            boolean isHovered    = p == hoveredPort;
            boolean isDragSource = p == dragFromPort;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(isHovered || isDragSource ? COLOR_PORT_HOVER : COLOR_PORT);
            canvas.drawCircle(p.x, p.y, 10f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(0xFF12102a);
            canvas.drawCircle(p.x, p.y, 10f, paint);
        }
    }

    private void drawComponent(Canvas canvas, Component c) {
        switch (c.type) {
            case "BUTTON_A":
            case "BUTTON_B":
            case "BUTTON_C":
            case "BUTTON_D":
                drawButton(canvas, c); break;
            case "AND":
            case "OR":
            case "NOT":
                drawGate(canvas, c); break;
            case "BULB":
                drawBulb(canvas, c); break;
        }
    }

    private void drawButton(Canvas canvas, Component c) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(COLOR_BTN_BG);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 14, 14, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f);
        paint.setColor(c.value ? COLOR_BTN_ON : COLOR_BTN_OFF);
        canvas.drawRoundRect(c.x, c.y, c.x+c.w, c.y+c.h, 14, 14, paint);

        float cx = c.x + c.w * 0.25f;
        float cy = c.y + c.h * 0.45f;
        float r  = Math.min(c.w, c.h) * 0.16f;
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

        String label;
        switch (c.type) {
            case "BUTTON_A": label = "Btn A"; break;
            case "BUTTON_B": label = "Btn B"; break;
            case "BUTTON_C": label = "Btn C"; break;
            case "BUTTON_D": label = "Btn D"; break;
            default: label = "Btn";
        }
        paint.setTextSize(c.h * 0.22f);
        paint.setColor(c.value ? COLOR_BTN_ON : 0xFF4a6a8a);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, c.x + c.w * 0.65f, c.y + c.h * 0.42f, paint);
        canvas.drawText(c.value ? "T" : "F", c.x + c.w * 0.65f, c.y + c.h * 0.72f, paint);
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
        paint.setTextSize(c.h * 0.22f);
        paint.setColor(COLOR_GATE_TEXT);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(c.type, c.x + c.w/2f, c.y + c.h/2f + c.h*0.08f, paint);
    }

    private void drawBulb(Canvas canvas, Component c) {
        float cx = c.x + c.w/2f;
        float cy = c.y + c.h/2f;
        float r  = c.w/2f;

        if (c.value) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x44FFD700);
            canvas.drawCircle(cx, cy, r*1.4f, paint);
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
        canvas.drawCircle(cx, cy - r*0.1f, r*0.55f, paint);

        paint.setTextSize(r * 0.38f);
        paint.setColor(c.value ? COLOR_BULB_ON : 0xFF555577);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(c.value ? "ON" : "OFF", cx, cy + r + r*0.4f, paint);
    }
}