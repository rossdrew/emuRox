package com.rox.emu.processor.mos6502.op;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.env.RoxByte;
import com.rox.emu.mem.Memory;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.mos6502.Mos6502Alu;
import com.rox.emu.processor.mos6502.Registers;
import com.rox.emu.processor.mos6502.op.util.OpCodeConverter;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.rox.emu.processor.mos6502.op.AddressingMode.*;
import static com.rox.emu.processor.mos6502.op.OpCode.Operation.*;

/**
 * Enum representation of {@link Mos6502} op-codes.  Each represented by an enum name using the convention
 * '{OP-CODE}{@value #TOKEN_SEPARATOR}{ADDRESSING-MODE}{@value #TOKEN_SEPARATOR}{INDEXING-MODE}'.<br/>
 * <br/>
 * {@link Mos6502} op-codes are therefore made of two parts.  The {@link AddressingMode} and the {@link Operation}
 *
 * @author Ross Drew
 */
public enum OpCode implements Instruction {
    BRK(0x00, (a, r, m) -> {}),

    ASL_A(0x0A, (a, r, m) -> {
        ACCUMULATOR.address(r, m, a, ASL::perform);
    }),

    ASL_Z(0x06, (a, r, m) -> {
        ZERO_PAGE.address(r, m, a, ASL::perform);
    }),

    ASL_ABS(0x0E, (a, r, m) -> {
        ABSOLUTE.address(r, m, a, ASL::perform);
    }),

    ASL_Z_IX(0x16, (a, r, m) -> {}),

    ASL_ABS_IX(0x1E, (a, r, m) -> {}),

    LSR_A(0x4A, (a, r, m) -> {}),
    LSR_Z(0x46, (a, r, m) -> {}),
    LSR_Z_IX(0x56, (a, r, m) -> {}),
    LSR_ABS(0x4E, (a, r, m) -> {}),
    LSR_ABS_IX(0x5E, (a, r, m) -> {}),

    ADC_Z(0x65, (a, r, m) -> {}),
    ADC_I(0x69, (a, r, m) -> {}),
    ADC_ABS(0x6D, (a, r, m) -> {}),
    ADC_ABS_IX(0x7D, (a, r, m) -> {}),
    ADC_ABS_IY(0x79, (a, r, m) -> {}),
    ADC_Z_IX(0x75, (a, r, m) -> {}),
    ADC_IND_IX(0x61, (a, r, m) -> {}),
    ADC_IND_IY(0x71, (a, r, m) -> {}),

    LDA_Z(0xA5, (a, r, m) -> {}),
    LDA_I(0xA9, (a, r, m) -> {}),
    LDA_ABS(0xAD, (a, r, m) -> {}),
    LDA_Z_IX(0xB5, (a, r, m) -> {}),
    LDA_ABS_IY(0xB9, (a, r, m) -> {}),
    LDA_IND_IX(0xA1, (a, r, m) -> {}),
    LDA_IND_IY(0xB1, (a, r, m) -> {}),
    LDA_ABS_IX(0xBD, (a, r, m) -> {}),

    CLV(0xB8, (a, r, m) -> {}),

    AND_Z(0x25, (a, r, m) -> {}),
    AND_Z_IX(0x35, (a, r, m) -> {}),
    AND_ABS_IX(0x3D, (a, r, m) -> {}),
    AND_ABS_IY(0x39, (a, r, m) -> {}),
    AND_ABS(0x2D, (a, r, m) -> {}),
    AND_I(0x29, (a, r, m) -> {}),
    AND_IND_IX(0x21, (a, r, m) -> {}),
    AND_IND_IY(0x31, (a, r, m) -> {}),

    ORA_I(0x09, (a, r, m) -> {}),
    ORA_Z(0x05, (a, r, m) -> {}),
    ORA_Z_IX(0x15, (a, r, m) -> {}),
    ORA_ABS(0x0D, (a, r, m) -> {}),
    ORA_ABS_IX(0x1D, (a, r, m) -> {}),
    ORA_ABS_IY(0x19, (a, r, m) -> {}),
    ORA_IND_IX(0x01, (a, r, m) -> {}),
    ORA_IND_IY(0x11, (a, r, m) -> {}),

    EOR_I(0x49, (a, r, m) -> {}),
    EOR_Z(0x45, (a, r, m) -> {}),
    EOR_Z_IX(0x55, (a, r, m) -> {}),
    EOR_ABS(0x4D, (a, r, m) -> {}),
    EOR_ABS_IX(0x5D, (a, r, m) -> {}),
    EOR_ABS_IY(0x59, (a, r, m) -> {}),
    EOR_IND_IX(0x41, (a, r, m) -> {}),
    EOR_IND_IY(0x51, (a, r, m) -> {}),

