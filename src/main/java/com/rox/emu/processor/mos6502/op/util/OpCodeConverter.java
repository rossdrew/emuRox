package com.rox.emu.processor.mos6502.op.util;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.mos6502.op.AddressingMode;
import com.rox.emu.processor.mos6502.op.OpCode;


/**
 * Utility for converting internal {@link Mos6502} {@link OpCode} representation names to human readable descriptions
 *
 * e.g.  <code>ADC_I</code> &rarr; "<em>ADC (Immediate)</em>"
 *
 * @author Ross Drew
 */
public class OpCodeConverter {
    /**
     * Extract a command line op-code of the {@link Mos6502} instruction set from the {@link String} representation of an {@link OpCode}.
     *
     * @param internalOpCodeName the {@link String} representation of an {@link OpCode}
     * @return A {@link String} instruction from the {@link Mos6502} instruction set, associated with the intended {@link OpCode}
     */
    public static String getOpCode(String internalOpCodeName){
        final String tokens[] = internalOpCodeName.split(OpCode.TOKEN_SEPARATOR);
        return tokens[OpCode.CODE_I];
    }

    /**
     * Extract the {@link Mos6502} {@link AddressingMode} from the {@link String} representation of an {@link OpCode}.
     *
     * @param internalOpCodeName the {@link String} representation of an {@link OpCode}
     * @return An {@link AddressingMode} object that represents the intended addressing mode of the {@link OpCode} in question
     */
    public static AddressingMode getAddressingMode(String internalOpCodeName){
        final String tokens[] = internalOpCodeName.split(OpCode.TOKEN_SEPARATOR);
        if (tokens.length <= OpCode.ADDR_I)
            return AddressingMode.IMPLIED;

        final String addressingModeDescriptor = tokens[OpCode.ADDR_I];

        //XXX Not pretty but necessary for proper test coverage - update if JaCoCo ever learns to deal with it
        final String indexToken = (tokens.length <= OpCode.INDX_I) ? "" : tokens[OpCode.INDX_I];
        if ("I".equals(addressingModeDescriptor))
            return AddressingMode.IMMEDIATE;
        else if ("A".equals(addressingModeDescriptor))
            return AddressingMode.ACCUMULATOR;
        else if ("Z".equals(addressingModeDescriptor))
            return withIndexing(AddressingMode.ZERO_PAGE, indexToken);
        else if ("ABS".equals(addressingModeDescriptor))
            return withIndexing(AddressingMode.ABSOLUTE, indexToken);
        else if ("IND".equals(addressingModeDescriptor))
            return withIndexing(AddressingMode.INDIRECT, indexToken);
        else
            throw new UnknownOpCodeException("Unrecognised addressing mode " + addressingModeDescriptor, internalOpCodeName);
    }

    private static AddressingMode withIndexing(final AddressingMode addressingMode, final String indexToken){
        if ("IX".equalsIgnoreCase(indexToken))
            return addressingMode.xIndexed();
        else if ("IY".equalsIgnoreCase(indexToken))
            return addressingMode.yIndexed();
        else
            return addressingMode;
    }
}
