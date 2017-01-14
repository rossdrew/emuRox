package com.rox.emu.P6502;

/**
 * A representation of the 6502 instruction set.
 *
 * This isn't a very OO way to do things and is actually kinda
 * bad coding but it makes life so much easier to have this in
 * a separate file which essentially mimics a struct-type data
 * structure
 *
 * @author Ross Drew
 */
public class InstructionSet {
    public static final int OP_ASL_A = 0x0A;
    public static final int OP_ASL_Z = 0x06;
    public static final int OP_ASL_ABS = 0x0E;
    public static final int OP_ASL_Z_IX = 0x16;
    public static final int OP_ASL_ABS_IX = 0x1E;
    public static final int OP_LSR_A = 0x4A;
    public static final int OP_LSR_Z = 0x56;
    public static final int OP_ADC_Z = 0x65;
    public static final int OP_ADC_I = 0x69;
    public static final int OP_ADC_ABS = 0x6D;
    public static final int OP_ADC_Z_IX = 0x75;
    public static final int OP_LDA_Z = 0xA5;
    public static final int OP_LDA_I = 0xA9;
    public static final int OP_LDA_ABS = 0xAD;
    public static final int OP_LDA_Z_IX = 0xB5;
    public static final int OP_LDA_IY = 0xB9;
    public static final int OP_LDA_IX = 0xBD;
    public static final int OP_CLV = 0xB8;
    public static final int OP_AND_Z = 0x25;
    public static final int OP_AND_ABS = 0x2D;
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
    public static final int OP_STA_ABS = 0x8D;
    public static final int OP_STA_Z_IX = 0x95;
    public static final int OP_STA_ABS_IX = 0x9D;
    public static final int OP_STX_Z = 0x86;
    public static final int OP_INY = 0xC8;
    public static final int OP_DEY = 0x88;
    public static final int OP_INX = 0xE8;
    public static final int OP_DEX = 0xCA;
    public static final int OP_PHA = 0x48;
    public static final int OP_PLA = 0x68;
    public static final int OP_NOP = 0xEA;
    public static final int OP_JMP_ABS = 0x4C;
    public static final int OP_TAX = 0xAA;
    public static final int OP_TAY = 0xA8;
    public static final int OP_TYA = 0x98;
    public static final int OP_TXA = 0x8A;
    public static final int OP_TXS = 0x9A;
    public static final int OP_TSX = 0xBA;
    public static final int OP_BIT_Z = 0x24;

    public static final int OP_BPL = 0x10;
    public static final int OP_BMI = 0x30;
    public static final int OP_BVC = 0x50;
    public static final int OP_BVS = 0x70;
    public static final int OP_BCC = 0x90;
    public static final int OP_BCS = 0xB0;
    public static final int OP_BNE = 0xD0;
    public static final int OP_BEQ = 0xF0;

    public static final int OP_ROL_A = 0x2A;
    public static final int OP_ROL_Z = 0x26;

    public static final int[] instructionSet = {OP_ASL_A, OP_ASL_Z, OP_ASL_ABS, OP_ASL_Z_IX, OP_ASL_ABS_IX, OP_LSR_A, OP_LSR_Z, OP_ROL_A, OP_ROL_Z,
                                                OP_ADC_Z, OP_ADC_I, OP_ADC_ABS, OP_ADC_Z_IX,
                                                OP_SBC_I,
                                                OP_LDA_Z, OP_LDA_I, OP_LDA_ABS, OP_LDA_Z_IX, OP_LDA_IY, OP_LDA_IX,
                                                OP_STA_Z, OP_STA_ABS, OP_STA_Z_IX, OP_STA_ABS_IX, OP_STX_Z, OP_STY_Z,
                                                OP_AND_I, OP_AND_Z, OP_AND_ABS, OP_ORA_I, OP_EOR_I, OP_BIT_Z,
                                                OP_SEC, OP_CLC, OP_CLV, OP_PHA, OP_PLA,
                                                OP_LDY_I, OP_INY, OP_DEY,
                                                OP_LDX_I, OP_INX, OP_DEX,
                                                OP_NOP, OP_JMP_ABS, OP_BCC, OP_BCS, OP_BNE, OP_BEQ, OP_BMI, OP_BPL, OP_BVS, OP_BVC,
                                                OP_TAX, OP_TAY, OP_TYA, OP_TXA, OP_TXS, OP_TSX};

