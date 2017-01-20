package com.rox.emu.p6502.com.rox.emu.p6502.asm

import spock.lang.Specification
import com.rox.emu.p6502.asm.P6502Assembler

public class AssemblerSpec  extends Specification {
    def testCompilerCreation() {
        when:
        P6502Assembler asm = new P6502Assembler()

        and:
        byte[] program = asm.process("")

        then:
        program.length == 0
    }


}
