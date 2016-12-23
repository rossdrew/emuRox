package com.rox.emu.P6502

import com.rox.emu.Memory
import com.rox.emu.SimpleMemory
import spock.lang.Specification
import spock.lang.Unroll

import static com.rox.emu.P6502.InstructionSet.*;

class OpCodeSpec extends Specification {

    @Unroll("LDA Immediate #Expected: Load #loadValue == #expectedAccumulator")
    def testImmediateLDA() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, loadValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadValue | expectedAccumulator | PC | Z     | N     | Expected
        0x0       | 0x0                 | 2  | true  | false | "With zero result"
        0x1       | 0x1                 | 2  | false | false | "Generic test 1"
        0x7F      | 0x7F                | 2  | false | false | "Generic test 2"
        0x80      | 0x80                | 2  | false | true  | "With negative result"
        0x81      | 0x81                | 2  | false | true  | "With (boundary test) negative result "
        0xFF      | 0xFF                | 2  | false | true  | "With max negative result"
    }

    @Unroll("LDA ZeroPage #Expected: Expecting #loadValue @ [30]")
    def testLDAFromZeroPage() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_Z, 30]
        memory.setByte(30, loadValue)
        memory.setMemory(0, program)

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadValue | expectedAccumulator | PC | Z     | N     | Expected
        0x0       | 0x0                 | 2  | true  | false | "With zero result"
        0x1       | 0x1                 | 2  | false | false | "Generic test 1"
        0x7F      | 0x7F                | 2  | false | false | "Generic test 2"
        0x80      | 0x80                | 2  | false | true  | "With negative result"
        0x81      | 0x81                | 2  | false | true  | "With (boundary test) negative result "
        0xFF      | 0xFF                | 2  | false | true  | "With max negative result"
    }

    @Unroll("LDA Absolute #Expected: Expecting #loadValue @ [300]")
    def testAbsoluteLDA() {
        when:
        Memory memory = new SimpleMemory(65534);
        //Load a memory address above Zero Page (>256) using [Opcode] [Low Order Byte] [High Order Byte]
        //   [2C, 1] == [1, 2C] == 0b100101100 == 300
        int[] program = [OP_LDA_A, 0x2C, 0x1]
        memory.setByte(300, loadValue)
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step()
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        loadValue | expectedAccumulator | PC | Z     | N     | Expected
        0x0       | 0x0                 | 3  | true  | false | "With zero result"
        0x1       | 0x1                 | 3  | false | false | "Generic test 1"
        0x7F      | 0x7F                | 3  | false | false | "Generic test 2"
        0x80      | 0x80                | 3  | false | true  | "With negative result"
        0x81      | 0x81                | 3  | false | true  | "With (boundary test) negative result "
        0xFF      | 0xFF                | 3  | false | true  | "With max negative result"
    }

    @Unroll("LDA Indexed Zero Page, X #Expected: Load [0x30 + X(#index)] -> #expectedAccumulator")
    def testLDAFromZeroPageIndexedByX() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDA_Z_IX, 0x30]
        int[] values = [0, 11, 0b11111111]
        memory.setMemory(0x30, values)
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | expectedAccumulator | PC | Z     | N     | Expected
          0   | 0                   | 4  | true  | false | "With zero result"
          1   | 11                  | 4  | false | false | "With normal result"
          2   | 0xFF                | 4  | false | true  | "With negative result"
    }

    @Unroll("LDA Indexed by X. #Expected: 300[#index] = #expectedAccumulator")
    def testLDAIndexedByX() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDX_I, index, OP_LDA_IX, 0x2C, 1]
        int[] values = [0, 11, 0b11111111]
        memory.setMemory(300, values)
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | expectedAccumulator | PC | Z     | N     | Expected
        0     | 0                   | 5  | true  | false | "With zero result"
        1     | 11                  | 5  | false | false | "With normal result"
        2     | 0xFF                | 5  | false | true  | "With negative result"
    }

    @Unroll("LDA Indexed by Y. #Expected: 300[#index] = #expectedAccumulator")
    def testLDAIndexedByY() {
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDY_I, index, OP_LDA_IY, 0x2C, 1]
        int[] values = [0, 11, 0b11111111]
        memory.setMemory(300, values)
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        index | expectedAccumulator | PC | Z     | N     | Expected
        0     | 0                   | 5  | true  | false | "With zero result"
        1     | 11                  | 5  | false | false | "With normal result"
        2     | 0xFF                | 5  | false | true  | "With negative result"
    }

    @Unroll("ADC Immediate #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    def testADC(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_ADC_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | C     | O     | Expected
        0x0        | 0x0         | 0x0                 | 4   | true   | false | false | false | "With zero result"
        0x50       | 0xD0        | 0x20                | 4   | false  | false | true  | false | "With positive, carried result"
        0x50       | 0x50        | 0xA0                | 4   | false  | true  | false | true  | "With negative overflow"
        0x80       | 0x1         | 0x81                | 4   | false  | true  | false | false | "With valid negative result"     //Essentially subtractions
        0xFF       | 0xFF        | 0xFE                | 4   | false  | true  | false | false | "With negative, carried result"
    }

    @Unroll()
    def testMultiByteADC(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_CLC,
                         OP_LDA_I, lowFirstByte,
                         OP_ADC_I, lowSecondByte,
                         OP_STA_Z, 40,
                         OP_LDA_I, highFirstByte,
                         OP_ADC_I, highSecondByte]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(6)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        C == registers.statusFlags[Registers.C]
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]

        where:
        lowFirstByte | lowSecondByte | highFirstByte | highSecondByte | expectedAccumulator | PC  | Z      | N     | C     | O     | Expected
        0            | 0             | 0             | 0              | 0                   | 11  | true   | false | false | false | "With zero result"
        //TODO test cases for carries and not carries
    }

    @Unroll("AND Immediate #Expected:  #firstValue & #secondValue = #expectedAccumulator in Accumulator.")
    def testAND(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_AND_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | 4   | false  | false | "Unchanged accumulator"
        0b00000001 | 0b00000010  | 0b00000000          | 4   | true   | false | "No matching bits"
        0b00000011 | 0b00000010  | 0b00000010          | 4   | false  | false | "1 matched bit, 1 unmatched"
        0b00101010 | 0b00011010  | 0b00001010          | 4   | false  | false | "Multiple matched/unmatched bits"
    }

    @Unroll("OR Immediate #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    def testOR(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_OR_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | 4   | false  | false | "Duplicate bits"
        0b00000000 | 0b00000001  | 0b00000001          | 4   | false  | false | "One bit in Accumulator"
        0b00000001 | 0b00000000  | 0b00000001          | 4   | false  | false | "One bit from passed value"
        0b00000001 | 0b00000010  | 0b00000011          | 4   | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 0b10000010  | 0b10000011          | 4   | false  | true  | "Negative result"
    }

    @Unroll("EOR Immediate #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    def testEOR(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_LDA_I, firstValue, OP_EOR_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(2)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        N == registers.statusFlags[Registers.N]

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | Expected
        0b00000001 | 0b00000000  | 0b00000001          | 4   | false  | false | "One"
        0b00000000 | 0b00000001  | 0b00000001          | 4   | false  | false | "The other"
        0b00000001 | 0b00000001  | 0b00000000          | 4   | true   | false | "Not both"
    }

    @Unroll("SBC Immediate #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    def testSBC(){
        when:
        Memory memory = new SimpleMemory(65534);
        int[] program = [OP_SEC, OP_LDA_I, firstValue, OP_SBC_I, secondValue]
        memory.setMemory(0, program);

        and:
        CPU processor = new CPU(memory)
        processor.reset()
        processor.step(3)
        Registers registers = processor.getRegisters()

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        PC == registers.getPC()
        Z == registers.statusFlags[Registers.Z]
        O == registers.statusFlags[Registers.V]
        N == registers.statusFlags[Registers.N]
        C == registers.statusFlags[Registers.C]

        where:
        firstValue | secondValue | expectedAccumulator | PC  | Z      | N     | O     | C     | Expected
        0x5        | 0x3         | 0x2                 | 5   | false  | false | false | false | "Basic subtraction"
        0x5        | 0x5         | 0x0                 | 5   | true   | false | false | false | "With zero result"
        0x5        | 0x6         | 0xFF                | 5   | false  | true  | false | false | "with negative result"
    }
}
