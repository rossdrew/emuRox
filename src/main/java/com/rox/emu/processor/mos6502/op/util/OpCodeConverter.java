package com.rox.emu.processor.mos6502.op.util;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.mos6502.op.Mos6502AddressingMode;
import com.rox.emu.processor.mos6502.op.Mos6502OpCode;
import com.rox.emu.processor.mos6502.op.Mos6502Operation;

import java.util.EnumSet;


/**
 * Utility for converting internal {@link Mos6502} {@link Mos6502OpCode} representation names to human readable descriptions
 *
 * e.g.  <code>ADC_I</code> &rarr; "<em>ADC (Immediate)</em>"
 *
 * @author Ross Drew
 */
public class OpCodeConverter {
    private OpCodeConverter(){/* Used to hide implicitly public constructor for a utility class*/}

    /**
     * Extract a command line op-code of the {@link Mos6502} instruction set from the {@link String} representation of an {@link Mos6502OpCode}.
     *
     * @param internalOpCodeName the {@link String} representation of an {@link Mos6502OpCode}
     * @return A {@link String} instruction from the {@link Mos6502} instruction set, associated with the intended {@link Mos6502OpCode}
     */
    public static String getOpCode(String internalOpCodeName){
        final String[] tokens = internalOpCodeName.split(Mos6502OpCode.TOKEN_SEPARATOR);
        return tokens[Mos6502OpCode.CODE_I];
    }


    /**
     * @param internalOpCodeName {@link String} representation of an {@link Mos6502OpCode}
     * @return the {@link Mos6502Operation} associated with this {@link Mos6502OpCode}
     */
    public static Mos6502Operation getOperation(final String internalOpCodeName) {
        return Mos6502Operation.valueOf(getOpCode(internalOpCodeName));
    }

    /**
     * Extract the {@link Mos6502} {@link Mos6502AddressingMode} from the {@link String} representation of an {@link Mos6502OpCode}.
     *
     * @param internalOpCodeName the {@link String} representation of an {@link Mos6502OpCode}
     * @return An {@link Mos6502AddressingMode} object that represents the intended addressing mode of the {@link Mos6502OpCode} in question
     */
    public static Mos6502AddressingMode getAddressingMode(String internalOpCodeName){
        final String[] tokens = internalOpCodeName.split(Mos6502OpCode.TOKEN_SEPARATOR);
        if (tokens.length <= Mos6502OpCode.ADDR_I) {
            //XXX Write this less ugly
            if (EnumSet.of( Mos6502Operation.JSR,
                            Mos6502Operation.BPL,
                            Mos6502Operation.BMI,
                            Mos6502Operation.BVC,
                            Mos6502Operation.BVS,
                            Mos6502Operation.BCC,
                            Mos6502Operation.BCS,
                            Mos6502Operation.BNE,
                            Mos6502Operation.BEQ).contains(getOperation(internalOpCodeName)))
                return Mos6502AddressingMode.RELATIVE;

            return Mos6502AddressingMode.IMPLIED;
        }

        final String addressingModeDescriptor = tokens[Mos6502OpCode.ADDR_I];

        //XXX Not pretty but necessary for proper test coverage - update if JaCoCo ever learns to deal with it
        final String indexToken = (tokens.length <= Mos6502OpCode.INDX_I) ? "" : tokens[Mos6502OpCode.INDX_I];
        if ("I".equals(addressingModeDescriptor))
            return Mos6502AddressingMode.IMMEDIATE;
        else if ("A".equals(addressingModeDescriptor))
            return Mos6502AddressingMode.ACCUMULATOR;
        else if ("Z".equals(addressingModeDescriptor))
            return withIndexing(Mos6502AddressingMode.ZERO_PAGE, indexToken);
        else if ("ABS".equals(addressingModeDescriptor))
            return withIndexing(Mos6502AddressingMode.ABSOLUTE, indexToken);
        else if ("IND".equals(addressingModeDescriptor))
            return withIndexing(Mos6502AddressingMode.INDIRECT, indexToken);
        else
            throw new UnknownOpCodeException("Unrecognised addressing mode " + addressingModeDescriptor, internalOpCodeName);
    }

    private static Mos6502AddressingMode withIndexing(final Mos6502AddressingMode addressingMode, final String indexToken){
        if ("IX".equalsIgnoreCase(indexToken))
            return addressingMode.xIndexed();
        else if ("IY".equalsIgnoreCase(indexToken))
            return addressingMode.yIndexed();
        else
            return addressingMode;
    }
}
