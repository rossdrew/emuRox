package com.rox.emu.processor.mos6502.op;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.mos6502.op.util.OpCodeConverter;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Enum representation of {@link Mos6502} op-codes.  Each represented by an enum name using the convention
 * '{OP-CODE}{@value #TOKEN_SEPARATOR}{ADDRESSING-MODE}{@value #TOKEN_SEPARATOR}{INDEXING-MODE}'.
 *
 * @author Ross Drew
 */
public enum OpCode {
    BRK(0x00),
    ASL_A(0x0A),
    ASL_Z(0x06),
    ASL_ABS(0x0E),
    ASL_Z_IX(0x16),
    ASL_ABS_IX(0x1E),

    LSR_A(0x4A),
    LSR_Z(0x46),
    LSR_Z_IX(0x56),
    LSR_ABS(0x4E),
    LSR_ABS_IX(0x5E),

    ADC_Z(0x65),
    ADC_I(0x69),
    ADC_ABS(0x6D),
    ADC_ABS_IX(0x7D),
    ADC_ABS_IY(0x79),
    ADC_Z_IX(0x75),
    ADC_IND_IX(0x61),
    ADC_IND_IY(0x71),

    LDA_Z(0xA5),
    LDA_I(0xA9),
    LDA_ABS(0xAD),
    LDA_Z_IX(0xB5),
    LDA_ABS_IY(0xB9),
    LDA_IND_IX(0xA1),
    LDA_IND_IY(0xB1),
    LDA_ABS_IX(0xBD),

    CLV(0xB8),

    AND_Z(0x25),
    AND_Z_IX(0x35),
    AND_ABS_IX(0x3D),
    AND_ABS_IY(0x39),
    AND_ABS(0x2D),
    AND_I(0x29),
    AND_IND_IX(0x21),
    AND_IND_IY(0x31),

    ORA_I(0x09),
    ORA_Z(0x05),
    ORA_Z_IX(0x15),
    ORA_ABS(0x0D),
    ORA_ABS_IX(0x1D),
    ORA_ABS_IY(0x19),
    ORA_IND_IX(0x01),
    ORA_IND_IY(0x11),

    EOR_I(0x49),
    EOR_Z(0x45),
    EOR_Z_IX(0x55),
    EOR_ABS(0x4D),
    EOR_ABS_IX(0x5D),
    EOR_ABS_IY(0x59),
    EOR_IND_IX(0x41),
    EOR_IND_IY(0x51),

    SBC_I(0xE9),
    SBC_Z(0xE5),
    SBC_Z_IX(0xF5),
    SBC_ABS(0xED),
    SBC_ABS_IX(0xFD),
    SBC_ABS_IY(0xF9),
    SBC_IND_IX(0xE1),
    SBC_IND_IY(0xF1),

    CLC(0x18),
    SEC(0x38),

    LDY_I(0xA0),
    LDY_Z(0xA4),
    LDY_Z_IX(0xB4),
    LDY_ABS(0xAC),
    LDY_ABS_IX(0xBC),

    LDX_I(0xA2),
    LDX_ABS(0xAE),
    LDX_ABS_IY(0xBE),
    LDX_Z(0xA6),
    LDX_Z_IY(0xB6),

    STY_Z(0x84),
    STY_ABS(0x8C),
    STY_Z_IX(0x94),

    STA_Z(0x85),
    STA_ABS(0x8D),
    STA_Z_IX(0x95),
    STA_ABS_IX(0x9D),
    STA_ABS_IY(0x99),
    STA_IND_IX(0x81),
    STA_IND_IY(0x91),

    STX_Z(0x86),
    STX_Z_IY(0x96),
    STX_ABS(0x8E),

    INY(0xC8),
    INX(0xE8),
    DEX(0xCA),

    INC_Z(0xE6),
    INC_Z_IX(0xF6),
    INC_ABS(0xEE),
    INC_ABS_IX(0xFE),

    DEC_Z(0xC6),
    DEC_Z_IX(0xD6),
    DEC_ABS(0xCE),
    DEC_ABS_IX(0xDE),
    DEY(0x88),

    PHA(0x48),
    PLA(0x68),
    PHP(0x08),
    PLP(0x28),

    NOP(0xEA),

    JMP_ABS(0x4C),
    JMP_IND(0x6C),

    TAX(0xAA),
    TAY(0xA8),
    TYA(0x98),
    TXA(0x8A),
    TXS(0x9A),
    TSX(0xBA),

    BIT_Z(0x24),
    BIT_ABS(0x2C),

    CMP_I(0xC9),
    CMP_Z(0xC5),
    CMP_Z_IX(0xD5),
    CMP_ABS(0xCD),
    CMP_ABS_IX(0xDD),
    CMP_ABS_IY(0xD9),
    CMP_IND_IX(0xC1),
    CMP_IND_IY(0xD1),

