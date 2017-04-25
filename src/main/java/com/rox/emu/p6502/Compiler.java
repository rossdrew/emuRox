package com.rox.emu.p6502;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.p6502.op.AddressingMode;
import com.rox.emu.p6502.op.OpCode;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {
    private static final Pattern PREFIX_REGEX = Pattern.compile("^\\D+");
    private static final Pattern VALUE_REGEX = Pattern.compile("\\d+");
    private static final Pattern POSTFIX_REGEX = Pattern.compile("[^\\d]*$");

    public static final String IMMEDIATE_PREFIX = "#";
    public static final String VALUE_PREFIX = "$";
    public static final String IMMEDIATE_VALUE_PREFIX = IMMEDIATE_PREFIX + VALUE_PREFIX;
    public static final String INDIRECT_X_PREFIX = "(" + VALUE_PREFIX;

    public static final String X_INDEXED_POSTFIX = ",X";
    public static final String Y_INDEXED_POSTFIX = ",Y";
    public static final String INDIRECT_X_POSTFIX = X_INDEXED_POSTFIX + ")";

    private final String programText;

    public Compiler(String programText){
        this.programText = programText;
    }

    public int[] getBytes() {
        return compileProgram();
    }

    private int[] compileProgram() throws UnknownOpCodeException{
        int[] program = new int[2000];
        int i=0;

        StringTokenizer tokenizer = new StringTokenizer(programText);
        while (tokenizer.hasMoreTokens()){
            String opCodeToken = tokenizer.nextToken();

            //  OpCodeName Param1(1..2 bytes)

            switch(opCodeToken){
                case "TAX": case "TAY":
                case "TYA": case "TXA": case "TXS": case "TXY": case "TSX":
                case "PHA": case "PLA":
                case "PHP": case "PLP":
                case "INY": case "DEY":
                case "INX": case "DEX":
                case "RTS": case "RTI":
                case "JSR":
                case "BPL": case "BMI": case "BVC": case "BVS": case "BCC": case "BCS": case "BNE": case "BEQ":
                case "SEC": case "CLC":
                case "SEI": case "SED":
                case "CLD": case "CLI": case "CLV":
                case "BRK":
                case "NOP":
                    program[i++] = OpCode.from(opCodeToken).getByteValue();
                    break;
                case "ADC": case "SBC":
                case "LDA": case "LDY": case "LDX":
                case "AND": case "ORA": case "EOR":
                case "ASL": case "ROL": case "LSR":
                case "STY": case "STX": case "STA":
                case "CMP": case "CPX": case "CPY":
                case "INC": case "DEC":
                case "BIT":

                case "JMP": //Absolute only

                case "ROR": //Accumulator only
                    final String valueToken = tokenizer.nextToken().trim();
                    final String prefix = extractFirstOccurrence(PREFIX_REGEX, valueToken, opCodeToken);
                    final String value = extractFirstOccurrence(VALUE_REGEX, valueToken, opCodeToken);
                    final String postfix = extractFirstOccurrence(POSTFIX_REGEX, valueToken, opCodeToken);

                    final AddressingMode addressingMode = getAddressingModeFrom(prefix, value, postfix);

                    program[i++] = OpCode.from(opCodeToken, addressingMode).getByteValue();
                    program[i++] = Integer.decode(value);
                    break;
                default:
                    throw new UnknownOpCodeException("Unknown op-code (\"" + opCodeToken + "\") while parsing program", opCodeToken);
            }
        }

        return Arrays.copyOf(program, i);
    }

    private AddressingMode getAddressingModeFrom(String prefix, String value, String postfix){
        if (prefix.equalsIgnoreCase(IMMEDIATE_VALUE_PREFIX)) {
            return AddressingMode.IMMEDIATE;
        }else if (prefix.equalsIgnoreCase(IMMEDIATE_PREFIX)){
            return AddressingMode.ACCUMULATOR;
        }else if (prefix.equalsIgnoreCase(VALUE_PREFIX)){
            if (value.length() <= 3) {
                if (postfix.equalsIgnoreCase(X_INDEXED_POSTFIX)) {
                    return AddressingMode.ZERO_PAGE_X;
                } else if (postfix.equalsIgnoreCase(Y_INDEXED_POSTFIX)){
                    return AddressingMode.ZERO_PAGE_Y;
                } else {
                    return AddressingMode.ZERO_PAGE;
                }
            }else if (value.length() <= 4){
                if (postfix.equalsIgnoreCase(X_INDEXED_POSTFIX)) {
                    return AddressingMode.ABSOLUTE_X;
                } else if (postfix.equalsIgnoreCase(Y_INDEXED_POSTFIX)){
                    return AddressingMode.ABSOLUTE_Y;
                } else {
                    return AddressingMode.ABSOLUTE;
                }
            }
        }else if (prefix.equalsIgnoreCase(INDIRECT_X_PREFIX)){
            if (postfix.equalsIgnoreCase(INDIRECT_X_POSTFIX)){
                return AddressingMode.INDIRECT_X;
            }
        }

        throw new UnknownOpCodeException("Invalid or unimplemented", prefix+value);
    }

    private String extractFirstOccurrence(Pattern pattern, String token, String opCode){
        final Matcher prefixMatcher = pattern.matcher(token);
        prefixMatcher.find();
        try {
            return prefixMatcher.group();
        }catch(IllegalStateException | ArrayIndexOutOfBoundsException e){
            throw new UnknownOpCodeException("Could not parse argument for " + opCode + " from '" + token + "'", token, e);
        }
    }
}