    public static String getName(int opCode){
        switch (opCode){
            case OP_ASL_A:      return "ASL (Accumulator)";
            case OP_ASL_Z:      return "ASL (Zero Page)";
            case OP_ASL_ABS:    return "ASL (Absolute)";
            case OP_ASL_Z_IX:   return "ASL (Zero Page[X])";
            case OP_ASL_ABS_IX: return "ASL (Absolute[X])";
            case OP_LSR_A:      return "LSR (Accumulator)";
            case OP_LSR_Z:      return "LSR (Zero Page)";
            case OP_ROL_A:      return "ROL (Accumulator)";
            case OP_ROL_Z:      return "ROL (Zero Page)";
            case OP_ADC_Z:      return "ADC (Zero Page)";
            case OP_ADC_I:      return "ADC (Immediate)";
            case OP_ADC_ABS:    return "ADC (Absolute)";
            case OP_ADC_Z_IX:   return "ADC (Zero Page[X]";
            case OP_LDA_Z:      return "LDA (Zero Page)";
            case OP_LDA_I:      return "LDA (Immediate)";
            case OP_LDA_ABS:    return "LDA (Absolute)";
            case OP_LDA_Z_IX:   return "LDA (Zero Page[X])";
            case OP_CLV:        return "Clear Overflow";
            case OP_LDA_IY:     return "LDA ([Y])";
            case OP_LDA_IX:     return "LDA ([X])";
            case OP_AND_I:      return "AND (Immediate)";
            case OP_AND_Z:      return "AND (Zero Page)";
            case OP_AND_ABS:    return "AND (Absolute)";
            case OP_BIT_Z:      return "BIT (Zero Page)";
            case OP_ORA_I:      return "OR (Immediate)";
            case OP_EOR_I:      return "EOR (Immediate)";
            case OP_SBC_I:      return "SBX (Immediate)";
            case OP_CLC:        return "Clear Carry";
            case OP_SEC:        return "SEC";
            case OP_LDY_I:      return "LDX (Immediate)";
            case OP_LDX_I:      return "LDX (Immediate)";
            case OP_STY_Z:      return "STY (Zero Page)";
            case OP_STA_Z:      return "STA (Zero Page)";
            case OP_STA_ABS:    return "STA (Absolute)";
            case OP_STA_Z_IX:   return "STA (Zero Page[X])";
            case OP_STA_ABS_IX: return "STA (Absolute[X])";
            case OP_STX_Z:      return "STX (Zero Page)";
            case OP_INY:        return "Increment Y";
            case OP_DEY:        return "Decrement Y";
            case OP_INX:        return "Increment X";
            case OP_DEX:        return "Decrement X";
            case OP_PHA:        return "Push Accumulator";
            case OP_PLA:        return "Pull Accumulator";
            case OP_JMP_ABS:    return "JMP (Absolute)";
            case OP_BCC:        return "BCC 'Branch on Carry Clear'";
            case OP_BCS:        return "BCS 'Branch on Carry Set'";
            case OP_BNE:        return "BNE 'Branch if NOT equal'";
            case OP_BEQ:        return "BEQ 'Branch if Equal'";
            case OP_BMI:        return "BMI 'Branch if Minus'";
            case OP_BPL:        return "BPL 'Branch on Plus'";
            case OP_BVS:        return "BVS 'Branch on Overflow Set'";
            case OP_BVC:        return "BVC 'Branch on Overflow Clear'";
            case OP_TAX:        return "TAX 'A->X";
            case OP_TAY:        return "TAX 'A->Y";
            case OP_TYA:        return "TYA 'Y->A'";
            case OP_TXA:        return "TYA 'X->A'";
            case OP_TXS:        return "TXS 'X->SP'";
            case OP_TSX:        return "TSX 'SP->X'";
            case OP_NOP:        return "NOP - No Operation";
            default:
                return "<Unknown Opcode: " + opCode + ">";
        }
    }
}
