package com.rox.emu.p6502;

import com.rox.emu.UnknownOpCodeException;
import com.rox.emu.p6502.op.AddressingMode;
import com.rox.emu.p6502.op.OpCode;

import java.util.Arrays;
import java.util.StringTokenizer;

public class Compiler {
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
            program[i++] = decodeToken(tokenizer);
        }

        return Arrays.copyOf(program, i);
    }

    private int decodeToken(StringTokenizer tokenizer) throws UnknownOpCodeException {
        String token = tokenizer.nextToken();

        //  OpCodeName [ Param1 | Param1 Param2 ]

        switch(token){
            case "SEC":
                return OpCode.OP_SEC.getByteValue();
            default:
                throw new UnknownOpCodeException("Unknown op-code while parsing program", token);
        }
    }
}
