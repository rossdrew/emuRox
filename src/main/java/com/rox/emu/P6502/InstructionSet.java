package com.rox.emu.P6502;

/**
 * @author rossdrew
 */
public class InstructionSet {
    public static final int OP_ADC_Z = 0x65;
    public static final int OP_ADC_I = 0x69;
    public static final int OP_ADC_A = 0x6D;
    public static final int OP_LDA_Z = 0xA5;
    public static final int OP_LDA_I = 0xA9;
    public static final int OP_LDA_A = 0xAD;
    public static final int OP_LDA_Z_IX = 0xB5;
    public static final int OP_LDA_IY = 0xB9;
    public static final int OP_LDA_IX = 0xBD;
    public static final int OP_CLV = 0xB8;
    public static final int OP_AND_I = 0x29;
    public static final int OP_OR_I = 0x09;
    public static final int OP_EOR_I = 0x49;
    public static final int OP_SBC_I = 0xE9;
    public static final int OP_CLC = 0x18;
    public static final int OP_SEC = 0x38;
    public static final int OP_LDY_I = 0xA0;
    public static final int OP_LDX_I = 0xA2;
    public static final int OP_STA_Z = 0x85;
    public static final int OP_INY = 0xC8;
    public static final int OP_DEY = 0x88;
    public static final int OP_INX = 0xE8;
    public static final int OP_DEX = 0xCA;
    public static final int OP_PHA = 0x48;

    public static final int[] instructionSet = {OP_ADC_Z, OP_ADC_I, OP_ADC_A,
                                                OP_SBC_I,
                                                OP_LDA_Z, OP_LDA_I, OP_LDA_A, OP_LDA_Z_IX, OP_LDA_IY, OP_LDA_IX,
                                                OP_STA_Z,
                                                OP_AND_I, OP_OR_I, OP_EOR_I,
                                                OP_SEC, OP_CLC, OP_CLV, OP_PHA,
                                                OP_LDY_I, OP_INY, OP_DEY,
                                                OP_LDX_I, OP_INX, OP_DEX};

    public static String getName(int opCode){
        switch (opCode){
            case OP_ADC_Z:      return "ADC (Zero Page)";
            case OP_ADC_I:      return "ADC (Immediate)";
            case OP_ADC_A:      return "ADC (Absolute)";
            case OP_LDA_Z:      return "LDA (Zero Page)";
            case OP_LDA_I:      return "LDA (Immediate)";
            case OP_LDA_A:      return "LDA (Absolute)";
            case OP_LDA_Z_IX:   return "LDA (Zero Page[X])";
            case OP_CLV:        return "Clear Overflow";
            case OP_LDA_IY:     return "LDA ([Y])";
            case OP_LDA_IX:     return "LDA ([X])";
            case OP_AND_I:      return "AND (Immediate)";
            case OP_OR_I:       return "OR (Immediate)";
            case OP_EOR_I:      return "EOR (Immediate)";
            case OP_SBC_I:      return "SBX (Immediate)";
            case OP_CLC:        return "Clear Carry";
            case OP_SEC:        return "SEC";
            case OP_LDY_I:      return "LDX (Immediate)";
            case OP_LDX_I:      return "LDX (Immediate)";
            case OP_STA_Z:      return "STA (Zero Page)";
            case OP_INY:        return "Increment Y";
            case OP_DEY:        return "Decrement Y";
            case OP_INX:        return "Increment X";
            case OP_DEX:        return "Decrement X";
            case OP_PHA:        return "Push Accumulator";
            default:
                return "<Unknown Opcode: " + opCode + ">";
        }
    }
}
