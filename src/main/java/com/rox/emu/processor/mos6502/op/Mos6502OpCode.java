package com.rox.emu.processor.mos6502.op;

import com.rox.emu.UnknownOpCodeException;
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
 * '{OP-CODE}{@literal _}{ADDRESSING-MODE}{@literal _}{INDEXING-MODE}'.<br/>
 * <br/>
 * {@link Mos6502} op-codes are therefore made of two parts.  The {@link Mos6502AddressingMode} and the {@link Mos6502Operation}
 *
 * @author Ross Drew
 */
public enum Mos6502OpCode implements Mos6502Instruction {
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

    /** Not implemented and/or not published on older 6502s */
    ROR_A(0x6A),

    CLI(0x58),
    SEI(0x78),
    SED(0xF8),
    CLD(0xD8),

    RTS(0x60),
    RTI(0x40);

    @Override
    public void perform(Mos6502Alu alu, Registers registers, Memory memory) {
        addressingMode.address(registers, memory, alu, operation);
    }

    private final Mos6502Operation operation;
    private final int byteValue;
    private final String opCodeName;
    private final Mos6502AddressingMode addressingMode;

    Mos6502OpCode(int byteValue){
        this.byteValue = byteValue;
        //XXX Should I keep doing this or just pass them in explicitly?
        this.addressingMode = OpCodeConverter.getAddressingMode(this.name());
        this.opCodeName = OpCodeConverter.getOpCode(this.name());
        this.operation = OpCodeConverter.getOperation(this.name());

    }

    /**
     * Get the {@link Mos6502OpCode} for
     *
     * @param byteValue this byte value
     * @return the OpCode associated with this byte value
     */
    public static Mos6502OpCode from(int byteValue){
        return from(opcode -> opcode.getByteValue() == byteValue, byteValue);
    }

    /**
     * Get the {@link Mos6502OpCode} for
     *
     * @param opCodeName Three character {@link String} representing an {@link Mos6502AddressingMode#IMPLIED} addressed OpCode
     * @return The OpCode instance associated with this name in {@link Mos6502AddressingMode#IMPLIED}
     */
    public static Mos6502OpCode from(String opCodeName){
        return from(opcode -> opcode.getOpCodeName().equalsIgnoreCase(opCodeName), opCodeName);
    }

    /**
     * Get the {@link Mos6502OpCode} for
     *
     * @param opCodeName Three character {@link String} representing OpCode name
     * @param addressingMode The {@link Mos6502AddressingMode} of the OpCode
     * @return The OpCode instance associated with this name in this {@link Mos6502AddressingMode}
     */
    public static Mos6502OpCode from(String opCodeName, Mos6502AddressingMode addressingMode){
        return matching(opcode -> opcode.getOpCodeName().equalsIgnoreCase(opCodeName) &&
                        opcode.getAddressingMode() == addressingMode,
                opCodeName + " in " + addressingMode,
                opCodeName);
    }

    /**
     * @param predicate A predicate used to search {@link Mos6502OpCode}s
     * @param predicateTerm The main term of {@link Object} used in the predicate
     * @return The first {@link Mos6502OpCode} found
     * @throws UnknownOpCodeException if no {@link Mos6502OpCode} matches the given predicate
     */
    private static Mos6502OpCode from(Predicate<? super Mos6502OpCode> predicate, Object predicateTerm) {
        return matching(predicate, ""+predicateTerm, predicateTerm);
    }

    /**
     * @param predicate A predicate used to search {@link Mos6502OpCode}s
     * @param predicateDescription A {@link String} description of the search
     * @param predicateTerm The main term of {@link Object} used in the predicate
     * @return The first {@link Mos6502OpCode} found
     * @throws UnknownOpCodeException if no {@link Mos6502OpCode} matches the given predicate
     */
    private static Mos6502OpCode matching(Predicate<? super Mos6502OpCode> predicate, String predicateDescription, Object predicateTerm) {
        Optional<Mos6502OpCode> result = Arrays.stream(Mos6502OpCode.values()).filter(predicate).findFirst();

        return result.orElseThrow(() -> new UnknownOpCodeException("Unknown opcode name while creating OpCode object: " + predicateDescription, predicateTerm));
    }

    public Mos6502Operation getOperation(){
        return this.operation;
    }

    /**
     * @return the 6502 byte value for this {@link Mos6502OpCode}
     */
    public int getByteValue(){
        return byteValue;
    }

    /**
     * @return the human readable {@link String} representing this {@link Mos6502OpCode}
     */
    public String getOpCodeName() {return opCodeName;}

    /**
     * @return the {@link Mos6502AddressingMode} that this {@link Mos6502OpCode} uses
     */
    public Mos6502AddressingMode getAddressingMode(){
        return this.addressingMode;
    }

    /**
     * @param addressingMode from which to get possible {@link Mos6502OpCode}s
     * @return a {@link Stream} of all {@link Mos6502OpCode}s that use the the specified {@link Mos6502AddressingMode}
     */
    public static Stream<Mos6502OpCode> streamOf(Mos6502AddressingMode addressingMode){
        return streamOf(opcode -> opcode.getAddressingMode() == addressingMode);
    }

    private static Stream<Mos6502OpCode> streamOf(Predicate<? super Mos6502OpCode> predicate){
        return Stream.of(Mos6502OpCode.values()).filter(predicate);
    }

    /**
     * @return The textual description of this {@link Mos6502OpCode} including the {@link Mos6502AddressingMode} it uses
     */
    @Override
    public String toString(){
        return opCodeName + " (" + addressingMode + ")[0x" + Integer.toHexString(byteValue) + "]";
    }
}