    CPX_I(0xE0),
    CPX_Z(0xE4),
    CPX_ABS(0xEC),

    CPY_I(0xC0),
    CPY_Z(0xC4),
    CPY_ABS(0xCC),

    JSR(0x20),
    BPL(0x10),
    BMI(0x30),
    BVC(0x50),
    BVS(0x70),
    BCC(0x90),
    BCS(0xB0),
    BNE(0xD0),
    BEQ(0xF0),

    ROL_A(0x2A),
    ROL_Z(0x26),
    ROL_Z_IX(0x36),
    ROL_ABS(0x2E),
    ROL_ABS_IX(0x3E),

    ROR_A(0x6A),

    CLI(0x58),
    SEI(0x78),
    SED(0xF8),
    CLD(0xD8),

    RTS(0x60),
    RTI(0x40);

    /**
     * The actual addressing-mode independent operation performed by an {@link OpCode}
     */
    public enum Operation {
        BRK, ASL, LSR, ADC, LDA, CLV, AND, ORA, EOR, SBC,
        CLC, SEC, LDY, LDX, STY, STA, STX, INY, INX, DEX,
        INC, DEC, DEY, PHA, PLA, PHP, PLP, NOP, JMP, TAX,
        TAY, TYA, TXA, TXS, TSX, BIT, CMP, CPX, CPY, JSR,
        BPL, BMI, BVC, BVS, BCC, BCS, BNE, BEQ, ROL, ROR,
        CLI, SEI, SED, CLD, RTS, RTI
    }

    /**
     * The separator used to delimit different elements in the {@link String} enum id
     */
    public static final String TOKEN_SEPARATOR = "_";
    /**
     * The index of the op-code name in the {@link String} enum id,
     * using the token delimiter {@value TOKEN_SEPARATOR}
     */
    public static final int CODE_I = 0;
    /**
     * The index of the addressing mode token in the {@link String} enum id,
     * using the token delimiter {@value TOKEN_SEPARATOR}
     */
    public static final int ADDR_I = CODE_I + 1;
    /**
     * The index of the indexing mode token in the {@link String} enum id,
     * using the token delimiter {@value TOKEN_SEPARATOR}
     */
    public static final int INDX_I = ADDR_I + 1;

    private final Operation operation;
    private final int byteValue;
    private final String opCodeName;
    private final AddressingMode addressingMode;

    OpCode(int byteValue){
        this.byteValue = byteValue;
        this.addressingMode = OpCodeConverter.getAddressingMode(this.name());
        this.opCodeName = OpCodeConverter.getOpCode(this.name());
        this.operation = OpCodeConverter.getOperation(this.name());
    }

    /**
     * Get the {@link OpCode} for
     *
     * @param byteValue this byte value
     * @return the OpCode associated with this byte value
     */
    public static OpCode from(int byteValue){
        return from(opcode -> opcode.getByteValue() == byteValue, byteValue);
    }

    /**
     * Get the {@link OpCode} for
     *
     * @param opCodeName Three character {@link String} representing an {@link AddressingMode#IMPLIED} addressed OpCode
     * @return The OpCode instance associated with this name in {@link AddressingMode#IMPLIED}
     */
    public static OpCode from(String opCodeName){
        return from(opcode -> opcode.getOpCodeName().equalsIgnoreCase(opCodeName), opCodeName);
    }

    /**
     * Get the {@link OpCode} for
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

    public Operation getOperation(){
        return this.operation;
    }

    /**
     * @return the 6502 byte value for this {@link OpCode}
     */
    public int getByteValue(){
        return byteValue;
    }

    /**
     * @return the human readable {@link String} representing this {@link OpCode}
     */
    public String getOpCodeName() {return opCodeName;}

    /**
     * @return the {@link AddressingMode} that this {@link OpCode} uses
     */
    public AddressingMode getAddressingMode(){
        return this.addressingMode;
    }

    /**
     * @param addressingMode from which to get possible {@link OpCode}s
     * @return a {@link Stream} of all {@link OpCode}s that use the the specified {@link AddressingMode}
     */
    public static Stream<OpCode> streamOf(AddressingMode addressingMode){
        return streamOf(opcode -> opcode.getAddressingMode() == addressingMode);
    }

    private static Stream<OpCode> streamOf(Predicate<? super OpCode> predicate){
        return Stream.of(OpCode.values()).filter(predicate);
    }

    /**
     * @return The textual description of this {@link OpCode} including the {@link AddressingMode} it uses
     */
    @Override
    public String toString(){
        return opCodeName + " (" + addressingMode + ")[0x" + Integer.toHexString(byteValue) + "]";
    }
}
