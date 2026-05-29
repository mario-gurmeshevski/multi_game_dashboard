package com.example.educationgame.logic;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.educationgame.R;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCircuitView extends View {

    public static class Port {
        public final String id;
        public final String componentId;
        public float x, y;
        public final boolean isOutput;

        public Port(String id, String componentId, float x, float y, boolean isOutput) {
            this.id = id;
            this.componentId = componentId;
            this.x = x;
            this.y = y;
            this.isOutput = isOutput;
        }
    }

    public static class Component {
        public final String id;
        public final String type;
        public float x, y;
        public final float w;
        public final float h;
        public boolean value = false;
        public final List<Port> ports = new ArrayList<>();

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
        public final Port from;
        public final Port to;
        public boolean active = false;

        public Wire(Port from, Port to) {
            this.from = from;
            this.to = to;
        }
    }

    protected final List<Component> components = new ArrayList<>();
    protected final List<Wire> wires = new ArrayList<>();
    protected final List<Port> allPorts = new ArrayList<>();

    protected Port dragFromPort = null;
    protected float dragX, dragY;
    protected Port hoveredPort = null;

    protected final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Runnable onCircuitChanged;

    protected int colorBg;
    protected int colorPort;
    protected int colorPortHover;
    protected int colorWireOn;
    protected int colorWireOff;
    protected int colorWireDrag;
    protected int colorGateBg;
    protected int colorGateBorder;
    protected int colorGateText;
    protected int colorBtnBg;
    protected int colorBtnOn;
    protected int colorBtnOff;
    protected int colorBulbOn;
    protected int colorBulbOff;
    protected int colorSwitchOn;
    protected int colorSwitchOff;
    protected int colorBulbGlow;
    protected int colorBulbInnerOn;
    protected int colorBulbInnerOff;
    protected int colorBulbHiOn;
    protected int colorBulbHiOff;
    protected int colorBulbTextOff;
    protected int colorSwitchTextOff;
    protected int colorPortBg;

    protected BaseCircuitView(Context context) {
        super(context);
        loadColors();
    }

    protected BaseCircuitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadColors();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadColors();
        invalidate();
    }

    public void setOnCircuitChangedListener(Runnable l) {
        this.onCircuitChanged = l;
    }

    private void loadColors() {
        colorBg             = ContextCompat.getColor(getContext(), R.color.logic_bg);
        colorPort           = ContextCompat.getColor(getContext(), R.color.logic_port_border);
        colorPortHover      = ContextCompat.getColor(getContext(), R.color.logic_port_hover);
        colorWireOn         = ContextCompat.getColor(getContext(), R.color.logic_wire_on);
        colorWireOff        = ContextCompat.getColor(getContext(), R.color.logic_wire_off);
        colorWireDrag       = ContextCompat.getColor(getContext(), R.color.logic_wire_drag);
        colorGateBg         = ContextCompat.getColor(getContext(), R.color.logic_gate_bg);
        colorGateBorder     = ContextCompat.getColor(getContext(), R.color.logic_gate_border);
        colorGateText       = ContextCompat.getColor(getContext(), R.color.logic_gate_text);
        colorBtnBg          = ContextCompat.getColor(getContext(), R.color.logic_btn_bg);
        colorBtnOn          = ContextCompat.getColor(getContext(), R.color.logic_on);
        colorBtnOff         = ContextCompat.getColor(getContext(), R.color.logic_off);
        colorBulbOn         = ContextCompat.getColor(getContext(), R.color.logic_bulb_on);
        colorBulbOff        = ContextCompat.getColor(getContext(), R.color.logic_bulb_off);
        colorSwitchOn       = ContextCompat.getColor(getContext(), R.color.logic_switch_on);
        colorSwitchOff      = ContextCompat.getColor(getContext(), R.color.logic_switch_off);
        colorBulbGlow       = ContextCompat.getColor(getContext(), R.color.logic_bulb_glow);
        colorBulbInnerOn    = ContextCompat.getColor(getContext(), R.color.logic_bulb_inner_on);
        colorBulbInnerOff   = ContextCompat.getColor(getContext(), R.color.logic_bulb_inner_off);
        colorBulbHiOn       = ContextCompat.getColor(getContext(), R.color.logic_bulb_hi_on);
        colorBulbHiOff      = ContextCompat.getColor(getContext(), R.color.logic_bulb_hi_off);
        colorBulbTextOff    = ContextCompat.getColor(getContext(), R.color.logic_bulb_text_off);
        colorSwitchTextOff  = ContextCompat.getColor(getContext(), R.color.logic_switch_text_off);
        colorPortBg         = ContextCompat.getColor(getContext(), R.color.logic_port_bg);
        loadExtraColors();
    }

    protected void loadExtraColors() {
    }

    protected Component makeButton(String id, String type,
                                   float x, float y, float w, float h, boolean value) {
        Component c = new Component(id, type, x, y, w, h);
        c.value = value;
        Port out = new Port(id + "_out", id, x + w, y + h / 2f, true);
        c.ports.add(out);
        return c;
    }

    protected Component makeGate(String id, String type,
                                 float x, float y, float w, float h) {
        Component c = new Component(id, type, x, y, w, h);
        if (type.equals("NOT")) {
            Port in  = new Port(id + "_in",  id, x,     y + h / 2f, false);
            Port out = new Port(id + "_out", id, x + w, y + h / 2f, true);
            c.ports.add(in);
            c.ports.add(out);
        } else {
            Port in1 = new Port(id + "_in1", id, x, y + h * 0.30f, false);
            Port in2 = new Port(id + "_in2", id, x, y + h * 0.70f, false);
            Port out = new Port(id + "_out", id, x + w, y + h / 2f, true);
            c.ports.add(in1);
            c.ports.add(in2);
            c.ports.add(out);
        }
        return c;
    }

    protected Component makeBulb(float x, float y, float size) {
        Component c = new Component("bulb", "BULB", x, y, size, size);
        Port in = new Port("bulb" + "_in", "bulb", x, y + size / 2f, false);
        c.ports.add(in);
        return c;
    }

    protected void addAll(Component... comps) {
        for (Component c : comps) {
            components.add(c);
            allPorts.addAll(c.ports);
        }
    }

    protected void evaluateCircuit() {
        for (Component c : components) {
            if (!c.type.startsWith("BUTTON")) c.value = false;
        }

        for (int pass = 0; pass < 10; pass++) {
            for (Wire wire : wires) {
                Component fromComp = findComponentById(wire.from.componentId);
                Component toComp   = findComponentById(wire.to.componentId);
                if (fromComp == null || toComp == null) continue;

                switch (toComp.type) {
                    case "AND":
                        toComp.value = getInputValue(toComp, "in1") && getInputValue(toComp, "in2");
                        break;
                    case "OR":
                        toComp.value = getInputValue(toComp, "in1") || getInputValue(toComp, "in2");
                        break;
                    case "NOT":
                        toComp.value = !fromComp.value;
                        break;
                    case "BULB":
                        toComp.value = fromComp.value;
                        break;
                }
                wire.active = fromComp.value;
            }
        }

        if (onCircuitChanged != null) onCircuitChanged.run();
        invalidate();
    }

    protected boolean getInputValue(Component gate, String suffix) {
        for (Wire wire : wires) {
            if (wire.to.componentId.equals(gate.id) && wire.to.id.contains(suffix)) {
                Component from = findComponentById(wire.from.componentId);
                return from != null && from.value;
            }
        }
        return false;
    }

    protected Component findComponentById(String id) {
        for (Component c : components) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }

    protected Port findPort(float x, float y, float radius) {
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

    public boolean getBulbState() {
        Component bulb = findComponentById("bulb");
        return bulb != null && bulb.value;
    }

    protected void drawWires(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setPathEffect(null);
        for (Wire wire : wires) {
            paint.setColor(wire.active ? colorWireOn : colorWireOff);
            Path path = new Path();
            float midX = (wire.from.x + wire.to.x) / 2f;
            path.moveTo(wire.from.x, wire.from.y);
            path.cubicTo(midX, wire.from.y, midX, wire.to.y, wire.to.x, wire.to.y);
            canvas.drawPath(path, paint);
        }
    }

    protected void drawDragWire(Canvas canvas) {
        if (dragFromPort == null) return;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(colorWireDrag);
        paint.setPathEffect(new android.graphics.DashPathEffect(new float[]{12f, 8f}, 0));
        Path path = new Path();
        float midX = (dragFromPort.x + dragX) / 2f;
        path.moveTo(dragFromPort.x, dragFromPort.y);
        path.cubicTo(midX, dragFromPort.y, midX, dragY, dragX, dragY);
        canvas.drawPath(path, paint);
        paint.setPathEffect(null);
    }

    protected void drawPorts(Canvas canvas) {
        for (Port p : allPorts) {
            boolean isHovered    = p == hoveredPort;
            boolean isDragSource = p == dragFromPort;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(isHovered || isDragSource ? colorPortHover : colorPort);
            canvas.drawCircle(p.x, p.y, 10f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setColor(colorPortBg);
            canvas.drawCircle(p.x, p.y, 10f, paint);
        }
    }

    protected void drawBulb(Canvas canvas, Component c) {
        float cx = c.x + c.w / 2f;
        float cy = c.y + c.h / 2f;
        float r  = c.w / 2f;

        if (c.value) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colorBulbGlow);
            canvas.drawCircle(cx, cy, r * 1.4f, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(c.value ? colorBulbInnerOn : colorBulbInnerOff);
        canvas.drawCircle(cx, cy, r, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(c.value ? colorBulbOn : colorBulbOff);
        canvas.drawCircle(cx, cy, r, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(c.value ? colorBulbHiOn : colorBulbHiOff);
        canvas.drawCircle(cx, cy - r * 0.1f, r * 0.55f, paint);

        paint.setTextSize(r * 0.38f);
        paint.setColor(c.value ? colorBulbOn : colorBulbTextOff);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(c.value ? "ON" : "OFF", cx, cy + r + r * 0.4f, paint);
    }

    protected void drawComponent(Canvas canvas, Component c) {
        switch (c.type) {
            case "BUTTON_A":
            case "BUTTON_B":
            case "BUTTON_C":
            case "BUTTON_D":
                drawButton(canvas, c);
                break;
            case "AND":
            case "OR":
            case "NOT":
                drawGate(canvas, c);
                break;
            case "BULB":
                drawBulb(canvas, c);
                break;
        }
    }

    protected abstract void drawButton(Canvas canvas, Component c);

    protected abstract void drawGate(Canvas canvas, Component c);
}
