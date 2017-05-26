package com.rox.emu.processor.mos6502.op;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.processor.mos6502.CPU;
import com.rox.emu.processor.mos6502.op.util.OpCodeConverter;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Enum representation of {@link CPU} op-codes.  Each represented by an enum name using the convention
 * 'OP{@value #OP_TOKEN_SEPARATOR}CODE{@value #OP_TOKEN_SEPARATOR}ADDR{@value #OP_TOKEN_SEPARATOR}INDX'.
 *
 * @author Ross Drew
 */
public enum OpCode {
    OP_BRK(0x00),
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
    OP_ADC_IND_IX(0x61),
    OP_ADC_IND_IY(0x71),

    OP_LDA_Z(0xA5),
    OP_LDA_I(0xA9),
    OP_LDA_ABS(0xAD),
    OP_LDA_Z_IX(0xB5),
    OP_LDA_ABS_IY(0xB9),
    OP_LDA_IND_IX(0xA1),
    OP_LDA_IND_IY(0xB1),
    OP_LDA_ABS_IX(0xBD),

    OP_CLV(0xB8),

    OP_AND_Z(0x25),
    OP_AND_Z_IX(0x35),
    OP_AND_ABS_IX(0x3D),
    OP_AND_ABS_IY(0x39),
    OP_AND_ABS(0x2D),
    OP_AND_I(0x29),
    OP_AND_IND_IX(0x21),
    OP_AND_IND_IY(0x31),

    OP_ORA_I(0x09),
    OP_ORA_Z(0x05),
    OP_ORA_Z_IX(0x15),
    OP_ORA_ABS(0x0D),
    OP_ORA_ABS_IX(0x1D),
    OP_ORA_ABS_IY(0x19),
    OP_ORA_IND_IX(0x01),
    OP_ORA_IND_IY(0x11),

    OP_EOR_I(0x49),
    OP_EOR_Z(0x45),
    OP_EOR_Z_IX(0x55),
    OP_EOR_ABS(0x4D),
    OP_EOR_ABS_IX(0x5D),
    OP_EOR_ABS_IY(0x59),
    OP_EOR_IND_IX(0x41),
    OP_EOR_IND_IY(0x51),

    OP_SBC_I(0xE9),
    OP_SBC_Z(0xE5),
    OP_SBC_Z_IX(0xF5),
    OP_SBC_ABS(0xED),
    OP_SBC_ABS_IX(0xFD),
    OP_SBC_ABS_IY(0xF9),
    OP_SBC_IND_IX(0xE1),
    OP_SBC_IND_IY(0xF1),

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
    OP_STA_IND_IX(0x81),
    OP_STA_IND_IY(0x91),

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
    OP_JMP_IND(0x6C),

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
    OP_CMP_IND_IX(0xC1),
    OP_CMP_IND_IY(0xD1),

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

    /**
     * The separator used to delimit different elements in the {@link String} enum id
     */
    public static final String OP_TOKEN_SEPARATOR = "_";
    /**
     * The index of the op-code name in the {@link String} enum id,
     * using the token delimiter '{@value OP_TOKEN_SEPARATOR}'
     */
    public static final int OP_CODE_I = 1;
    /**
     * The index of the addressing mode token in the {@link String} enum id,
     * using the token delimiter '{@value OP_TOKEN_SEPARATOR}'
     */
    public static final int OP_ADDR_I = 2;
    /**
     * The index of the indexing mode token in the {@link String} enum id,
     * using the token delimiter '{@value OP_TOKEN_SEPARATOR}'
     */
    public static final int OP_INDX_I = 3;

    private final int byteValue;
    private final String opCodeName;
    private final AddressingMode addressingMode;

    OpCode(int byteValue){
        this.byteValue = byteValue;
        this.addressingMode = OpCodeConverter.getAddressingMode(this.name());
        this.opCodeName = OpCodeConverter.getOpCode(this.name());
    }

    /**
     * Get the OpCode for
     *
     * @param byteValue this byte value
     * @return the OpCode associated with this byte value
     */
    public static OpCode from(int byteValue){
        return from(opcode -> opcode.getByteValue() == byteValue, byteValue);
    }

    /**
     * Get the OpCode for
     *
     * @param opCodeName Three character {@link String} representing an {@link AddressingMode#IMPLIED} addressed OpCode
     * @return The OpCode instance associated with this name in {@link AddressingMode#IMPLIED}
     */
    public static OpCode from(String opCodeName){
        return from(opcode -> opcode.getOpCodeName().equalsIgnoreCase(opCodeName), opCodeName);
    }

    /**
     * Get the OpCode for
     *
     * @param opCodeName Three character {@link String} representing OpCode name
     * @param addressingMode The {@link AddressingMode} of the OpCode
     * @return The OpCode instance associated with this name in this {@link AddressingMode}
     */
    public static OpCode from(String opCodeName, AddressingMode addressingMode){
        return matching(opcode -> opcode.getOpCodeName().equalsIgnoreCase(opCodeName) &&
                                                       opcode.getAddressingMode() == addressingMode,
                    opCodeName + " in " + addressingMode,
                    opCodeName);
    }

    /**
     * @param predicate A predicate used to search {@link OpCode}s
     * @param predicateTerm The main term of {@link Object} used in the predicate
     * @return The first {@link OpCode} found
     * @throws UnknownOpCodeException
     */
    private static OpCode from(Predicate<? super OpCode> predicate, Object predicateTerm) throws UnknownOpCodeException{
        return matching(predicate, ""+predicateTerm, predicateTerm);
    }

    /**
     * @param predicate A predicate used to search {@link OpCode}s
     * @param predicateDescription A {@link String} description of the search
     * @param predicateTerm The main term of {@link Object} used in the predicate
     * @return The first {@link OpCode} found
     * @throws UnknownOpCodeException if no {@link OpCode} matches the given predicate
     */
    private static OpCode matching(Predicate<? super OpCode> predicate, String predicateDescription, Object predicateTerm) throws UnknownOpCodeException{
        Optional<OpCode> result = Arrays.stream(OpCode.values()).filter(predicate).findFirst();

        if (result.isPresent())
            return result.get();

        throw new UnknownOpCodeException("Unknown opcode name while creating OpCode object: " + predicateDescription, predicateTerm);
    }

    public int getByteValue(){
        return byteValue;
    }

    public String getOpCodeName() {return opCodeName;}

    public AddressingMode getAddressingMode(){
        return this.addressingMode;
    }

    public static Stream<OpCode> streamOf(AddressingMode addressingMode){
        return streamOf(opcode -> opcode.getAddressingMode() == addressingMode);
    }

    private static Stream<OpCode> streamOf(Predicate<? super OpCode> predicate){
        return Stream.of(OpCode.values()).filter(predicate);
    }

    @Override
    public String toString(){
        return opCodeName + " (" + addressingMode + ")";
    }
}
