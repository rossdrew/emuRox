package com.rox.emu.P6502;

/**
 * @author rossdrew
 */
public class InstructionSet {
    public static final int OP_ADC_I = 0x69;   //ADC Immediate
    public static final int OP_LDA_Z = 0xA5;   //LDA (Zero Page)
    public static final int OP_LDA_I = 0xA9;   //... Immediate
    public static final int OP_LDA_A = 0xAD;   //... Absolute
    public static final int OP_LDA_Z_IX = 0xB5;//... Zero Page indexed with X
    public static final int OP_LDA_IY = 0xB9;  //... Indexed with Y
    public static final int OP_LDA_IX = 0xBD;  //... Indexed with X
    public static final int OP_AND_I = 0x29;   //AND Immediate
    public static final int OP_OR_I = 0x09;    //OR Immediate
    public static final int OP_EOR_I = 0x49;   //EOR Immediate
    public static final int OP_SBC_I = 0xE9;   //SBX Immediate
    public static final int OP_SEC = 0x38;     //SEC (Implied)
    public static final int OP_LDY_I = 0xA0;   //LDX Immediate
    public static final int OP_LDX_I = 0xA2;   //LDX Immediate
}
