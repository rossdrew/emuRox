package com.rox.emu.P6502

import spock.lang.Specification
import spock.lang.Unroll

class OpCodeSpec extends Specification {

    @Unroll("LDA Immediate #Expected: Load #loadValue")
    def "LDA (Load Accumulator) Test"() {
        when:
        int[] memory = new int[65534]
        int[] program = [CPU.OP_LDA_I, loadValue]
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        //C == registers.statusFlags[CPU.STATUS_FLAG_CARRY]
        Z == registers.getStatusFlags()[1]
        //I == registers.statusFlags[CPU.STATUS_FLAG_IRQ_DISABLE]
        //D == registers.statusFlags[CPU.STATUS_FLAG_DEC]
        //B == registers.statusFlags[CPU.STATUS_FLAG_BREAK]
        //U == registers.statusFlags[CPU.STATUS_FLAG_UNUSED]
        //O == registers.statusFlags[CPU.STATUS_FLAG_OVERFLOW]
        N == registers.statusFlags[7]

        where:
        loadValue | expectedAccumulator | PC | Z     | N     | Expected
        0x0       | 0x0                 | 2  | true  | false | "With zero result"
        0x1       | 0x1                 | 2  | false | false | ""
        0x7F      | 0x7F                | 2  | false | false | ""
        0x80      | 0x80                | 2  | false | true  | "With negative result"
        0x81      | 0x81                | 2  | false | true  | "With negative result"
        0xFF      | 0xFF                | 2  | false | true  | "With negative result"

        //loadValue | expectedAccumulator  | PC | C      | Z     | I     | D     | B     | U     | O     | N
    }

    @Unroll("LDA ZeroPage #Expected: Load #loadValue")
    def "LDA (Load Accumulator) from Zero Page Test"() {
        when:
        int[] memory = new int[65534]
        int[] program = [CPU.OP_LDA_Z, 30]
        memory[30] = loadValue
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.getStatusFlags()[1]
        N == registers.statusFlags[7]

        where:
        loadValue | expectedAccumulator | PC | Z     | N     | Expected
        0x0       | 0x0                 | 2  | true  | false | "With zero result"
        0x1       | 0x1                 | 2  | false | false | ""
        0x7F      | 0x7F                | 2  | false | false | ""
        0x80      | 0x80                | 2  | false | true  | "With negative result"
        0x81      | 0x81                | 2  | false | true  | "With negative result"
        0xFF      | 0xFF                | 2  | false | true  | "With negative result"
    }

    @Unroll("LDA Absolute #Expected: Load #loadValue")
    def "LDA (Load Accumulator) Absolute Test"() {
        when:
        int[] memory = new int[65534]
        //Load a memory address above Zero Page (>256) using [Opcode] [Low Order Byte] [High Order Byte]
        //   [2C, 1] == [1, 2C] == 0b100101100 == 300
        int[] program = [CPU.OP_LDA_A, 0x2C, 0x1]
        memory[300] = loadValue
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.getStatusFlags()[1]
        N == registers.statusFlags[7]

        where:
        loadValue | expectedAccumulator | PC | Z     | N     | Expected
        0x0       | 0x0                 | 3  | true  | false | "With zero result"
        0x1       | 0x1                 | 3  | false | false | ""
        0x7F      | 0x7F                | 3  | false | false | ""
        0x80      | 0x80                | 3  | false | true  | "With negative result"
        0x81      | 0x81                | 3  | false | true  | "With negative result"
        0xFF      | 0xFF                | 3  | false | true  | "With negative result"
    }

    @Unroll("LDA Zero Page Indirect, X #Expected: Load #loadValue")
    def "LDA (Load Accumulator) Indirectly using X from Zero Page Test"() {
        when:
        int[] memory = new int[65534]
        int[] program = [CPU.OP_LDX_I, index, CPU.OP_LDA_IND, 0x2C, 0x1] //F6
        memory[34] = 55
        memory[33] = 44
        memory[32] = 44
        memory[31] = 33
        memory[30] = 22
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.getStatusFlags()[1]
        N == registers.statusFlags[7]

        where:
        loadValue | index | expectedAccumulator | PC | Z     | N     | Expected
        0x0       |   0   | 0x0                 | 4  | true  | false | "With zero result"
        0x1       |   0   | 0x1                 | 4  | false | false | ""
        0x7F      |   0   | 0x7F                | 4  | false | false | ""
        0x80      |   0   | 0x80                | 4  | false | true  | "With negative result"
        0x81      |   0   | 0x81                | 4  | false | true  | "With negative result"
        0xFF      |   0   | 0xFF                | 4  | false | true  | "With negative result"
    }

