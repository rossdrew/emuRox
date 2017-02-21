package com.rox.emu.p6502.op;

/**
 * Utility for converting between internal and external OpCode representations.
 *
 * e.g.  OP_ADC_I  ->  "ADC (Immediate)"
 */
public class OpCodeNameConverter {
    private static final int OP = 0;
    private static final int ADD = 1;
    private static final int I = 2;

    public static String toDescription(String internalOpCodeName){
        String description = "<UNKNOWN OPCODE>";

        if (internalOpCodeName != null && !internalOpCodeName.isEmpty()){
            String tokens[] = internalOpCodeName.split("_");
            description = tokens [OP] + getAddressingMode(tokens);
        }

        return description;
    }

    private static String getAddressingMode(String[] t) {
        String addressingModeDescription = " (";

        if (t.length > ADD){
            switch (t[ADD]){
                case "I":
                    addressingModeDescription += "Immediate";
                    break;
                case "A":
                    addressingModeDescription += "Accumulator";
                    break;
                case "Z":
                    addressingModeDescription += "Zero Page";
                    break;
                case "ABS":
                    addressingModeDescription += "Absolute";
                    break;
                case "IND":
                    addressingModeDescription += "Indirect";
                    break;

            }
            addressingModeDescription += getIndexingMode(t);
        }else{
            addressingModeDescription += "Implied";
        }
        return addressingModeDescription + ")";
    }

    private static String getIndexingMode(String[] t) {
        String indexingModeDescription = "";
        if (t.length > I){
            switch (t[I]){
                case "IX":
                    indexingModeDescription += "[X]";
                    break;
                case "IY":
                    indexingModeDescription += "[Y]";
                    break;
            }
        }
        return indexingModeDescription;
    }
}
