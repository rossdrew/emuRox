package com.rox.emu.P6502;

/**
 * A representation of the 6502 instruction set.
 *
 * XXX
 * This isn't a very OO way to do things and is actually kinda
 * bad coding but it makes life so much easier to have this in
 * a separate file which essentially mimics a struct-type data
 * structure
 *
 * @author Ross Drew
 */
public class InstructionSet {
    public static final int OP_ASL_A = 0x0A;
    public static final int OP_LSR_A = 0x4A;
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
    public static final int OP_ORA_I = 0x09;
    public static final int OP_EOR_I = 0x49;
    public static final int OP_SBC_I = 0xE9;
    public static final int OP_CLC = 0x18;
    public static final int OP_SEC = 0x38;
    public static final int OP_LDY_I = 0xA0;
    public static final int OP_LDX_I = 0xA2;
    public static final int OP_STY_Z = 0x84;
    public static final int OP_STA_Z = 0x85;
    public static final int OP_STX_Z = 0x86;
    public static final int OP_INY = 0xC8;
    public static final int OP_DEY = 0x88;
    public static final int OP_INX = 0xE8;
    public static final int OP_DEX = 0xCA;
    public static final int OP_PHA = 0x48;
    public static final int OP_PLA = 0x68;
    public static final int OP_NOP = 0xEA;
    public static final int OP_JMP_A = 0x4C;
    public static final int OP_BCC = 0x90;

    public static final int[] instructionSet = {OP_ASL_A, OP_LSR_A,
                                                OP_ADC_Z, OP_ADC_I, OP_ADC_A,
                                                OP_SBC_I,
                                                OP_LDA_Z, OP_LDA_I, OP_LDA_A, OP_LDA_Z_IX, OP_LDA_IY, OP_LDA_IX,
                                                OP_STA_Z, OP_STX_Z, OP_STY_Z,
                                                OP_AND_I, OP_ORA_I, OP_EOR_I,
                                                OP_SEC, OP_CLC, OP_CLV, OP_PHA, OP_PLA,
                                                OP_LDY_I, OP_INY, OP_DEY,
                                                OP_LDX_I, OP_INX, OP_DEX,
                                                OP_NOP, OP_JMP_A, OP_BCC};

    public static String getName(int opCode){
        switch (opCode){
            case OP_ASL_A:      return "ASL (Accumulator)";
            case OP_LSR_A:      return "LSR (Accumulator)";
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
            case OP_ORA_I:      return "OR (Immediate)";
            case OP_EOR_I:      return "EOR (Immediate)";
            case OP_SBC_I:      return "SBX (Immediate)";
            case OP_CLC:        return "Clear Carry";
            case OP_SEC:        return "SEC";
            case OP_LDY_I:      return "LDX (Immediate)";
            case OP_LDX_I:      return "LDX (Immediate)";
            case OP_STY_Z:      return "STY (Zero Page)";
            case OP_STA_Z:      return "STA (Zero Page)";
            case OP_STX_Z:      return "STX (Zero Page)";
            case OP_INY:        return "Increment Y";
            case OP_DEY:        return "Decrement Y";
            case OP_INX:        return "Increment X";
            case OP_DEX:        return "Decrement X";
            case OP_PHA:        return "Push Accumulator";
            case OP_PLA:        return "Pull Accumulator";
            case OP_JMP_A:      return "JMP (Absolute)";
            case OP_BCC:        return "BCC";
            case OP_NOP:        return "NOP - No Operation";
            default:
                return "<Unknown Opcode: " + opCode + ">";
        }
    }
}
