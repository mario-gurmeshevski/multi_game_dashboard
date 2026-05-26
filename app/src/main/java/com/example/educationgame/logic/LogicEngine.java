package com.example.educationgame.logic;

public class LogicEngine {

    public enum GateType {
        AND, OR, NOT
    }

    public static boolean evaluate(GateType gate, boolean inputA, boolean inputB) {
        switch (gate) {
            case AND:    return inputA && inputB;
            case OR:     return inputA || inputB;
            case NOT:    return !inputA;
            default:     return false;
        }
    }


}