package com.rox.emu.p6502;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.p6502.op.AddressingMode;
import com.rox.emu.p6502.op.OpCode;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {
    public static final String IMMEDIATE_PREFIX = "#$";

    private static final Pattern PREFIX_REGEX = Pattern.compile("^\\D+");
    private static final Pattern VALUE_REGEX = Pattern.compile("\\d+");

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

            //  OpCodeName [ Param1 | Param1 Param2 ]

            switch(opCodeToken){
                case "TAX":
                case "TAY":
                case "TYA":
                case "TXA":
                case "TXS":
                case "TXY":
                case "TSX":
                case "PHA":
                case "PLA":
                case "PHP":
                case "PLP":
                case "INY":
                case "DEY":
                case "INX":
                case "DEX":
                case "RTS":
                case "JSR":
                case "BPL":
                case "BMI":
                case "BVC":
                case "BVS":
                case "BCC":
                case "BCS":
                case "BNE":
                case "BEQ":
                case "SEC":
                case "CLC":
                case "SEI":
                case "SED":
                case "CLD":
                case "CLI":
                case "CLV":
                case "BRK":
                case "NOP":
                    program[i++] = OpCode.from(opCodeToken).getByteValue();
                    break;
                case "ADC":
                case "LDA":
                case "AND":
                case "ORA":
                case "EOR":
                case "SBC":
                case "LDY":
                case "LDX":
                case "CMP":
                case "CPX":
                case "CPY":
                    final String valueToken = tokenizer.nextToken().trim();
                    final String prefix = extractFirstOccurrence(PREFIX_REGEX, valueToken);
                    final String value = extractFirstOccurrence(VALUE_REGEX, valueToken);

                    if (prefix.compareToIgnoreCase(IMMEDIATE_PREFIX) == 0){
                        AddressingMode addressingMode = AddressingMode.IMMEDIATE;

                        program[i++] = OpCode.from(opCodeToken, addressingMode).getByteValue();
                        program[i++] = Integer.decode(value);
                    }
                    break;
                default:
                    throw new UnknownOpCodeException("Unknown op-code (\"" + opCodeToken + "\") while parsing program", opCodeToken);
            }
        }

        return Arrays.copyOf(program, i);
    }

    private String extractFirstOccurrence(Pattern pattern, String token){
        final Matcher prefixMatcher = pattern.matcher(token);
        prefixMatcher.find();
        return prefixMatcher.group(0);
    }

    private int decodeToken(StringTokenizer tokenizer) throws UnknownOpCodeException {
        String token = tokenizer.nextToken();

        //  OpCodeName [ Param1 | Param1 Param2 ]

        switch(token){
            case "SEC":
                return OpCode.OP_SEC.getByteValue();
            case "ADC":
                final String value = tokenizer.nextToken().trim();
                if (value.startsWith("#$")){

                }
            default:
                throw new UnknownOpCodeException("Unknown op-code while parsing program", token);
        }
    }
}
