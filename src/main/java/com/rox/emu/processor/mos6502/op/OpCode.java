package com.rox.emu.processor.mos6502.op;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;
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
public enum OpCode implements Instruction {
    BRK(0x00, (r, m, a) -> {}),

    ASL_A(0x0A, (r, m, a) -> {
        final RoxByte value = r.getRegister(Registers.Register.ACCUMULATOR);
        final RoxByte newValue = a.asl(value);

        r.setRegister(Registers.Register.ACCUMULATOR, newValue);
        r.setFlagsBasedOn(newValue);
    }),

    ASL_Z(0x06, (r, m, a) -> {
        final RoxWord valAddr = RoxWord.from(m.getByte(r.getPC()));
        r.setPC(RoxWord.fromLiteral(valAddr.getRawValue()+1));

        final RoxByte value = m.getByte(valAddr);
        final RoxByte newValue = a.asl(value);

        final int rawValue = newValue.getRawValue();

        m.setByteAt(valAddr, newValue);
    }),

    ASL_ABS(0x0E, (r, m, a) -> {}),
    ASL_Z_IX(0x16, (r, m, a) -> {}),
    ASL_ABS_IX(0x1E, (r, m, a) -> {}),

    LSR_A(0x4A, (r, m, a) -> {}),
    LSR_Z(0x46, (r, m, a) -> {}),
    LSR_Z_IX(0x56, (r, m, a) -> {}),
    LSR_ABS(0x4E, (r, m, a) -> {}),
    LSR_ABS_IX(0x5E, (r, m, a) -> {}),

    ADC_Z(0x65, (r, m, a) -> {}),
    ADC_I(0x69, (r, m, a) -> {}),
    ADC_ABS(0x6D, (r, m, a) -> {}),
    ADC_ABS_IX(0x7D, (r, m, a) -> {}),
    ADC_ABS_IY(0x79, (r, m, a) -> {}),
    ADC_Z_IX(0x75, (r, m, a) -> {}),
    ADC_IND_IX(0x61, (r, m, a) -> {}),
    ADC_IND_IY(0x71, (r, m, a) -> {}),

    LDA_Z(0xA5, (r, m, a) -> {}),
    LDA_I(0xA9, (r, m, a) -> {}),
    LDA_ABS(0xAD, (r, m, a) -> {}),
    LDA_Z_IX(0xB5, (r, m, a) -> {}),
    LDA_ABS_IY(0xB9, (r, m, a) -> {}),
    LDA_IND_IX(0xA1, (r, m, a) -> {}),
    LDA_IND_IY(0xB1, (r, m, a) -> {}),
    LDA_ABS_IX(0xBD, (r, m, a) -> {}),

    CLV(0xB8, (r, m, a) -> {}),

    AND_Z(0x25, (r, m, a) -> {}),
    AND_Z_IX(0x35, (r, m, a) -> {}),
    AND_ABS_IX(0x3D, (r, m, a) -> {}),
    AND_ABS_IY(0x39, (r, m, a) -> {}),
    AND_ABS(0x2D, (r, m, a) -> {}),
    AND_I(0x29, (r, m, a) -> {}),
    AND_IND_IX(0x21, (r, m, a) -> {}),
    AND_IND_IY(0x31, (r, m, a) -> {}),

    ORA_I(0x09, (r, m, a) -> {}),
    ORA_Z(0x05, (r, m, a) -> {}),
    ORA_Z_IX(0x15, (r, m, a) -> {}),
    ORA_ABS(0x0D, (r, m, a) -> {}),
    ORA_ABS_IX(0x1D, (r, m, a) -> {}),
    ORA_ABS_IY(0x19, (r, m, a) -> {}),
    ORA_IND_IX(0x01, (r, m, a) -> {}),
    ORA_IND_IY(0x11, (r, m, a) -> {}),

