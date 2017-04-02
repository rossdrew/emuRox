package com.rox.emu.p6502.op;

public enum OpCode {
            OP_ASL_A(0x0A),
            OP_ASL_Z(0x06),
            OP_ASL_ABS(0x0E),
            OP_ASL_Z_IX(0x16),
            OP_ASL_ABS_IX(0x1E),

            OP_LSR_A(0x4A),
            OP_LSR_Z(0x46),
            OP_LSR_Z_IX(0x56),
            OP_LSR_ABS(0x4E),
            OP_LSR_ABS_IX(0x5E),

            OP_ADC_Z(0x65),
            OP_ADC_I(0x69),
            OP_ADC_ABS(0x6D),
            OP_ADC_ABS_IX(0x7D),
            OP_ADC_ABS_IY(0x79),
            OP_ADC_Z_IX(0x75),

            OP_LDA_Z(0xA5),
            OP_LDA_I(0xA9),
            OP_LDA_ABS(0xAD),
            OP_LDA_Z_IX(0xB5),
            OP_LDA_ABS_IY(0xB9),
            OP_LDA_IND_IX(0xA1),
            OP_LDA_ABS_IX(0xBD),

            OP_CLV(0xB8),

            OP_AND_Z(0x25),
            OP_AND_Z_IX(0x35),
            OP_AND_ABS_IX(0x3D),
            OP_AND_ABS_IY(0x39),
            OP_AND_ABS(0x2D),
            OP_AND_I(0x29),
            OP_AND_IND_IX(21),

            OP_ORA_I(0x09),
            OP_ORA_Z(0x05),
            OP_ORA_Z_IX(0x15),
            OP_ORA_ABS(0x0D),
            OP_ORA_ABS_IX(0x1D),
            OP_ORA_ABS_IY(0x19),

            OP_EOR_I(0x49),
            OP_EOR_Z(0x45),
            OP_EOR_Z_IX(0x55),
            OP_EOR_ABS(0x4D),
            OP_EOR_ABS_IX(0x5D),
            OP_EOR_ABS_IY(0x59),

            OP_SBC_I(0xE9),
            OP_SBC_Z(0xE5),
            OP_SBC_Z_IX(0xF5),
            OP_SBC_ABS(0xED),
            OP_SBC_ABS_IX(0xFD),
            OP_SBC_ABS_IY(0xF9),

            OP_CLC(0x18),
            OP_SEC(0x38),

            OP_LDY_I(0xA0),
            OP_LDY_Z(0xA4),
            OP_LDY_Z_IX(0xB4),
            OP_LDY_ABS(0xAC),
            OP_LDY_ABS_IX(0xBC),

            OP_LDX_I(0xA2),
            OP_LDX_ABS(0xAE),
            OP_LDX_ABS_IY(0xBE),
            OP_LDX_Z(0xA6),
            OP_LDX_Z_IY(0xB6),

            OP_STY_Z(0x84),
            OP_STY_ABS(0x8C),
            OP_STY_Z_IX(0x94),

            OP_STA_Z(0x85),
            OP_STA_ABS(0x8D),
            OP_STA_Z_IX(0x95),
            OP_STA_ABS_IX(0x9D),
            OP_STA_ABS_IY(0x99),

            OP_STX_Z(0x86),
            OP_STX_Z_IY(0x96),
            OP_STX_ABS(0x8E),

            OP_INY(0xC8),
            OP_INX(0xE8),
            OP_DEX(0xCA),

            OP_INC_Z(0xE6),
            OP_INC_Z_IX(0xF6),
            OP_INC_ABS(0xEE),
            OP_INC_ABS_IX(0xFE),

            OP_DEC_Z(0xC6),
            OP_DEC_Z_IX(0xD6),
            OP_DEC_ABS(0xCE),
            OP_DEC_ABS_IX(0xDE),
            OP_DEY(0x88),

            OP_PHA(0x48),
            OP_PLA(0x68),
            OP_PHP(0x08),
            OP_PLP(0x28),

            OP_NOP(0xEA),

            OP_JMP_ABS(0x4C),

            OP_TAX(0xAA),
            OP_TAY(0xA8),
            OP_TYA(0x98),
            OP_TXA(0x8A),
            OP_TXS(0x9A),
            OP_TSX(0xBA),

            OP_BIT_Z(0x24),
            OP_BIT_ABS(0x2C),

            OP_CMP_I(0xC9),
            OP_CMP_Z(0xC5),
            OP_CMP_Z_IX(0xD5),
            OP_CMP_ABS(0xCD),
            OP_CMP_ABS_IX(0xDD),
            OP_CMP_ABS_IY(0xD9),

            OP_CPX_I(0xE0),
            OP_CPX_Z(0xE4),
            OP_CPX_ABS(0xEC),

            OP_CPY_I(0xC0),
            OP_CPY_Z(0xC4),
            OP_CPY_ABS(0xCC),

            OP_JSR(0x20),
            OP_BPL(0x10),
            OP_BMI(0x30),
            OP_BVC(0x50),
            OP_BVS(0x70),
            OP_BCC(0x90),
            OP_BCS(0xB0),
            OP_BNE(0xD0),
            OP_BEQ(0xF0),

            OP_ROL_A(0x2A),
            OP_ROL_Z(0x26),
            OP_ROL_Z_IX(0x36),
            OP_ROL_ABS(0x2E),
            OP_ROL_ABS_IX(0x3E),

            OP_ROR_A(0x6A),

            OP_CLI(0x58),
            OP_SEI(0x78),
            OP_SED(0xF8),
            OP_CLD(0xD8),

            OP_RTS(0x60),
            OP_RTI(0x40);

    private int byteValue;
    private String description;

    OpCode(int byteValue){
        this.byteValue = byteValue;
        description = OpCodeConverter.toDescription(this.name());
        //TODO instead of generating description, generate addressing mode and indexing mode
        //     this way we can tell how many arguments each takes without having to hard code it
        //TODO then switch to this opcode method
    }

    public int getByteValue(){
        return byteValue;
    }

    @Override
    public String toString(){
        return description;
    }
}
