package com.rox.emu.p6502;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.p6502.op.AddressingMode;
import com.rox.emu.p6502.op.OpCode;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {
    public static final String VALUE_PREFIX = "$";
    public static final String IMMEDIATE_PREFIX = "#" + VALUE_PREFIX;

    private static final Pattern PREFIX_REGEX = Pattern.compile("^[(#$?) ($)?]\\D+");
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
                    final String prefix = extractFirstOccurrence(PREFIX_REGEX, valueToken, opCodeToken);
                    final String value = extractFirstOccurrence(VALUE_REGEX, valueToken, opCodeToken);

                    if (prefix.equalsIgnoreCase(IMMEDIATE_PREFIX)){
                        AddressingMode addressingMode = AddressingMode.IMMEDIATE;

                        program[i++] = OpCode.from(opCodeToken, addressingMode).getByteValue();
                        program[i++] = Integer.decode(value);
                    }else{
                        throw new UnknownOpCodeException("Addressing mode or opcode not implemented", valueToken + " " + prefix + value);
                    }
                    break;
                default:
                    throw new UnknownOpCodeException("Unknown op-code (\"" + opCodeToken + "\") while parsing program", opCodeToken);
            }
        }

        return Arrays.copyOf(program, i);
    }

    private String extractFirstOccurrence(Pattern pattern, String token, String opCode){
        final Matcher prefixMatcher = pattern.matcher(token);
        prefixMatcher.find();
        try {
            String result = prefixMatcher.group();
            return prefixMatcher.group();
        }catch(IllegalStateException | ArrayIndexOutOfBoundsException e){
            throw new UnknownOpCodeException("Could not parse argument for " + opCode + " from '" + token + "'", token, e);
        }
    }
}
