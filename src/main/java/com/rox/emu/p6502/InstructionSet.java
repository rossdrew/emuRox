package com.rox.emu.p6502;

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
    public static final int OP_BRK = 0x00;

    public static final int OP_ASL_A = 0x0A;
    public static final int OP_ASL_Z = 0x06;
    public static final int OP_ASL_ABS = 0x0E;
    public static final int OP_ASL_Z_IX = 0x16;
    public static final int OP_ASL_ABS_IX = 0x1E;
    public static final int OP_LSR_A = 0x4A;
    public static final int OP_LSR_Z = 0x46;
    public static final int OP_LSR_Z_IX = 0x56;
    public static final int OP_LSR_ABS = 0x4E;
    public static final int OP_LSR_ABS_IX = 0x5E;
    public static final int OP_ADC_Z = 0x65;
    public static final int OP_ADC_I = 0x69;
    public static final int OP_ADC_ABS = 0x6D;
    public static final int OP_ADC_ABS_IX = 0x7D;
    public static final int OP_ADC_ABS_IY = 0x79;
    public static final int OP_ADC_Z_IX = 0x75;
    public static final int OP_ADC_IND_IX = 0x61;
    public static final int OP_LDA_Z = 0xA5;
    public static final int OP_LDA_I = 0xA9;
    public static final int OP_LDA_ABS = 0xAD;
    public static final int OP_LDA_Z_IX = 0xB5;
    public static final int OP_LDA_ABS_IY = 0xB9;
    public static final int OP_LDA_IND_IX = 0xA1;
    public static final int OP_LDA_ABS_IX = 0xBD;
    public static final int OP_CLV = 0xB8;
    public static final int OP_AND_Z = 0x25;
    public static final int OP_AND_Z_IX = 0x35;
    public static final int OP_AND_ABS_IX = 0x3D;
    public static final int OP_AND_ABS_IY = 0x39;
    public static final int OP_AND_ABS = 0x2D;
    public static final int OP_AND_I = 0x29;
    public static final int OP_AND_IND_IX = 0x21;
    public static final int OP_ORA_I = 0x09;
    public static final int OP_ORA_Z = 0x05;
    public static final int OP_ORA_Z_IX = 0x15;
    public static final int OP_ORA_ABS = 0x0D;
    public static final int OP_ORA_ABS_IX = 0x1D;
    public static final int OP_ORA_ABS_IY = 0x19;
    public static final int OP_ORA_IND_IX = 0x01;
    public static final int OP_EOR_I = 0x49;
    public static final int OP_EOR_Z = 0x45;
    public static final int OP_EOR_Z_IX = 0x55;
    public static final int OP_EOR_ABS = 0x4D;
    public static final int OP_EOR_ABS_IX = 0x5D;
    public static final int OP_EOR_ABS_IY = 0x59;
    public static final int OP_EOR_IND_IX = 0x41;
    public static final int OP_SBC_I = 0xE9;
    public static final int OP_SBC_Z = 0xE5;
    public static final int OP_SBC_Z_IX = 0xF5;
    public static final int OP_SBC_ABS = 0xED;
    public static final int OP_SBC_ABS_IX = 0xFD;
    public static final int OP_SBC_ABS_IY = 0xF9;
    public static final int OP_SBC_IND_IX = 0xE1;
    public static final int OP_CLC = 0x18;
    public static final int OP_SEC = 0x38;
    public static final int OP_LDY_I = 0xA0;
    public static final int OP_LDY_Z = 0xA4;
    public static final int OP_LDY_Z_IX = 0xB4;
    public static final int OP_LDY_ABS = 0xAC;
    public static final int OP_LDY_ABS_IX = 0xBC;
    public static final int OP_LDX_I = 0xA2;
    public static final int OP_LDX_ABS = 0xAE;
    public static final int OP_LDX_ABS_IY = 0xBE;
    public static final int OP_LDX_Z = 0xA6;
    public static final int OP_LDX_Z_IY = 0xB6;
    public static final int OP_STY_Z = 0x84;
    public static final int OP_STY_ABS = 0x8C;
    public static final int OP_STY_Z_IX = 0x94;
    public static final int OP_STA_Z = 0x85;
    public static final int OP_STA_ABS = 0x8D;
    public static final int OP_STA_Z_IX = 0x95;
    public static final int OP_STA_ABS_IX = 0x9D;
    public static final int OP_STA_ABS_IY = 0x99;
    public static final int OP_STA_IND_IX = 0x81;
    public static final int OP_STA_IND_IY = 0x91;
    public static final int OP_STX_Z = 0x86;
    public static final int OP_STX_Z_IY = 0x96;
    public static final int OP_STX_ABS = 0x8E;
    public static final int OP_INY = 0xC8;
    public static final int OP_INC_Z = 0xE6;
    public static final int OP_INC_Z_IX = 0xF6;
    public static final int OP_INC_ABS = 0xEE;
    public static final int OP_INC_ABS_IX = 0xFE;
    public static final int OP_DEC_Z = 0xC6;
    public static final int OP_DEC_Z_IX = 0xD6;
    public static final int OP_DEC_ABS = 0xCE;
    public static final int OP_DEC_ABS_IX = 0xDE;
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
    public static final int OP_BIT_ABS = 0x2C;

    public static final int OP_CMP_I = 0xC9;
    public static final int OP_CMP_Z = 0xC5;
    public static final int OP_CMP_Z_IX = 0xD5;
    public static final int OP_CMP_ABS = 0xCD;
    public static final int OP_CMP_ABS_IX = 0xDD;
    public static final int OP_CMP_ABS_IY = 0xD9;
    public static final int OP_CMP_IND_IX = 0xC1;
    public static final int OP_CPX_I = 0xE0;
    public static final int OP_CPX_Z = 0xE4;
    public static final int OP_CPX_ABS = 0xEC;
    public static final int OP_CPY_I = 0xC0;
    public static final int OP_CPY_Z = 0xC4;
    public static final int OP_CPY_ABS = 0xCC;
    public static final int OP_PHP = 0x08;
    public static final int OP_PLP = 0x28;

    public static final int OP_JSR = 0x20;
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
    public static final int OP_ROL_Z_IX = 0x36;
    public static final int OP_ROL_ABS = 0x2E;
    public static final int OP_ROL_ABS_IX = 0x3E;

    public static final int OP_ROR_A = 0x6A;

    public static final int OP_CLI = 0x58;
    public static final int OP_SEI = 0x78;
    public static final int OP_SED = 0xF8;
    public static final int OP_CLD = 0xD8;

    public static final int OP_RTS = 0x60;

    public static final int[] instructionSet = {OP_BRK,
                                                OP_ASL_A, OP_ASL_Z, OP_ASL_ABS, OP_ASL_Z_IX, OP_ASL_ABS_IX, OP_LSR_A, OP_LSR_Z, OP_LSR_Z_IX, OP_LSR_ABS, OP_LSR_ABS_IX,OP_ROL_A, OP_ROL_Z, OP_ROL_Z_IX, OP_ROL_ABS, OP_ROL_ABS_IX, OP_ROR_A,
                                                OP_ADC_Z, OP_ADC_I, OP_ADC_ABS, OP_ADC_ABS_IX, OP_ADC_ABS_IY, OP_ADC_Z_IX, OP_ADC_IND_IX,
                                                OP_SBC_I, OP_SBC_Z, OP_SBC_Z_IX, OP_SBC_ABS, OP_SBC_ABS_IX, OP_SBC_ABS_IY, OP_SBC_IND_IX,
                                                OP_LDA_Z, OP_LDA_I, OP_LDA_ABS, OP_LDA_Z_IX, OP_LDA_ABS_IY, OP_LDA_IND_IX, OP_LDA_ABS_IX,
                                                OP_STA_Z, OP_STA_ABS, OP_STA_Z_IX, OP_STA_ABS_IX, OP_STA_ABS_IY, OP_STA_IND_IX, OP_STA_IND_IY, OP_STX_Z, OP_STX_Z_IY, OP_STX_ABS, OP_STY_Z, OP_STY_ABS, OP_STY_Z_IX,
                                                OP_AND_I, OP_AND_IND_IX, OP_AND_Z, OP_AND_Z_IX, OP_AND_ABS_IX, OP_AND_ABS_IY, OP_AND_ABS, OP_ORA_I, OP_ORA_Z, OP_ORA_Z_IX, OP_ORA_ABS, OP_ORA_ABS_IX, OP_ORA_ABS_IY, OP_ORA_IND_IX, OP_EOR_I, OP_EOR_Z, OP_EOR_Z_IX, OP_EOR_ABS, OP_EOR_ABS_IX, OP_EOR_ABS_IY, OP_BIT_Z, OP_BIT_ABS,
                                                OP_SEC, OP_CLC, OP_CLV, OP_PHA, OP_PLA,
                                                OP_LDY_I, OP_LDY_Z, OP_LDY_Z_IX, OP_LDY_ABS, OP_LDY_ABS_IX,OP_INY, OP_INC_Z, OP_INC_Z_IX, OP_INC_ABS, OP_INC_ABS_IX, OP_DEC_Z, OP_DEC_Z_IX, OP_DEC_ABS, OP_DEC_ABS_IX, OP_DEY,
                                                OP_LDX_I, OP_LDX_Z, OP_LDX_Z_IY, OP_LDX_ABS, OP_LDX_ABS_IY, OP_INX, OP_DEX,
                                                OP_NOP, OP_JMP_ABS, OP_BCC, OP_BCS, OP_BNE, OP_BEQ, OP_BMI, OP_JSR, OP_BPL, OP_BVS, OP_BVC,
                                                OP_TAX, OP_TAY, OP_TYA, OP_TXA, OP_TXS, OP_TSX,
                                                OP_CMP_I, OP_CMP_Z, OP_CMP_Z_IX, OP_CMP_ABS, OP_CMP_ABS_IX, OP_CMP_ABS_IY, OP_CMP_IND_IX, OP_CPX_I, OP_CPX_Z, OP_CPX_ABS, OP_CPY_I, OP_CPY_Z, OP_CPY_ABS, OP_PHP, OP_PLP,
                                                OP_CLI, OP_SEI, OP_SED, OP_CLD, OP_RTS};

    public static String getOpCodeName(int opCode){
        switch (opCode){
            case OP_BRK:        return "BRK";

            case OP_ASL_A:      return "ASL (Accumulator)";
            case OP_ASL_Z:      return "ASL (Zero Page)";
            case OP_ASL_ABS:    return "ASL (Absolute)";
            case OP_ASL_Z_IX:   return "ASL (Zero Page[X])";
            case OP_ASL_ABS_IX: return "ASL (Absolute[X])";
            case OP_LSR_A:      return "LSR (Accumulator)";
            case OP_LSR_Z:      return "LSR (Zero Page)";
            case OP_LSR_Z_IX:   return "LSR (Zero Page[X])";
            case OP_LSR_ABS:    return "LSR (Absolute)";
            case OP_LSR_ABS_IX: return "LSR (Absolute[X])";
            case OP_ROL_A:      return "ROL (Accumulator)";
            case OP_ROL_Z:      return "ROL (Zero Page)";
            case OP_ROL_Z_IX:   return "ROL (Zero Page[X])";
            case OP_ROL_ABS:    return "ROL (Absolute)";
            case OP_ROL_ABS_IX: return "ROL (Absolute[X])";
            case OP_ROR_A:      return "ROR (Accumulator)";
            case OP_ADC_Z:      return "ADC (Zero Page)";
            case OP_ADC_I:      return "ADC (Immediate)";
            case OP_ADC_ABS:    return "ADC (Absolute)";
            case OP_ADC_ABS_IX: return "ADC (Absolute[X])";
            case OP_ADC_ABS_IY: return "ADC (Absolute[Y])";
            case OP_ADC_Z_IX:   return "ADC (Zero Page[X]";
            case OP_ADC_IND_IX: return "ADC (Indirect, X)";
            case OP_LDA_Z:      return "LDA (Zero Page)";
            case OP_LDA_I:      return "LDA (Immediate)";
            case OP_LDA_ABS:    return "LDA (Absolute)";
            case OP_LDA_Z_IX:   return "LDA (Zero Page[X])";
            case OP_CLV:        return "Clear Overflow";
            case OP_LDA_ABS_IY: return "LDA ([Y])";
            case OP_LDA_IND_IX: return "LDA (Indirect, X)";
            case OP_LDA_ABS_IX: return "LDA ([X])";
            case OP_AND_I:      return "AND (Immediate)";
            case OP_AND_Z:      return "AND (Zero Page)";
            case OP_AND_Z_IX:   return "AND (Zero Page[X])";
            case OP_AND_ABS_IX: return "AND (Absolute[X])";
            case OP_AND_ABS_IY: return "AND (Absolute[Y])";
            case OP_AND_ABS:    return "AND (Absolute)";
            case OP_AND_IND_IX: return "AND (Indirect, X)";
            case OP_BIT_Z:      return "BIT (Zero Page)";
            case OP_BIT_ABS:    return "BIT (Absolute)";
            case OP_CMP_I:      return "CMP (Immediate)";
            case OP_CMP_Z:      return "CMP (Zero Page)";
            case OP_CMP_Z_IX:   return "CMP (Zero Page[X])";
            case OP_CMP_ABS:    return "CMP (Absolute)";
            case OP_CMP_ABS_IX: return "CMP (Absolute[X])";
            case OP_CMP_ABS_IY: return "CMP (Absolute[Y])";
            case OP_CMP_IND_IX: return "CMP (Indirect, X)";
            case OP_CPX_I:      return "CPX (Immediate)";
            case OP_CPX_Z:      return "CPX (Zero Page)";
            case OP_CPX_ABS:    return "CPX (Absolute)";
            case OP_CPY_I:      return "CPY (Immediate)";
            case OP_CPY_Z:      return "CPY (Zero Page)";
            case OP_CPY_ABS:    return "CPY (Absolute)";
            case OP_PHP:        return "PHP";
            case OP_PLP:        return "PLP";
            case OP_ORA_I:      return "OR (Immediate)";
            case OP_ORA_Z:      return "OR (Zero Page)";
            case OP_ORA_Z_IX:   return "OR (Zero Page[X])";
            case OP_ORA_ABS:    return "OR (Absolute)";
            case OP_ORA_ABS_IX: return "OR (Absolute[X])";
            case OP_ORA_ABS_IY: return "OR (Absolute[Y])";
            case OP_ORA_IND_IX: return "OR (Indirect, X)";
            case OP_EOR_I:      return "EOR (Immediate)";
            case OP_EOR_Z:      return "EOR (Zero Page)";
            case OP_EOR_Z_IX:   return "EOR (Zero Page[X])";
            case OP_EOR_ABS:    return "EOR (Absolute)";
            case OP_EOR_ABS_IX: return "EOR (Absolute[X])";
            case OP_EOR_ABS_IY: return "EOR (Absolute[Y])";
            case OP_SBC_I:      return "SBC (Immediate)";
            case OP_SBC_Z:      return "SBC (Zero Page)";
            case OP_SBC_Z_IX:   return "SBC (Zero Page[X])";
            case OP_SBC_ABS:    return "SBC (Absolute)";
            case OP_SBC_ABS_IX: return "SBC (Absolute[X])";
            case OP_SBC_ABS_IY: return "SBC (Absolute[Y])";
            case OP_SBC_IND_IX: return "SBC (Indirect, X)";
            case OP_CLC:        return "Clear Carry";
            case OP_SEC:        return "SEC";
            case OP_LDY_I:      return "LDX (Immediate)";
            case OP_LDY_Z:      return "LDX (Zero Page)";
            case OP_LDY_Z_IX:   return "LDX (Zero Page[X])";
            case OP_LDY_ABS:    return "LDY (Absolute)";
            case OP_LDY_ABS_IX: return "LDY (Absolute[X])";
            case OP_LDX_I:      return "LDX (Immediate)";
            case OP_LDX_Z:      return "LDX (Zero Page)";
            case OP_LDX_Z_IY:   return "LDX (Zero Page[Y])";
            case OP_LDX_ABS:    return "LDX (Absolute)";
            case OP_LDX_ABS_IY: return "LDX (Absolute[Y])";
            case OP_STY_Z:      return "STY (Zero Page)";
            case OP_STY_ABS:    return "STY (Absolute)";
            case OP_STY_Z_IX:   return "STY (Zero Page[X])";
            case OP_STA_Z:      return "STA (Zero Page)";
            case OP_STA_ABS:    return "STA (Absolute)";
            case OP_STA_Z_IX:   return "STA (Zero Page[X])";
            case OP_STA_ABS_IX: return "STA (Absolute[X])";
            case OP_STA_ABS_IY: return "STA (Absolute[Y])";
            case OP_STA_IND_IX: return "STA (Indirect, X)";
            case OP_STA_IND_IY: return "STA (Indirect, Y)";
            case OP_STX_Z:      return "STX (Zero Page)";
            case OP_STX_Z_IY:   return "STX (Zero Page[Y])";
            case OP_STX_ABS:    return "STX (Absolute)";
            case OP_INC_Z:      return "Increment (Zero Page)";
            case OP_INC_Z_IX:   return "Increment (Zero Page[X])";
            case OP_INC_ABS:    return "Increment (Absolute)";
            case OP_INC_ABS_IX: return "Increment (Absolute[X])";
            case OP_DEC_Z:      return "Decrement (Zero Page)";
            case OP_DEC_Z_IX:   return "Decrement (Zero Page[X])";
            case OP_DEC_ABS:    return "Decrement (Absolute)";
            case OP_DEC_ABS_IX: return "Decrement (Absolute[X])";
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
            case OP_JSR:        return "JSR";
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

            case OP_CLI:        return "CLI";
            case OP_SEI:        return "SEI";
            case OP_SED:        return "SED";
            case OP_CLD:        return "CLD";

            case OP_RTS:        return "RTS";
            default:
                return "<Unknown Opcode: " + opCode + ">";
        }
    }
}