    @Unroll("ADC Immediate #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def "ADC (ADd with Carry to Accumulator) Test"(){
        when:
        int[] memory = new int[65534]
        int[] program = [CPU.OP_LDA_I, firstValue, CPU.OP_ADC_I, secondValue]
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        C == registers.statusFlags[0]
        Z == registers.statusFlags[1]
        O == registers.statusFlags[6]
        N == registers.statusFlags[7]

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | C     | O     | Expected
        0x0        | 0x0         | 0x0                 | 4   | true   | false | false | false | "With zero result"
        0x80       | 0x1         | 0x81                | 4   | false  | true  | false | false | "With valid negative result"
        0xFF       | 0xFF        | 0xFE                | 4   | false  | true  | true  | false | "With negative, carried result"
        0x50       | 0xD0        | 0x20                | 4   | false  | false | true  | false | "With positive, carried result"
        0x50       | 0x50        | 0xA0                | 4   | false  | true  | false | true  | "With negative overflow"
    }

    @Unroll("AND Immediate #Expected:  #firstValue & #secondValue = #expectedAccumulator in Accumulator.")
    def "AND (And with Accumulator) Test"(){
        when:
        int[] memory = new int[65534]
        int[] program = [CPU.OP_LDA_I, firstValue, CPU.OP_AND_I, secondValue]
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[1]
        N == registers.statusFlags[7]

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | 4   | false  | false | "Unchanged accumulator"
        0b00000001 | 0b00000010  | 0b00000000          | 4   | true   | false | "No matching bits"
        0b00000011 | 0b00000010  | 0b00000010          | 4   | false  | false | "1 matched bit, 1 unmatched"
        0b00101010 | 0b00011010  | 0b00001010          | 4   | false  | false | "Multiple matched/unmatched bits"
    }

    @Unroll("OR Immediate #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    def "OR (Or with Accumulator) Test"(){
        when:
        int[] memory = new int[65534]
        int[] program = [CPU.OP_LDA_I, firstValue, CPU.OP_OR_I, secondValue]
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[1]
        N == registers.statusFlags[7]

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | 4   | false  | false | "Duplicate bits"
        0b00000000 | 0b00000001  | 0b00000001          | 4   | false  | false | "One bit in Accumulator"
        0b00000001 | 0b00000000  | 0b00000001          | 4   | false  | false | "One bit from passed value"
        0b00000001 | 0b00000010  | 0b00000011          | 4   | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 0b10000010  | 0b10000011          | 4   | false  | true  | "Negative result"
    }

    @Unroll("EOR Immediate #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    def "EOR (Exclusive Or with Accumulator) Test"(){
        when:
        int[] memory = new int[65534]
        int[] program = [CPU.OP_LDA_I, firstValue, CPU.OP_EOR_I, secondValue]
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[1]
        N == registers.statusFlags[7]

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | Expected
        0b00000001 | 0b00000000  | 0b00000001          | 4   | false  | false | "One"
        0b00000000 | 0b00000001  | 0b00000001          | 4   | false  | false | "The other"
        0b00000001 | 0b00000001  | 0b00000000          | 4   | true   | false | "Not both"
    }

    @Unroll("SBC Immediate #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    def "SBC (Subtract from Accumulator) Test"(){
        when:
        int[] memory = new int[65534]
        int[] program = [CPU.OP_SEC, CPU.OP_LDA_I, firstValue, CPU.OP_SBC_I, secondValue]
        System.arraycopy(program, 0, memory, 0, program.length);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        processor.step()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[1]
        O == registers.statusFlags[6]
        N == registers.statusFlags[7]
        //TODO C

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | O     | Expected
        0x5        | 0x3         | 0x2                 | 5   | false  | false | false | "Basic subtraction"
        0x5        | 0x5         | 0x0                 | 5   | true   | false | false | "Zero subtraction"
        0x5        | 0x6         | 0xFF                | 5   | false  | true  | false | "Negative subtraction"
    }
}