    EOR_I(0x49, (r, m, a) -> {}),
    EOR_Z(0x45, (r, m, a) -> {}),
    EOR_Z_IX(0x55, (r, m, a) -> {}),
    EOR_ABS(0x4D, (r, m, a) -> {}),
    EOR_ABS_IX(0x5D, (r, m, a) -> {}),
    EOR_ABS_IY(0x59, (r, m, a) -> {}),
    EOR_IND_IX(0x41, (r, m, a) -> {}),
    EOR_IND_IY(0x51, (r, m, a) -> {}),

    SBC_I(0xE9, (r, m, a) -> {}),
    SBC_Z(0xE5, (r, m, a) -> {}),
    SBC_Z_IX(0xF5, (r, m, a) -> {}),
    SBC_ABS(0xED, (r, m, a) -> {}),
    SBC_ABS_IX(0xFD, (r, m, a) -> {}),
    SBC_ABS_IY(0xF9, (r, m, a) -> {}),
    SBC_IND_IX(0xE1, (r, m, a) -> {}),
    SBC_IND_IY(0xF1, (r, m, a) -> {}),

    CLC(0x18, (r, m, a) -> {}),
    SEC(0x38, (r, m, a) -> {}),

    LDY_I(0xA0, (r, m, a) -> {}),
    LDY_Z(0xA4, (r, m, a) -> {}),
    LDY_Z_IX(0xB4, (r, m, a) -> {}),
    LDY_ABS(0xAC, (r, m, a) -> {}),
    LDY_ABS_IX(0xBC, (r, m, a) -> {}),

    LDX_I(0xA2, (r, m, a) -> {}),
    LDX_ABS(0xAE, (r, m, a) -> {}),
    LDX_ABS_IY(0xBE, (r, m, a) -> {}),
    LDX_Z(0xA6, (r, m, a) -> {}),
    LDX_Z_IY(0xB6, (r, m, a) -> {}),

    STY_Z(0x84, (r, m, a) -> {}),
    STY_ABS(0x8C, (r, m, a) -> {}),
    STY_Z_IX(0x94, (r, m, a) -> {}),

    STA_Z(0x85, (r, m, a) -> {}),
    STA_ABS(0x8D, (r, m, a) -> {}),
    STA_Z_IX(0x95, (r, m, a) -> {}),
    STA_ABS_IX(0x9D, (r, m, a) -> {}),
    STA_ABS_IY(0x99, (r, m, a) -> {}),
    STA_IND_IX(0x81, (r, m, a) -> {}),
    STA_IND_IY(0x91, (r, m, a) -> {}),

    STX_Z(0x86, (r, m, a) -> {}),
    STX_Z_IY(0x96, (r, m, a) -> {}),
    STX_ABS(0x8E, (r, m, a) -> {}),

    INY(0xC8, (r, m, a) -> {}),
    INX(0xE8, (r, m, a) -> {}),
    DEX(0xCA, (r, m, a) -> {}),

    INC_Z(0xE6, (r, m, a) -> {}),
    INC_Z_IX(0xF6, (r, m, a) -> {}),
    INC_ABS(0xEE, (r, m, a) -> {}),
    INC_ABS_IX(0xFE, (r, m, a) -> {}),

    DEC_Z(0xC6, (r, m, a) -> {}),
    DEC_Z_IX(0xD6, (r, m, a) -> {}),
    DEC_ABS(0xCE, (r, m, a) -> {}),
    DEC_ABS_IX(0xDE, (r, m, a) -> {}),
    DEY(0x88, (r, m, a) -> {}),

    PHA(0x48, (r, m, a) -> {}),
    PLA(0x68, (r, m, a) -> {}),
    PHP(0x08, (r, m, a) -> {}),
    PLP(0x28, (r, m, a) -> {}),

    NOP(0xEA, (r, m, a) -> {}),

    JMP_ABS(0x4C, (r, m, a) -> {}),
    JMP_IND(0x6C, (r, m, a) -> {}),

