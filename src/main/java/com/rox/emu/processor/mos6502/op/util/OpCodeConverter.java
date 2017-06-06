package com.rox.emu.processor.mos6502.op.util;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.processor.mos6502.op.AddressingMode;
import com.rox.emu.processor.mos6502.op.OpCode;


/**
 * Utility for converting internal opcode representation names to human readable descriptions
 *
 * e.g.  ADC_I  ->  "ADC (Immediate)"
 *
 * @author Ross Drew
 */
public class OpCodeConverter {
    public static String getOpCode(String internalOpCodeName){
        final String tokens[] = internalOpCodeName.split(OpCode.TOKEN_SEPARATOR);
        return tokens[OpCode.CODE_I];
    }

    public static AddressingMode getAddressingMode(String internalOpCodeName){
        final String tokens[] = internalOpCodeName.split(OpCode.TOKEN_SEPARATOR);
        if (tokens.length <= OpCode.ADDR_I)
            return AddressingMode.IMPLIED;

        final String addressingModeDescriptor = tokens[OpCode.ADDR_I];

        final String indexToken = (tokens.length <= OpCode.INDX_I) ? "" : tokens[OpCode.INDX_I];
        switch (addressingModeDescriptor){
            case "I": return AddressingMode.IMMEDIATE;
            case "A": return AddressingMode.ACCUMULATOR;
            case "Z":
                return withIndexing(AddressingMode.ZERO_PAGE, indexToken);
            case "ABS":
                return withIndexing(AddressingMode.ABSOLUTE, indexToken);
            case "IND":
                return withIndexing(AddressingMode.INDIRECT, indexToken);
            default:
                throw new UnknownOpCodeException("Unrecognised addressing mode " + addressingModeDescriptor, internalOpCodeName);
        }
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
