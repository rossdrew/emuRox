package com.rox.emu.p6502;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.p6502.op.OpCode;

import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {
    private static final String IMMEDIATE_PREFIX = "#$";

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
            String token = tokenizer.nextToken();

            //  OpCodeName [ Param1 | Param1 Param2 ]

            switch(token){
                case "SEC":
                    program[i++] = OpCode.OP_SEC.getByteValue();
                    break;
                case "ADC":
                    final String valueToken = tokenizer.nextToken().trim();
                    final Matcher prefixMatcher = PREFIX_REGEX.matcher(valueToken);
                    prefixMatcher.find();
                    final Matcher valueMatcher = VALUE_REGEX.matcher(valueToken);
                    valueMatcher.find();
                    String group = prefixMatcher.group(0);
                    String value = valueMatcher.group(0);

                    if (group.compareToIgnoreCase("#$") == 0){ //TODO can I convert this to an AddressingMode and can that convert an ADC to an addressed OP_ADC_I
                        program[i++] = OpCode.OP_ADC_I.getByteValue();
                        program[i++] = Integer.decode(value);
                    }
                    break;
                default:
                    throw new UnknownOpCodeException("Unknown op-code while parsing program", token);
            }
        }

        return Arrays.copyOf(program, i);
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