    TAX(0xAA, (r, m, a) -> {}),
    TAY(0xA8, (r, m, a) -> {}),
    TYA(0x98, (r, m, a) -> {}),
    TXA(0x8A, (r, m, a) -> {}),
    TXS(0x9A, (r, m, a) -> {}),
    TSX(0xBA, (r, m, a) -> {}),

    BIT_Z(0x24, (r, m, a) -> {}),
    BIT_ABS(0x2C, (r, m, a) -> {}),

    CMP_I(0xC9, (r, m, a) -> {}),
    CMP_Z(0xC5, (r, m, a) -> {}),
    CMP_Z_IX(0xD5, (r, m, a) -> {}),
    CMP_ABS(0xCD, (r, m, a) -> {}),
    CMP_ABS_IX(0xDD, (r, m, a) -> {}),
    CMP_ABS_IY(0xD9, (r, m, a) -> {}),
    CMP_IND_IX(0xC1, (r, m, a) -> {}),
    CMP_IND_IY(0xD1, (r, m, a) -> {}),

    CPX_I(0xE0, (r, m, a) -> {}),
    CPX_Z(0xE4, (r, m, a) -> {}),
    CPX_ABS(0xEC, (r, m, a) -> {}),

    CPY_I(0xC0, (r, m, a) -> {}),
    CPY_Z(0xC4, (r, m, a) -> {}),
    CPY_ABS(0xCC, (r, m, a) -> {}),

    JSR(0x20, (r, m, a) -> {}),
    BPL(0x10, (r, m, a) -> {}),
    BMI(0x30, (r, m, a) -> {}),
    BVC(0x50, (r, m, a) -> {}),
    BVS(0x70, (r, m, a) -> {}),
    BCC(0x90, (r, m, a) -> {}),
    BCS(0xB0, (r, m, a) -> {}),
    BNE(0xD0, (r, m, a) -> {}),
    BEQ(0xF0, (r, m, a) -> {}),

    ROL_A(0x2A, (r, m, a) -> {}),
    ROL_Z(0x26, (r, m, a) -> {}),
    ROL_Z_IX(0x36, (r, m, a) -> {}),
    ROL_ABS(0x2E, (r, m, a) -> {}),
    ROL_ABS_IX(0x3E, (r, m, a) -> {}),

    /** Not implemented and/or not published on older 6502s */
    ROR_A(0x6A, (r, m, a) -> {}),

    CLI(0x58, (r, m, a) -> {}),
    SEI(0x78, (r, m, a) -> {}),
    SED(0xF8, (r, m, a) -> {}),
    CLD(0xD8, (r, m, a) -> {}),

    RTS(0x60, (r, m, a) -> {}),
    RTI(0x40, (r, m, a) -> { });

    @Override
    public void perform(Registers r, Memory m, Mos6502Alu alu) {
        instruction.perform(r,m, alu);
    }

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

    private final Instruction instruction;

    OpCode(int byteValue, Instruction instruction){
        this.byteValue = byteValue;
        this.addressingMode = OpCodeConverter.getAddressingMode(this.name());
        this.opCodeName = OpCodeConverter.getOpCode(this.name());
        this.operation = OpCodeConverter.getOperation(this.name());

        this.instruction = instruction;
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
     * @throws UnknownOpCodeException if no {@link OpCode} matches the given predicate
     */
    private static OpCode from(Predicate<? super OpCode> predicate, Object predicateTerm) {
        return matching(predicate, ""+predicateTerm, predicateTerm);
    }

    /**
     * @param predicate A predicate used to search {@link OpCode}s
     * @param predicateDescription A {@link String} description of the search
     * @param predicateTerm The main term of {@link Object} used in the predicate
     * @return The first {@link OpCode} found
     * @throws UnknownOpCodeException if no {@link OpCode} matches the given predicate
     */
    private static OpCode matching(Predicate<? super OpCode> predicate, String predicateDescription, Object predicateTerm) {
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
