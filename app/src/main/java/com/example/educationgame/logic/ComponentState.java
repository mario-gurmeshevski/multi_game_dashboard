package com.example.educationgame.logic;

public class ComponentState {
    private boolean inputA;
    private boolean inputB;
    private LogicEngine.GateType gateType;

    public ComponentState(LogicEngine.GateType gateType) {
        this.gateType = gateType;
        this.inputA = false;
        this.inputB = false;
    }

    public void setInputA(boolean val) { this.inputA = val; }
    public void setInputB(boolean val) { this.inputB = val; }

    public boolean getInputA() { return inputA; }
    public boolean getInputB() { return inputB; }
    public LogicEngine.GateType getGateType() { return gateType; }

    public boolean getOutput() {
        return LogicEngine.evaluate(gateType, inputA, inputB);
    }
}