    SBC_I(0xE9, (a, r, m) -> {}),
    SBC_Z(0xE5, (a, r, m) -> {}),
    SBC_Z_IX(0xF5, (a, r, m) -> {}),
    SBC_ABS(0xED, (a, r, m) -> {}),
    SBC_ABS_IX(0xFD, (a, r, m) -> {}),
    SBC_ABS_IY(0xF9, (a, r, m) -> {}),
    SBC_IND_IX(0xE1, (a, r, m) -> {}),
    SBC_IND_IY(0xF1, (a, r, m) -> {}),

    CLC(0x18, (a, r, m) -> {}),
    SEC(0x38, (a, r, m) -> {}),

    LDY_I(0xA0, (a, r, m) -> {}),
    LDY_Z(0xA4, (a, r, m) -> {}),
    LDY_Z_IX(0xB4, (a, r, m) -> {}),
    LDY_ABS(0xAC, (a, r, m) -> {}),
    LDY_ABS_IX(0xBC, (a, r, m) -> {}),

    LDX_I(0xA2, (a, r, m) -> {}),
    LDX_ABS(0xAE, (a, r, m) -> {}),
    LDX_ABS_IY(0xBE, (a, r, m) -> {}),
    LDX_Z(0xA6, (a, r, m) -> {}),
    LDX_Z_IY(0xB6, (a, r, m) -> {}),

    STY_Z(0x84, (a, r, m) -> {}),
    STY_ABS(0x8C, (a, r, m) -> {}),
    STY_Z_IX(0x94, (a, r, m) -> {}),

    STA_Z(0x85, (a, r, m) -> {}),
    STA_ABS(0x8D, (a, r, m) -> {}),
    STA_Z_IX(0x95, (a, r, m) -> {}),
    STA_ABS_IX(0x9D, (a, r, m) -> {}),
    STA_ABS_IY(0x99, (a, r, m) -> {}),
    STA_IND_IX(0x81, (a, r, m) -> {}),
    STA_IND_IY(0x91, (a, r, m) -> {}),

    STX_Z(0x86, (a, r, m) -> {}),
    STX_Z_IY(0x96, (a, r, m) -> {}),
    STX_ABS(0x8E, (a, r, m) -> {}),

    INY(0xC8, (a, r, m) -> {}),
    INX(0xE8, (a, r, m) -> {}),
    DEX(0xCA, (a, r, m) -> {}),

    INC_Z(0xE6, (a, r, m) -> {}),
    INC_Z_IX(0xF6, (a, r, m) -> {}),
    INC_ABS(0xEE, (a, r, m) -> {}),
    INC_ABS_IX(0xFE, (a, r, m) -> {}),

    DEC_Z(0xC6, (a, r, m) -> {}),
    DEC_Z_IX(0xD6, (a, r, m) -> {}),
    DEC_ABS(0xCE, (a, r, m) -> {}),
    DEC_ABS_IX(0xDE, (a, r, m) -> {}),
    DEY(0x88, (a, r, m) -> {}),

    PHA(0x48, (a, r, m) -> {}),
    PLA(0x68, (a, r, m) -> {}),
    PHP(0x08, (a, r, m) -> {}),
    PLP(0x28, (a, r, m) -> {}),

    NOP(0xEA, (a, r, m) -> {}),

    JMP_ABS(0x4C, (a, r, m) -> {}),
    JMP_IND(0x6C, (a, r, m) -> {}),

    TAX(0xAA, (a, r, m) -> {}),
    TAY(0xA8, (a, r, m) -> {}),
    TYA(0x98, (a, r, m) -> {}),
    TXA(0x8A, (a, r, m) -> {}),
    TXS(0x9A, (a, r, m) -> {}),
    TSX(0xBA, (a, r, m) -> {}),

    BIT_Z(0x24, (a, r, m) -> {}),
    BIT_ABS(0x2C, (a, r, m) -> {}),

    CMP_I(0xC9, (a, r, m) -> {}),
    CMP_Z(0xC5, (a, r, m) -> {}),
    CMP_Z_IX(0xD5, (a, r, m) -> {}),
    CMP_ABS(0xCD, (a, r, m) -> {}),
    CMP_ABS_IX(0xDD, (a, r, m) -> {}),
    CMP_ABS_IY(0xD9, (a, r, m) -> {}),
    CMP_IND_IX(0xC1, (a, r, m) -> {}),
    CMP_IND_IY(0xD1, (a, r, m) -> {}),

    CPX_I(0xE0, (a, r, m) -> {}),
    CPX_Z(0xE4, (a, r, m) -> {}),
    CPX_ABS(0xEC, (a, r, m) -> {}),

