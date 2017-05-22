package com.rox.emu.processor.mos6502.op.util;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.processor.mos6502.op.AddressingMode;

import java.util.Arrays;

/**
 * Utility for converting internal opcode representation names to human readable descriptions
 *
 * e.g.  OP_ADC_I  ->  "ADC (Immediate)"
 *
 * @author Ross Drew
 */
public class OpCodeConverter {
    //Internal representation details
    private static final String SEPARATOR = "_";
    private static final int OP_DELIMITER = 0;
    private static final int OP_CODE = 1;
    private static final int OP_ADD = 2;
    private static final int OP_I = 3;

    //Addressing modes XXX These can be removed once toDescription() is switched over
    public static final String ADDR_IMP = "Implied";
    public static final String ADDR_I = "Immediate";
    public static final String ADDR_A = "Accumulator";
    public static final String ADDR_Z = "Zero Page";
    public static final String ADDR_ABS = "Absolute";
    public static final String ADDR_IND = "Indirect";

    //Indexing modes
    public static final String INDEX_X = " [X]";
    public static final String INDEX_Y = " [Y]";

    public static String getOpCode(String internalOpCodeName){
        final String tokens[] = internalOpCodeName.split(SEPARATOR);
        return tokens[OP_CODE];
    }

    public static AddressingMode getAddressingMode(String internalOpCodeName){
        final String tokens[] = internalOpCodeName.split(SEPARATOR);
        if (tokens.length < 3)
            return AddressingMode.IMPLIED;

        final String addressingModeDescriptor = tokens[OP_ADD];

        final String indexToken = (tokens.length <= OP_I) ? "" : tokens[OP_I];
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

    public static AddressingMode withIndexing(final AddressingMode addressingMode, final String indexToken){
        switch (indexToken){
            case "IX": return addressingMode.xIndexed();
            case "IY": return addressingMode.yIndexed();
            default: return addressingMode;
        }
    }

    public static String toDescription(String internalOpCodeName){
        if (internalOpCodeName != null && !internalOpCodeName.isEmpty()){
            String tokens[] = internalOpCodeName.split(SEPARATOR);
            if (!tokens[OP_DELIMITER].equalsIgnoreCase("OP"))
                throw new UnknownOpCodeException("Opcode not properly delimited", internalOpCodeName);
            return (getOpCode(tokens) + getAddressingMode(tokens)); //XXX Change to use the new style
        }else{
            throw new UnknownOpCodeException("Empty Opcode", internalOpCodeName);
        }
    }

    private static String getOpCode(String[] t){
        return t[OP_CODE];
    }

    private static String getAddressingMode(String[] t) {
        String addressingModeDescription = " (";

        if (t.length > OP_ADD){
            switch (t[OP_ADD]){
                case "I":
                    addressingModeDescription += ADDR_I;
                    break;
                case "A":
                    addressingModeDescription += ADDR_A;
                    break;
                case "Z":
                    addressingModeDescription += ADDR_Z;
                    break;
                case "ABS":
                    addressingModeDescription += ADDR_ABS;
                    break;
                case "IND":
                    addressingModeDescription += ADDR_IND;
                    break;
                default:
                    throw new UnknownOpCodeException("Unrecognised addressing mode " + t[OP_ADD], Arrays.toString(t));
            }
            addressingModeDescription += getIndexingMode(t);
        }else{
            addressingModeDescription += ADDR_IMP;
        }
        return addressingModeDescription + ")";
    }

    private static String getIndexingMode(String[] t) {
        String indexingModeDescription = "";
        if (t.length > OP_I){
            switch (t[OP_I]){
                case "IX":
                    indexingModeDescription += INDEX_X;
                    break;
                case "IY":
                    indexingModeDescription += INDEX_Y;
                    break;
                default:
                    throw new UnknownOpCodeException("Unrecognised indexing mode " + t[OP_ADD] + " in " + Arrays.toString(t), Arrays.toString(t));
            }
        }
        return indexingModeDescription;
    }

}