    CPY_I(0xC0, (a, r, m) -> {}),
    CPY_Z(0xC4, (a, r, m) -> {}),
    CPY_ABS(0xCC, (a, r, m) -> {}),

    JSR(0x20, (a, r, m) -> {}),
    BPL(0x10, (a, r, m) -> {}),
    BMI(0x30, (a, r, m) -> {}),
    BVC(0x50, (a, r, m) -> {}),
    BVS(0x70, (a, r, m) -> {}),
    BCC(0x90, (a, r, m) -> {}),
    BCS(0xB0, (a, r, m) -> {}),
    BNE(0xD0, (a, r, m) -> {}),
    BEQ(0xF0, (a, r, m) -> {}),

    ROL_A(0x2A, (a, r, m) -> {}),
    ROL_Z(0x26, (a, r, m) -> {}),
    ROL_Z_IX(0x36, (a, r, m) -> {}),
    ROL_ABS(0x2E, (a, r, m) -> {}),
    ROL_ABS_IX(0x3E, (a, r, m) -> {}),

    /** Not implemented and/or not published on older 6502s */
    ROR_A(0x6A, (a, r, m) -> {}),

    CLI(0x58, (a, r, m) -> {}),
    SEI(0x78, (a, r, m) -> {}),
    SED(0xF8, (a, r, m) -> {}),
    CLD(0xD8, (a, r, m) -> {}),

    RTS(0x60, (a, r, m) -> {}),
    RTI(0x40, (a, r, m) -> {});

    @Override
    public void perform(Mos6502Alu alu, Registers registers, Memory memory) {
        instruction.perform(alu, registers, memory);
    }

    /**
     * The actual addressing-mode independent operation performed by an {@link OpCode}
     */
    public enum Operation implements AddressedValueInstruction{
        BRK((r,m,a,i)->i),

        ASL((a, r, m, v) -> {
            final RoxByte newValue = a.asl(v);
            r.setFlagsBasedOn(newValue);
            return newValue;
        }),

        LSR((r,m,a,i)->i),
        ADC((r,m,a,i)->i),
        LDA((r,m,a,i)->i),
        CLV((r,m,a,i)->i),
        AND((r,m,a,i)->i),
        ORA((r,m,a,i)->i),
        EOR((r,m,a,i)->i),
        SBC((r,m,a,i)->i),
       
        CLC((r,m,a,i)->i),
        SEC((r,m,a,i)->i),
        LDY((r,m,a,i)->i),
        LDX((r,m,a,i)->i),
        STY((r,m,a,i)->i),
        STA((r,m,a,i)->i),
        STX((r,m,a,i)->i),
        INY((r,m,a,i)->i),
        INX((r,m,a,i)->i),
        DEX((r,m,a,i)->i),
       
        INC((r,m,a,i)->i),
        DEC((r,m,a,i)->i),
        DEY((r,m,a,i)->i),
        PHA((r,m,a,i)->i),
        PLA((r,m,a,i)->i), 
        PHP((r,m,a,i)->i),
        PLP((r,m,a,i)->i),
        NOP((r,m,a,i)->i),
        JMP((r,m,a,i)->i),
        TAX((r,m,a,i)->i),
       
        TAY((r,m,a,i)->i),
        TYA((r,m,a,i)->i),
        TXA((r,m,a,i)->i),
        TXS((r,m,a,i)->i),
        TSX((r,m,a,i)->i),
        BIT((r,m,a,i)->i),
        CMP((r,m,a,i)->i),
        CPX((r,m,a,i)->i),
        CPY((r,m,a,i)->i),
        JSR((r,m,a,i)->i),
       
        BPL((r,m,a,i)->i),
        BMI((r,m,a,i)->i),
        BVC((r,m,a,i)->i),
        BVS((r,m,a,i)->i),
        BCC((r,m,a,i)->i),
        BCS((r,m,a,i)->i),
        BNE((r,m,a,i)->i),
        BEQ((r,m,a,i)->i),
        ROL((r,m,a,i)->i),
        ROR((r,m,a,i)->i),
       
        CLI((r,m,a,i)->i),
        SEI((r,m,a,i)->i),
        SED((r,m,a,i)->i),
        CLD((r,m,a,i)->i),
        RTS((r,m,a,i)->i),
        RTI((r,m,a,i)->i);
        
        private AddressedValueInstruction instruction;
        
        Operation(AddressedValueInstruction instruction){
            this.instruction = instruction;
        }

        @Override
        public RoxByte perform(Mos6502Alu alu, Registers registers, Memory memory, RoxByte value) {
            return instruction.perform(alu, registers, memory, value);
        }
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
