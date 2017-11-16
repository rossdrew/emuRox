package com.rox.emu.processor.mos6502

import com.rox.emu.mem.Memory

import com.rox.emu.mem.SimpleMemory
import com.rox.emu.processor.mos6502.util.Program
import spock.lang.Specification
import spock.lang.Unroll

import static com.rox.emu.processor.mos6502.op.OpCode.*

class OpCodeSpec extends Specification {
    private Memory memory
    private Mos6502 processor
    private Registers registers
    
    def setup(){
        memory = new SimpleMemory()
        processor = new Mos6502(memory)
        processor.reset()
        registers = processor.getRegisters()
    }

    Program loadMemoryWithProgram(Object ... programElements){
        final Program program = new Program().with(programElements)
        memory.setBlock(0, program.getProgramAsByteArray())
        return program
    }

    boolean testFlags(boolean Z,
                      boolean N,
                      boolean C = registers.getFlag(Registers.C),
                      boolean V = registers.getFlag(Registers.V)){

        assert Z == registers.getFlag(Registers.Z) : "Expected Z to be " + Z + ", was " + registers.getFlag(Registers.Z)
        assert N == registers.getFlag(Registers.N) : "Expected N to be " + N + ", was " + registers.getFlag(Registers.N)
        assert C == registers.getFlag(Registers.C) : "Expected C to be " + C + ", was " + registers.getFlag(Registers.C)
        assert V == registers.getFlag(Registers.V) : "Expected V to be " + V + ", was " + registers.getFlag(Registers.V)

        return Z == registers.getFlag(Registers.Z) &&
               N == registers.getFlag(Registers.N) &&
               C == registers.getFlag(Registers.C) &&
               V == registers.getFlag(Registers.V)
    }

    /**
     * Value | Z  | N | Description
     */
    def loadValueTestData(){
        [
          [0x0,        true,   false,  "With zero result"],
          [0x1,        false,  false,  "Generic test 1"],
          [0x7F,       false,  false,  "Generic test 2"],
          [0x80,       false,  true,   "With negative result"],
          [0x81,       false,  true,   "With (boundary test) negative result "],
          [0xFF,       false,  true,   "With max negative result"],
          [0b01111111, false,  false,  "With max positive result"]
        ]
    }

    /**
     * First Value | Second  Value | Expected Accumulator | Z | N | C | V | Description
     */
    def adcTestData() {
        [
          [0x0,  0x0,  0x0,  true,  false, false, false, "With zero result"],
          [0x50, 0xD0, 0x20, false, false, true,  false, "With positive, carried result"],
          [0x50, 0x50, 0xA0, false, true,  false, true,  "With negative overflow"]
        ]
    }

    /**
     * First Value | Second Value | Result  | Z | N | C | V | Description
     */
    def sbcTestData() {
        [
          [0x5,         0x3,  0x2,          false, false, true,  false, "Basic subtraction"],
          [0x5,         0x5,  0x0,          true,  false, true,  false, "With zero result"],
          [0x5,         0x6,  0xFF,         false, true,  false, false, "With negative result"],
          [0b10000000,  0x1,  0b01111111,   false, false, true,  true,  "With overflow & carry"],
          [0x50,        0xB0, 0xA0,         false, true,  false, true,  "With overflow & no carry"]
        ]
    }

    /**
     * Value | Result | Z | N | C | Description
     */
    def aslTestData() {
        [
          [0b00010101, 0b00101010, false, false, false, "Basic shift"],
          [0b00000000, 0b00000000, true , false, false, "Zero shift"],
          [0b01000000, 0b10000000, false, true , false, "Negative shift"],
          [0b10000001, 0b00000010, false, false, true , "Carried shift"],
          [0b10000000, 0b00000000, true , false, true , "Carried, zero shift"],
          [0b11000000, 0b10000000, false, true , true,  "Carried, negative shift"]
        ]
    }

    /**
     * First Value | Second Value | Z | N | C | Description
     */
    def cmpTestData() {
        [
          [0x10, 0x10, true,  false, true,  "Basic compare"],
          [0x11, 0x10, false, false, true,  "Carry flag set"],
          [0x10, 0x11, false, true,  false, "Smaller value - larger"],
          [0xFF, 0x01, false, true,  true,  "Negative result"]
        ]
    }

    /**
     * Pre-Instr | First Value | Result | Z | N | C | Description
     */
    def rolTestData() {
        [
          [CLC, 0b00000001, 0b00000010, false, false, false, "Standard rotate left"],
          [CLC, 0b00000000, 0b00000000, true,  false, false, "Rotate to zero"],
          [CLC, 0b01000000, 0b10000000, false, true,  false, "Rotate to negative"],
          [CLC, 0b10000001, 0b00000010, false, false, true,  "Rotate to carry out"],
          [SEC, 0b00000001, 0b00000011, false, false, false, "Rotate with carry in, no carry out"],
          [SEC, 0b10000000, 0b00000001, false, false, true,  "Carry in then carry out"],
          [SEC, 0b01000000, 0b10000001, false, true,  false, "Carry in to negative"]
        ]
    }

    /**
     * Index | {Test Data ...}
     */
    def withIndex(testData){
        int i = 0
        testData.collect{ [i++, *it] }
    }

    /**
     * Pointer Hi | Pointer Low | {Test Data ...}
     */
    def withWordPointer(testData){
        int hi = 1
        int lo = 10
        testData.collect{ [hi++, lo+=10, *it] }
    }

    /**
     * Pointer Low | {test data ...}
     */
    def withBytePointer(testData){
        int lo = 10
        testData.collect{ [lo+=10, *it] }
    }
    
    @Unroll("LDA (Immediate) #Expected: Load #value")
    testImmediateLDA() {
        when:
        Program program = loadMemoryWithProgram(LDA_I, value)

        and:
        processor.step()
    
        then:
        value == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
        testFlags(Z, N)

        where:
        [value, Z, N, Expected] << loadValueTestData()
    }
    
    @Unroll("LDA (Zero Page) #Expected: Expecting #value @ [30]")
    testLDAFromZeroPage() {
        when:
        Program program = loadMemoryWithProgram(LDA_Z, 30)
        memory.setByteAt(30, value)

        and:
        processor.step()
    
        then:
        value == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
        testFlags(Z, N)
    
        where:
        [value, Z, N, Expected] << loadValueTestData()
    }
    
    @Unroll("LDA (Zero Page[X]) #Expected: Load [0x30 + X(#index)] -> #value")
    testLDAFromZeroPageIndexedByX() {
        when:
        Program program = loadMemoryWithProgram(LDX_I, index, LDA_Z_IX, 0x30)
        memory.setByteAt(0x30 + index, value)

        and:
        processor.step(2)
    
        then:
        value == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
        testFlags(Z, N)
    
        where:
        [index, value, Z, N, Expected] << withIndex(loadValueTestData())
    }
    
    @Unroll("LDA (Absolute) #Expected: Expecting #value @ [300]")
    testAbsoluteLDA() {
        when:
        Program program = loadMemoryWithProgram(LDA_ABS, 0x1, 0x2C)
        memory.setByteAt(300, value)

        and:
        processor.step()
    
        then:
        value == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
        testFlags(Z, N)
    
        where:
        [value, Z, N, Expected] << loadValueTestData()
    }
    
    @Unroll("LDA (Absolute[X]). #Expected: 300[#index] = #value")
    testLDAIndexedByX() {
        when:
        Program program = loadMemoryWithProgram(LDX_I, index, LDA_ABS_IX, 1, 0x2C)
        memory.setByteAt(300 + index, value)

        and:
        processor.step(2)
    
        then:
        value == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
        testFlags(Z, N)
    
        where:
        [index, value, Z, N, Expected] << withIndex(loadValueTestData())
    }
    
    
    @Unroll("LDA (Absolute[Y]). #Expected: 300[#index] = #value")
    testLDAIndexedByY() {
        when:
        Program program = loadMemoryWithProgram(LDY_I, index, LDA_ABS_IY, 1, 0x2C)
        memory.setByteAt(300 + index, value)

        and:
        processor.step(2)
    
        then:
        value == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
        testFlags(Z, N)
    
        where:
        [index, value, Z, N, Expected] << withIndex(loadValueTestData())
    }
    
    @Unroll("LDA (Indirect, X). #Expected: 0x30[#index] -> [#indAddressHi|#indAddressLo] = #value")
    testLDA_IND_IX() {
        when:
        Program program = loadMemoryWithProgram(LDA_I, value,    //Value at indirect address
                                                STA_ABS, indAddressHi, indAddressLo,
                                                LDX_I, index,
                                                LDA_I, indAddressHi,  //Indirect address in memory
                                                STA_Z_IX, 0x30,
                                                LDA_I, indAddressLo,
                                                STA_Z_IX, 0x31,
                                                LDA_IND_IX, 0x30)
    
        and:
        processor.step(8)
    
        then:
        value == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
        testFlags(Z, N)
    
        where:
        [indAddressHi, indAddressLo, index, value, Z, N, Expected] << withWordPointer(withIndex(loadValueTestData()))
    }
    
    @Unroll("LDA (Indirect, Y). #expected: 0x60 -> *[#pointerHi|#pointerLo]@[#index] = #expectedValue")
    testLDA_IND_IY() {
        when:
        loadMemoryWithProgram(LDY_I, index,           //Index to use
                              LDA_I, expectedValue,   //High order byte at pointer
                              STA_ABS_IY, pointerHi, pointerLo,
                              LDA_I, pointerHi,       //Pointer location
                              STA_Z, 0x60,
                              LDA_I, pointerLo,       //Pointer location
                              STA_Z, 0x61,
                              LDA_I, 0x0,             //Reset accumulator
                              LDA_IND_IY, 0x60)

        and:
        processor.step(9)
    
        then:
        expectedValue == registers.getRegister(Registers.REG_ACCUMULATOR)
        testFlags(Z, N)
    
        where:
        [pointerHi, pointerLo, index, expectedValue, Z, N, Expected] << withWordPointer(withIndex(loadValueTestData()))
    }
    
    @Unroll("LDX (Immediate): Load #value")
    testLDX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, value)

        and:
        processor.step()
    
        then:
        value == registers.getRegister(Registers.REG_X_INDEX)
        program.length == registers.getPC()
        testFlags(Z,N)
    
        where:
        [value, Z, N, Expected] << loadValueTestData()
    }
    
    @Unroll("LDX (Absolute): Load #value from [#addressHi | #addressLo]")
    testLDX_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDX_ABS, addressHi, addressLo)
    
        and:
        memory.setByteAt(addressHi << 8 | addressLo, value)
    
        and:
        processor.step()
    
        then:
        value == registers.getRegister(Registers.REG_X_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        [addressHi, addressLo, value, Z, N, Expected] << withWordPointer(loadValueTestData())
    }
    
    @Unroll("LDX (Absolute[Y]): #Expected #value from [#addressHi | #addressLo]")
    testLDX_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDA_I, value,
                                                STA_ABS_IY, addressHi, addressLo,
                                                LDX_ABS_IY, addressHi, addressLo)

        and:
        processor.step(4)
    
        then:
        value == registers.getRegister(Registers.REG_X_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        [index, addressHi, addressLo, value, Z, N, Expected] << withIndex(withWordPointer(loadValueTestData()))
    }
    
    @Unroll("LDX (Zero Page): Load #firstValue from [#address]")
    testLX_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, value,
                                                STA_Z, address,
                                                LDX_Z, address)

        and:
        processor.step(3)
    
        then:
        value == registers.getRegister(Registers.REG_X_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        [address, value, Z, N, Expected] << withBytePointer(loadValueTestData())
    }
    
    @Unroll("LDX (Zero Page[Y]): Load #value from [#address[#index]")
    testLX_Z_IY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDA_I, value,
                                                STA_Z, address + index,
                                                LDX_Z_IY, address)

        and:
        processor.step(4)
    
        then:
        value == registers.getRegister(Registers.REG_X_INDEX)
        program.length == registers.getPC()
		testFlags(Z, N)
    
        where:
        [address, index, value, Z, N, Expected] << withBytePointer(withIndex(loadValueTestData()))
    }
    
    @Unroll("LDY (Immediate): Load #value")
    testLDY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, value)

        and:
        processor.step()

        then:
        value == registers.getRegister(Registers.REG_Y_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        [value, Z, N, Expected] << loadValueTestData()
    }
    
    @Unroll("LDY (Zero Page): Load #value from [#address]")
    testLDY_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, value,
                                                STA_Z, address,
                                                LDY_Z, address)

        and:
        processor.step(3)
    
        then:
        value == registers.getRegister(Registers.REG_Y_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        [address, value, Z, N, Expected] << withBytePointer(loadValueTestData())
    }
    
    @Unroll("LDY (Zero Page[X]): Load Y with #value from #address[#index]")
    testLDY_Z_IX(){
        when: 'We place a value in memory[X] via the accumulator, then retrieve it again into Y'
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, value,
                                                STA_Z_IX, address,
                                                LDY_Z_IX, address)

        and:
        processor.step(4)

        then:
        value == registers.getRegister(Registers.REG_Y_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        [address, index, value, Z, N, Expected] << withBytePointer(withIndex(loadValueTestData()))
    }
    
    @Unroll("LDY (Absolute): Load Y with #value at [#addressHi | #addressLo]")
    testLDY_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDY_ABS, addressHi, addressLo)

        and: 'The required value is present in memory'
        memory.setByteAt(addressHi << 8 | addressLo, value)
    
        and:
        processor.step()
    
        then:
        value == registers.getRegister(Registers.REG_Y_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        [addressHi, addressLo, value, Z, N, Expected] << withWordPointer(loadValueTestData())
    }
    
    @Unroll("LDY (Absolute[X]): Load Y with #value at [#addressHi | #addressLo][#index]")
    testLDY_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index, LDY_ABS_IX, addressHi, addressLo)

        and: 'The required value is present in memory'
        memory.setByteAt((addressHi << 8 | addressLo)+index, value)
    
        and:
        processor.step(2)
    
        then:
        value == registers.getRegister(Registers.REG_Y_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        [addressHi, addressLo, index, value, Z, N, Expected] << withWordPointer(withIndex(loadValueTestData()))
    }
    
    @Unroll("ADC (Immediate) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    testADC(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue, ADC_I, secondValue)

        and:
        processor.step(2)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << adcTestData()
    }
    
    @Unroll("ADC (Zero Page[X]) #Expected: #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    testADC_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                LDX_I, index,
                                                ADC_Z_IX, memoryAddress)
        
        and:
        memory.setByteAt(memoryAddress+index, secondValue)
        
        and:
        processor.step(3)
    
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [memoryAddress, index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withBytePointer(withIndex(adcTestData()))
    }
    
    @Unroll("ADC (Zero Page) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    testADC_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue, ADC_Z, 0x30)

        and:
        memory.setByteAt(0x30, secondValue)
    
        and:
        processor.step(2)
        
    
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << adcTestData()
    }
    
    @Unroll("ADC (Absolute) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    testADC_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue, ADC_ABS, 0x1, 0x2C)

        and:
        memory.setByteAt(300, secondValue)
    
        and:
        processor.step(2)
        
    
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << adcTestData()
    }
    
    @Unroll("ADC (Absolute[X)) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    testADC_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                                 LDA_I, firstValue,
                                                                 ADC_ABS_IX, 0x1, 0x2C)

        and:
        memory.setByteAt(300 + index, secondValue)
    
        and:
        processor.step(3)
    
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withIndex(adcTestData())
    }
    
    @Unroll("ADC (Absolute[Y]) #Expected:  #firstValue + #secondValue = #expectedAccumulator in Accumulator.")
    testADC_ABS_IY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDA_I, firstValue,
                                                ADC_ABS_IY, 0x1, 0x2C)
        
        and:
        memory.setByteAt(300 + index, secondValue)
    
        and:
        processor.step(3)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withIndex(adcTestData())
    }
    
    @Unroll("ADC (Indirect, X) #Expected: #firstValue *[#locationHi|#locationLo] & #secondValue = #expectedAccumulator")
    testADC_IND_IX() {
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,    //Value at indirect address
                                                STA_ABS, locationHi, locationLo,
                                                LDX_I, index,
                                                LDA_I, locationHi,   //Indirect address in memory
                                                STA_Z_IX, 0x30,
                                                LDA_I, locationLo,
                                                STA_Z_IX, 0x31,
                                                LDA_I, secondValue,
                                                ADC_IND_IX, 0x30)
        
    
        and:
        processor.step(9)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [locationHi, locationLo, index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withWordPointer(withIndex(adcTestData()))
    }
    
    @Unroll("ADC (Indirect, Y) #Expected: #firstValue + #secondValue -> #expectedAccumulator")
    testADC_IND_IY() {
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,           //Index to use
                                                LDA_I, firstValue,      //High order byte at pointer
                                                STA_ABS_IY, pointerHi, pointerLo,
                                                LDA_I, pointerHi,       //Pointer location
                                                STA_Z, 0x60,
                                                LDA_I, pointerLo,       //Pointer location
                                                STA_Z, 0x61,
                                                LDA_I, secondValue,
                                                ADC_IND_IY, 0x60)

        and:
        processor.step(9)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [pointerHi, pointerLo, index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withWordPointer(withIndex(adcTestData()))
    }
    
    @Unroll("ADC 16bit [#lowFirstByte|#highFirstByte] + [#lowSecondByte|#highSecondByte] = #Expected")
    testMultiByteADC(){
        when:
        Program program = loadMemoryWithProgram(CLC,
                                                LDA_I, lowFirstByte,
                                                ADC_I, lowSecondByte,
                                                STA_Z, 40,
                                                LDA_I, highFirstByte,
                                                ADC_I, highSecondByte)

        and:
        registers.clearFlag(Registers.C)

        and:
        processor.step(6)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,O)
        storedValue == memory.getByte(40)

        /**
         This should result in an Accumulator of 01 with no flags set

         CLC
         LDA #$50
         ADC #$D0
         STA $40
         LDA #$0
         ADC #$0

         same with (with 02 in Accumulator)

         CLC
         LDA #$50
         ADC #$D3
         STA $40
         LDA #$0
         ADC #$1

         C is being set in both
         */

        where:
        lowFirstByte | lowSecondByte | highFirstByte | highSecondByte | expectedAccumulator         | storedValue | Z     | N     | C     | O     | Expected
        0            | 0             | 0             | 0              | 0                           | 0           | true  | false | false | false | "With zero result"
        0x50         | 0xD0          | 0             | 0              | 1                           | 0x20        | false | false | false | false | "With simple carry to high byte"
        0x50         | 0xD3          | 0             | 1              | 2                           | 0x23        | false | false | false | false | "With carry to high byte and changed high"
        0            | 0             | 0x50          | 0x50           | 0xA0                        | 0           | false | true  | false | true  | "With negative overflow"
        0            | 0             | 0x50          | 0xD0           | 0x20                        | 0           | false | false | true  | false | "With carried result"
    }
    
    @Unroll("AND (Immediate) #Expected:  #firstValue & #secondValue = #expectedAccumulator in Accumulator.")
    testAND(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue, AND_I, secondValue)
    
        and:
        processor.step(2)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | false  | false | "Unchanged accumulator"
        0b00000001 | 0b00000010  | 0b00000000          | true   | false | "No matching bits"
        0b00000011 | 0b00000010  | 0b00000010          | false  | false | "1 matched bit, 1 unmatched"
        0b00101010 | 0b00011010  | 0b00001010          | false  | false | "Multiple matched/unmatched bits"
    }
    
    @Unroll("AND (Zero Page) #Expected: #firstValue & #secondValue = #expectedAccumulator in Accumulator.")
    testAND_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_Z, 0x20,
                                                LDA_I, secondValue,
                                                AND_Z, 0x20)

        and:
        processor.step(4)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | false  | false | "Unchanged accumulator"
        0b00000001 | 0b00000010  | 0b00000000          | true   | false | "No matching bits"
        0b00000011 | 0b00000010  | 0b00000010          | false  | false | "1 matched bit, 1 unmatched"
        0b00101010 | 0b00011010  | 0b00001010          | false  | false | "Multiple matched/unmatched bits"
    }
    
    @Unroll("AND (Zero Page[X]) #Expected: #firstValue & #secondValue = #expectedAccumulator")
    testAND_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_Z_IX, 0x20,
                                                LDA_I, secondValue,
                                                AND_Z_IX, 0x20)

        and:
        processor.step(5)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Unchanged accumulator"
        0b00000001 | 1     | 0b00000010  | 0b00000000          | true   | false | "No matching bits"
        0b00000011 | 2     | 0b00000010  | 0b00000010          | false  | false | "1 matched bit, 1 unmatched"
        0b00101010 | 3     | 0b00011010  | 0b00001010          | false  | false | "Multiple matched/unmatched bits"
    }
    
    @Unroll("AND (Absolute) #Expected:  #firstValue & #secondValue = #expectedAccumulator in Accumulator.")
    testAND_A(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_ABS, 0x20, 0x01,
                                                LDA_I, secondValue,
                                                AND_ABS, 0x20, 0x01)

        and:
        processor.step(4)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | false  | false | "Unchanged accumulator"
        0b00000001 | 0b00000010  | 0b00000000          | true   | false | "No matching bits"
        0b00000011 | 0b00000010  | 0b00000010          | false  | false | "1 matched bit, 1 unmatched"
        0b00101010 | 0b00011010  | 0b00001010          | false  | false | "Multiple matched/unmatched bits"
    }
    
    @Unroll("AND (Absolute[X]) #Expected: #firstValue & #secondValue = #expectedAccumulator")
    testAND_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_ABS_IX, locationHi, locationLo,
                                                LDA_I, secondValue,
                                                AND_ABS_IX, locationHi, locationLo)

        and:
        processor.step(5)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        locationHi | locationLo | firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0x1        | 0x10       | 0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Unchanged accumulator"
        0x2        | 0x20       | 0b00000001 | 1     | 0b00000010  | 0b00000000          | true   | false | "No matching bits"
        0x3        | 0x30       | 0b00000011 | 2     | 0b00000010  | 0b00000010          | false  | false | "1 matched bit, 1 unmatched"
        0x4        | 0x40       | 0b00101010 | 3     | 0b00011010  | 0b00001010          | false  | false | "Multiple matched/unmatched bits"
    }
    
    @Unroll("AND (Absolute[Y]) #Expected: #firstValue & #secondValue = #expectedAccumulator")
    testAND_ABS_IY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDA_I, firstValue,
                                                STA_ABS_IY, locationHi, locationLo,
                                                LDA_I, secondValue,
                                                AND_ABS_IY, locationHi, locationLo)
        
        and:
        processor.step(5)
        
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        locationHi | locationLo | firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0x1        | 0x10       | 0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Unchanged accumulator"
        0x2        | 0x20       | 0b00000001 | 1     | 0b00000010  | 0b00000000          | true   | false | "No matching bits"
        0x3        | 0x30       | 0b00000011 | 2     | 0b00000010  | 0b00000010          | false  | false | "1 matched bit, 1 unmatched"
        0x4        | 0x40       | 0b00101010 | 3     | 0b00011010  | 0b00001010          | false  | false | "Multiple matched/unmatched bits"
    }
    
    @Unroll("AND (Indirect, X) #Expected: #firstValue (@[#locationHi|#locationLo]) & #secondValue = #expectedAccumulator")
    testAND_IND_IX() {
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,    //Value at indirect address
                                                STA_ABS, locationHi, locationLo,
                                                LDX_I, index,
                                                LDA_I, locationHi,   //Indirect address in memory
                                                STA_Z_IX, 0x30,
                                                LDA_I, locationLo,
                                                STA_Z_IX, 0x31,
                                                LDA_I, secondValue,
                                                AND_IND_IX, 0x30)

        and:
        processor.step(9)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        locationHi | locationLo | firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0x1        | 0x10       | 0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Unchanged accumulator"
        0x2        | 0x20       | 0b00000001 | 1     | 0b00000010  | 0b00000000          | true   | false | "No matching bits"
        0x3        | 0x30       | 0b00000011 | 2     | 0b00000010  | 0b00000010          | false  | false | "1 matched bit, 1 unmatched"
        0x4        | 0x40       | 0b00101010 | 3     | 0b00011010  | 0b00001010          | false  | false | "Multiple matched/unmatched bits"
    }
    
    @Unroll("AND (Indirect, Y) #Expected: #firstValue & #secondValue -> #expectedAccumulator")
    testAND_IND_IY() {
        when:
        loadMemoryWithProgram(LDY_I, index,           //Index to use
                              LDA_I, firstValue,      //High order byte at pointer
                              STA_ABS_IY, pointerHi, pointerLo,
                              LDA_I, pointerHi,       //Pointer location
                              STA_Z, 0x60,
                              LDA_I, pointerLo,       //Pointer location
                              STA_Z, 0x61,
                              LDA_I, secondValue,
                              AND_IND_IY, 0x60)
    
        and:
        processor.step(9)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
    
        where:
    
        pointerHi | pointerLo | firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0x1       | 0x10      | 0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Unchanged accumulator"
        0x2       | 0x20      | 0b00000001 | 1     | 0b00000010  | 0b00000000          | true   | false | "No matching bits"
        0x3       | 0x30      | 0b00000011 | 2     | 0b00000010  | 0b00000010          | false  | false | "1 matched bit, 1 unmatched"
        0x4       | 0x40      | 0b00101010 | 3     | 0b00011010  | 0b00001010          | false  | false | "Multiple matched/unmatched bits"
    }
    
    @Unroll("ORA (Immediate) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    testOR(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue, ORA_I, secondValue)
        
        and:
        processor.step(2)
        
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0b00000000 | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0b00000001 | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }
    
    @Unroll("ORA (Zero Page) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    testOR_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_Z, 0x20,
                                                LDA_I, secondValue,
                                                ORA_Z, 0x20)
        
        and:
        processor.step(4)
        
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0b00000000 | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0b00000001 | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }
    
    @Unroll("ORA (Zero Page[X)) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    testOR_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_Z_IX, 0x20,
                                                LDA_I, secondValue,
                                                ORA_Z_IX, 0x20)
        
        and:
        processor.step(5)
        
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0b00000000 | 1     | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0b00000001 | 2     | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0b00000001 | 3     | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 4     | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }
    
    @Unroll("ORA (Absolute) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    testOR_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_ABS, 0x20, 0x05,
                                                LDA_I, secondValue,
                                                ORA_ABS, 0x20, 0x05)
        
        and:
        processor.step(4)
        
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0b00000000 | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0b00000001 | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }
    
    @Unroll("ORA (Absolute[X]) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    testOR_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_ABS_IX, 0x20, 0x05,
                                                LDA_I, secondValue,
                                                ORA_ABS_IX, 0x20, 0x05)
        
        and:
        processor.step(5)
        
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0b00000000 | 1     | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0b00000001 | 2     | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0b00000001 | 3     | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 4     | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }
    
    @Unroll("ORA (Absolute[Y]) #Expected:  #firstValue | #secondValue = #expectedAccumulator in Accumulator.")
    testOR_ABS_IY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDA_I, firstValue,
                                                STA_ABS_IY, 0x20, 0x05,
                                                LDA_I, secondValue,
                                                ORA_ABS_IY, 0x20, 0x05)
        
        and:
        processor.step(5)
        
    
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0b00000000 | 1     | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0b00000001 | 2     | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0b00000001 | 3     | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0b00000001 | 4     | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }
    
    @Unroll("ORA (Indirect, X) #Expected: #firstValue (@[#locationHi|#locationLo]) | #secondValue = #expectedAccumulator")
    testOR_IND_IX() {
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,    //Value at indirect address
                                                STA_ABS, locationHi, locationLo,
                                                LDX_I, index,
                                                LDA_I, locationHi,  //Indirect address in memory
                                                STA_Z_IX, 0x30,
                                                LDA_I, locationLo,
                                                STA_Z_IX, 0x31,
                                                LDA_I, secondValue,
                                                ORA_IND_IX, 0x30)
        
        and:
        processor.step(9)
        
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        locationHi | locationLo | firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0x1        | 0x10       | 0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0x2        | 0x20       | 0b00000000 | 1     | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0x3        | 0x30       | 0b00000001 | 2     | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0x4        | 0x40       | 0b00000001 | 3     | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0x5        | 0x50       | 0b00000001 | 4     | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }
    
    @Unroll("ORA (Indirect, Y) #Expected: #firstValue | #secondValue -> #expectedAccumulator")
    testORA_IND_IY() {
        when:
        loadMemoryWithProgram(LDY_I, index,           //Index to use
                              LDA_I, firstValue,      //High order byte at pointer
                              STA_ABS_IY, pointerHi, pointerLo,
                              LDA_I, pointerHi,       //Pointer location
                              STA_Z, 0x60,
                              LDA_I, pointerLo,       //Pointer location
                              STA_Z, 0x61,
                              LDA_I, secondValue,
                              ORA_IND_IY, 0x60)
        
    
        and:
        processor.step(9)
        
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
    
        where:
        pointerHi | pointerLo | firstValue | index | secondValue | expectedAccumulator | Z      | N     | Expected
        0x1       | 0x10      | 0b00000001 | 0     | 0b00000001  | 0b00000001          | false  | false | "Duplicate bits"
        0x2       | 0x20      | 0b00000000 | 1     | 0b00000001  | 0b00000001          | false  | false | "One bit in Accumulator"
        0x3       | 0x30      | 0b00000001 | 2     | 0b00000000  | 0b00000001          | false  | false | "One bit from passed value"
        0x4       | 0x40      | 0b00000001 | 3     | 0b00000010  | 0b00000011          | false  | false | "One bit fro Accumulator, one from new value"
        0x5       | 0x50      | 0b00000001 | 4     | 0b10000010  | 0b10000011          | false  | true  | "Negative result"
    }
    
    @Unroll("EOR (Immediate) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    testEOR(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue, EOR_I, secondValue)

        and:
        processor.step(2)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }
    
    @Unroll("EOR (Zero Page) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    testEOR_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_Z, 0x20,
                                                LDA_I, firstValue,
                                                EOR_Z, 0x20)

        and:
        processor.step(4)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }
    
    @Unroll("EOR (Zero Page[X]) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    testEOR_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, secondValue,
                                                STA_Z_IX, 0x20,
                                                LDA_I, firstValue,
                                                EOR_Z_IX, 0x20)

        and:
        processor.step(5)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        index | firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0     | 0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        1     | 0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        2     | 0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }
    
    @Unroll("EOR (Absolute) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    testEOR_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_ABS, 0x20, 0x04,
                                                LDA_I, firstValue,
                                                EOR_ABS, 0x20, 0x04)

        and:
        processor.step(4)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }
    
    @Unroll("EOR (Absolute[X]) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    testEOR_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, secondValue,
                                                STA_ABS_IX, 0x20, 0x04,
                                                LDA_I, firstValue,
                                                EOR_ABS_IX, 0x20, 0x04)

        and:
        processor.step(5)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        index | firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0     | 0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        1     | 0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        2     | 0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }
    
    @Unroll("EOR (Absolute[Y]) #Expected:  #firstValue ^ #secondValue = #expectedAccumulator in Accumulator.")
    testEOR_ABS_IY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDA_I, secondValue,
                                                STA_ABS_IY, 0x20, 0x04,
                                                LDA_I, firstValue,
                                                EOR_ABS_IY, 0x20, 0x04)

        and:
        processor.step(5)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        index | firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0     | 0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        1     | 0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        2     | 0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }
    
    @Unroll("EOR (Indirect, X) #Expected: #firstValue (@[#locationHi|#locationLo]) EOR #secondValue = #expectedAccumulator")
    testEOR_IND_IX() {
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,      //Value at indirect address
                                                STA_ABS, locationHi, locationLo,
                                                LDX_I, index,
                                                LDA_I, locationHi,      //Indirect address in memory
                                                STA_Z_IX, 0x30,
                                                LDA_I, locationLo,
                                                STA_Z_IX, 0x31,
                                                LDA_I, secondValue,
                                                EOR_IND_IX, 0x30)

        and:
        processor.step(9)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        locationHi | locationLo | index | firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0x1        | 0x10       | 0     | 0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        0x2        | 0x20       | 1     | 0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        0x3        | 0x34       | 2     | 0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }
    
    @Unroll("EOR (Indirect, Y) #Expected: #firstValue ^ #secondValue -> #expectedAccumulator")
    testEOR_IND_IY() {
        when:
        loadMemoryWithProgram(LDY_I, index,           //Index to use
                              LDA_I, firstValue,      //High order byte at pointer
                              STA_ABS_IY, pointerHi, pointerLo,
                              LDA_I, pointerHi,       //Pointer location
                              STA_Z, 0x60,
                              LDA_I, pointerLo,       //Pointer location
                              STA_Z, 0x61,
                              LDA_I, secondValue,
                              EOR_IND_IY, 0x60)
        
    
        and:
        processor.step(9)
        
    
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
    
        where:
        pointerHi | pointerLo | index | firstValue | secondValue | expectedAccumulator | Z      | N     | Expected
        0x1       | 0x10      | 0     | 0b00000001 | 0b00000000  | 0b00000001          | false  | false | "One"
        0x2       | 0x20      | 1     | 0b00000000 | 0b00000001  | 0b00000001          | false  | false | "The other"
        0x3       | 0x34      | 2     | 0b00000001 | 0b00000001  | 0b00000000          | true   | false | "Not both"
    }
    
    @Unroll("STA (Indirect, X) #Expected: #value stored at [#locationHi|#locationLo]")
    testSTA_IND_IX() {
        when:
        loadMemoryWithProgram(LDX_I, index,
                              LDA_I, locationHi,      //Indirect address in memory
                              STA_Z_IX, 0x30,
                              LDA_I, locationLo,
                              STA_Z_IX, 0x31,
                              LDA_I, value,           //Value to store
                              STA_IND_IX, 0x30)
        
    
        and:
        processor.step(7)
    
        then: 'The value has been stored at the expected address'
        memory.getByte( (locationHi << 8) | locationLo ) == value
    
        where:
        locationHi | locationLo | value | index | Expected
        0x01       | 0x10       | 1     | 0     | "Standard store accumulator"
        0x01       | 0x10       | 1     | 1     | "Store using index 1"
        0x01       | 0x10       | 30    | 2     | "Store random value"
        0x01       | 0x11       | 45    | 3     | "Store at different low order location"
        0x04       | 0x11       | 12    | 4     | "Store at different high order location"
    }
    
    @Unroll("STA (Indirect, Y) #Expected: #value stored at [#locationHi|#locationLo]")
    testSTA_IND_IY() {
        when:
        loadMemoryWithProgram(LDA_I, locationHi,      //Indirect address in memory
                              STA_Z, 0x30,
                              LDA_I, locationLo,
                              STA_Z, 0x31,
                              LDA_I, value,           //Value to store
                              LDY_I, index,
                              STA_IND_IY, 0x30)       // (Z[0x30] = two byte address) + Y -> pointer
        
    
        and:
        processor.step(7)

        then: 'The value has been stored at the expected address'
        int address = (locationHi << 8) | locationLo
        int yIndex = registers.getRegister(Registers.REG_Y_INDEX)
        memory.getByte( address + yIndex )  == value
    
        where:
        locationHi | locationLo | value | index | Expected
        0x01       | 0x10       | 1     | 0     | "Standard store accumulator"
        0x01       | 0x10       | 1     | 1     | "Store using index 1"
        0x01       | 0x10       | 30    | 2     | "Store random value"
        0x01       | 0x11       | 45    | 3     | "Store at different low order location"
        0x04       | 0x11       | 12    | 4     | "Store at different high order location"
    }
    
    @Unroll("SBC (Immediate) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    testSBC(){
        when:
        Program program = loadMemoryWithProgram(SEC, LDA_I, firstValue, SBC_I, secondValue)

        and:
        processor.step(3)

        then:
        registers.getRegister(Registers.REG_ACCUMULATOR) == expectedAccumulator
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << sbcTestData()
    }
    
    @Unroll("SBC (Zero Page) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    testSBC_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_Z, 0x20,
                                                LDA_I, firstValue,
                                                SEC,
                                                SBC_Z, 0x20)

        and:
        processor.step(5)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << sbcTestData()
    }
    
    @Unroll("SBC (Zero Page[X]) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    testSBC_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, secondValue,
                                                STA_Z_IX, 0x20,
                                                LDA_I, firstValue,
                                                SEC,
                                                SBC_Z_IX, 0x20)

        and:
        processor.step(6)
    
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withIndex(sbcTestData())
    }
    
    @Unroll("SBC (Absolute) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    testSBC_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_ABS, 0x02, 0x20,
                                                LDA_I, firstValue,
                                                SEC,
                                                SBC_ABS, 0x02, 0x20)

        and:
        processor.step(5)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << sbcTestData()
    }
    
    @Unroll("SBC (Absolute[X]) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    testSBC_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, secondValue,
                                                STA_ABS_IX, 0x02, 0x20,
                                                LDA_I, firstValue,
                                                SEC,
                                                SBC_ABS_IX, 0x02, 0x20)

        and:
        processor.step(6)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withIndex(sbcTestData())
    }
    
    @Unroll("SBC (Absolute[Y]) #Expected:  #firstValue - #secondValue = #expectedAccumulator in Accumulator.")
    testSBC_ABS_IY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDA_I, secondValue,
                                                STA_ABS_IY, 0x02, 0x20,
                                                LDA_I, firstValue,
                                                SEC,
                                                SBC_ABS_IY, 0x02, 0x20)

        and:
        processor.step(6)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withIndex(sbcTestData())
    }
    
    @Unroll("SBC (Indirect, X) #Expected: #firstValue (@[#locationHi|#locationLo]) - #secondValue = #expectedAccumulator")
    testSBC_IND_IX() {
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,    //Value at indirect address
                                                STA_ABS, pointerHi, pointerLo,
                                                LDX_I, index,
                                                LDA_I, pointerHi,                      //Indirect address in memory
                                                STA_Z_IX, 0x30,
                                                LDA_I, pointerLo,
                                                STA_Z_IX, 0x31,
                                                LDA_I, firstValue,
                                                SEC,
                                                SBC_IND_IX, 0x30)

        and:
        processor.step(10)

        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
		testFlags(Z,N,C,V)
    
        where:
        [pointerHi, pointerLo, index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withWordPointer(withIndex(sbcTestData()))
    }
    
    @Unroll("SBC (Indirect, Y) #Expected: #firstValue (@[#locationHi|#locationLo]) - #secondValue = #expectedAccumulator")
    testSBC_IND_IY() {
        when:
        loadMemoryWithProgram(LDY_I, index,           //Index to use
                              LDA_I, secondValue,     //High order byte at pointer
                              STA_ABS_IY, pointerHi, pointerLo,
                              LDA_I, pointerHi,       //Pointer location
                              STA_Z, 0x60,
                              LDA_I, pointerLo,       //Pointer location
                              STA_Z, 0x61,
                              LDA_I, firstValue,
                              SEC,
                              SBC_IND_IY, 0x60)
        
    
        and:
        processor.step(10)
        
        then:
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C,V)
    
        where:
        [pointerHi, pointerLo, index, firstValue, secondValue, expectedAccumulator, Z, N, C, V, Expected] << withWordPointer(withIndex(sbcTestData()))
    }
    
    @Unroll("INX #Expected: on #firstValue = #expectedX")
    testINX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, firstValue, INX)
        
        and:
        processor.step(2)
    
        then:
        expectedX == registers.getRegister(Registers.REG_X_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | expectedX | Z      | N     | Expected
        0          | 1         | false  | false | "Simple increment"
        0xFE       | 0xFF      | false  | true  | "Increment to negative value"
        0b11111111 | 0x0       | true   | false | "Increment to zero"
    }
    
    @Unroll("INC (Zero Page) #Expected: on #firstValue = #expectedMem")
    testINC_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_Z, 0x20,
                                                INC_Z, 0x20)
        
        and:
        processor.step(3)
        
        then:
        expectedMem == memory.getByte(0x20)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | expectedMem | Z      | N     | Expected
        0          | 1           | false  | false | "Simple increment"
        0xFE       | 0xFF        | false  | true  | "Increment to negative value"
        0b11111111 | 0x0         | true   | false | "Increment to zero"
    }
    
    @Unroll("INC (Zero Page[X]) #Expected: on #firstValue = #expectedMem")
    testINC_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_Z_IX, 0x20,
                                                INC_Z_IX, 0x20)

        and:
        processor.step(4)

        then:
        expectedMem == memory.getByte(0x20 + index)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | index | expectedMem | Z      | N     | Expected
        0          | 0     | 1           | false  | false | "Simple increment"
        0xFE       | 0     | 0xFF        | false  | true  | "Increment to negative value"
        0b11111111 | 0     | 0x0         | true   | false | "Increment to zero"
    }
    
    @Unroll("INC (Absolute) #Expected: on #firstValue = #expectedMem")
    testINC_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_ABS, 0x01, 0x20,
                                                INC_ABS, 0x01, 0x20)

        and:
        processor.step(3)

        then:
        expectedMem == memory.getByte(0x0120)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | expectedMem | Z      | N     | Expected
        0          | 1           | false  | false | "Simple increment"
        0xFE       | 0xFF        | false  | true  | "Increment to negative value"
        0b11111111 | 0x0         | true   | false | "Increment to zero"
    }
    
    @Unroll("INC (Absolute, X) #Expected: at 0x120[#index] on #firstValue = #expectedMem")
    testINC_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_ABS_IX, 0x01, 0x20,
                                                INC_ABS_IX, 0x01, 0x20)
        
        and:
        processor.step(4)
        
        then:
        expectedMem == memory.getByte(0x0120 + index)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | index | expectedMem | Z      | N     | Expected
        0          | 1     | 1           | false  | false | "Simple increment"
        0xFE       | 2     | 0xFF        | false  | true  | "Increment to negative value"
        0b11111111 | 3     | 0x0         | true   | false | "Increment to zero"
    }
    
    @Unroll("DEC (Zero Page) #Expected: on #firstValue = #expectedMem")
    testDEC_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_Z, 0x20,
                                                DEC_Z, 0x20)
        
        and:
        processor.step(3)
        
        then:
        expectedMem == memory.getByte(0x20)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | expectedMem | Z      | N     | Expected
        9          | 8           | false  | false | "Simple decrement"
        0xFF       | 0xFE        | false  | true  | "Decrement to negative value"
        0b00000001 | 0x0         | true   | false | "Decrement to zero"
    }
    
    @Unroll("DEC (Zero Page[X]) #Expected: on #firstValue at #loc[#index) = #expectedMem")
    testDEC_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_Z_IX, loc,
                                                DEC_Z_IX, loc)
        
        and:
        processor.step(4)
        
        then:
        expectedMem == memory.getByte(loc + index)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | loc  | index | expectedMem | Z      | N     | Expected
        9          | 0x20 | 0     | 8           | false  | false | "Simple decrement"
        0xFF       | 0x20 | 1     | 0xFE        | false  | true  | "Decrement to negative value"
        0b00000001 | 0x20 | 2     | 0x0         | true   | false | "Decrement to zero"
    }
    
    @Unroll("DEC (Absolute) #Expected: on #firstValue = #expectedMem")
    testDEC_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_ABS, 0x01, 0x20,
                                                DEC_ABS, 0x01, 0x20)
        
        and:
        processor.step(3)
        
        then:
        expectedMem == memory.getByte(0x0120)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | expectedMem | Z      | N     | Expected
        9          | 8           | false  | false | "Simple decrement"
        0xFF       | 0xFE        | false  | true  | "Decrement to negative value"
        0b00000001 | 0x0         | true   | false | "Decrement to zero"
    }
    
    @Unroll("DEC (Absolute[X]) #Expected: on #firstValue = #expectedMem")
    testDEC_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_ABS_IX, 0x01, 0x20,
                                                DEC_ABS_IX, 0x01, 0x20)
        
        and:
        processor.step(4)
        
        then:
        expectedMem == memory.getByte(0x0120 + index)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        index | firstValue | expectedMem | Z      | N     | Expected
        0     | 9          | 8           | false  | false | "Simple decrement"
        1     | 0xFF       | 0xFE        | false  | true  | "Decrement to negative value"
        2     | 0b00000001 | 0x0         | true   | false | "Decrement to zero"
    }
    
    @Unroll("DEX #Expected: on #firstValue = #expectedX")
    testDEX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, firstValue, DEX)
        
        and:
        processor.step(2)
        
        then:
        expectedX == registers.getRegister(Registers.REG_X_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | expectedX | Z      | N     | Expected
        5          | 4         | false  | false | "Simple increment"
        0          | 0xFF      | false  | true  | "Decrement to negative value"
        1          | 0x0       | true   | false | "Increment to zero"
    }
    
    @Unroll("INY #Expected: on #firstValue = #expectedX")
    testINY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, firstValue, INY)
        
        and:
        processor.step(2)
        
        then:
        expectedX == registers.getRegister(Registers.REG_Y_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | expectedX | Z      | N     | Expected
        0          | 1         | false  | false | "Simple increment"
        0xFE       | 0xFF      | false  | true  | "Increment to negative value"
        0b11111111 | 0x0       | true   | false | "Increment to zero"
    }
    
    @Unroll("DEY #Expected: on #firstValue = #expectedY")
    testDEY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, firstValue, DEY)
        
        and:
        processor.step(2)
        
        then:
        expectedY == registers.getRegister(Registers.REG_Y_INDEX)
        program.length == registers.getPC()
		testFlags(Z,N)
    
        where:
        firstValue | expectedY | Z      | N     | Expected
        5          | 4         | false  | false | "Simple increment"
        0          | 0xFF      | false  | true  | "Decrement to negative value"
        1          | 0x0       | true   | false | "Increment to zero"
    }
    
    @Unroll("PLA #Expected: #firstValue from stack at (#expectedSP - 1)")
    testPLA(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                PHA,
                                                LDA_I, 0x11,
                                                PLA)

        and:
        processor.step(2)
        assert(registers.getRegister(Registers.REG_SP) == 0xFE)
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        expectedSP == registers.getRegister(Registers.REG_SP)
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        stackItem == memory.getByte(0x01FF)
    
        where:
        firstValue | expectedSP | stackItem  | expectedAccumulator  | Z     | N     | Expected
        0x99       | 0x0FF      | 0x99       | 0x99                 | false | false | "Basic stack push"
        0x0        | 0x0FF      | 0x0        | 0x0                  | false | true  | "Zero stack push"
        0b10001111 | 0x0FF      | 0b10001111 | 0b10001111           | true  | true  | "Negative stack push"
    }
    
    @Unroll("ASL (Accumulator) #Expected: #firstValue becomes #expectedAccumulator")
    testASL(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, value, ASL_A)
        
        and:
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        [value, expectedAccumulator, Z, N, C, Expected] << aslTestData()
    }
    
    @Unroll("ASL (ZeroPage) #Expected: #firstValue becomes #expectedMem")
    testASL_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_Z, 0x20,
                                                ASL_Z, 0x20)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x20)
		testFlags(Z,N,C)
    
        where:
        [firstValue, expectedMem, Z, N, C, Expected] << aslTestData()
    }
    
    @Unroll("ASL (Absolute) #Expected: #firstValue becomes #expectedMem")
    testASL_A(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_ABS, 0x01, 0x20,
                                                ASL_ABS, 0x01, 0x20)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x120)
		testFlags(Z,N,C)
    
        where:
        [firstValue, expectedMem, Z, N, C, Expected] << aslTestData()
    }
    
    @Unroll("ASL (Zero Page[X]) #Expected: #firstValue (@ 0x20[#index]) becomes #expectedMem")
    testASL_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                LDX_I, index,
                                                STA_Z_IX, 0x20,
                                                ASL_Z_IX, 0x20)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x20 + index)
		testFlags(Z,N,C)
    
        where:
        [index, firstValue, expectedMem, Z, N, C, Expected] << withIndex(aslTestData())
    }
    
    @Unroll("ASL (Absolute[X]) #Expected: #firstValue (@ 0x20[#index]) becomes #expectedMem")
    testASL_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                LDX_I, index,
                                                STA_ABS_IX, 0x01, 0x20,
                                                ASL_ABS_IX, 0x01, 0x20)
        
        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x120 + index)
		testFlags(Z,N,C)
    
        where:
        [index, firstValue, expectedMem, Z, N, C, Expected] << withIndex(aslTestData())
    }
    
    @Unroll("LSR (Accumulator) #Expected: #firstValue becomes #expectedAccumulator")
    testLSR(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue, LSR_A)

        and:
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        firstValue | expectedAccumulator | Z     | N     | C     | Expected
        0b01000000 | 0b00100000          | false | false | false | "Basic shift"
        0b00000000 | 0b00000000          | true  | false | false | "Shift to zero"
        0b00000011 | 0b00000001          | false | false | true  | "Shift with carry"
        0b00000001 | 0b00000000          | true  | false | true  | "Shift to zero with carry"
    }
    
    @Unroll("LSR (Zero Page) #Expected: #firstValue becomes #expectedMem")
    testLSR_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_Z, 0x20,
                                                LSR_Z, 0x20)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x20)
		testFlags(Z,N,C)
    
        where:
        firstValue | expectedMem | Z     | N     | C     | Expected
        0b01000000 | 0b00100000  | false | false | false | "Basic shift"
        0b00000000 | 0b00000000  | true  | false | false | "Shift to zero"
        0b00000011 | 0b00000001  | false | false | true  | "Shift with carry"
        0b00000001 | 0b00000000  | true  | false | true  | "Shift to zero with carry"
    }
    
    @Unroll("LSR (Zero Page[X]) #Expected: #firstValue becomes #expectedMem")
    testLSR_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_Z_IX, 0x20,
                                                LSR_Z_IX, 0x20)
        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x20 + index)
		testFlags(Z,N,C)
    
        where:
        firstValue | index | expectedMem | Z     | N     | C     | Expected
        0b01000000 | 0     | 0b00100000  | false | false | false | "Basic shift"
        0b00000000 | 1     | 0b00000000  | true  | false | false | "Shift to zero"
        0b00000011 | 2     | 0b00000001  | false | false | true  | "Shift with carry"
        0b00000001 | 3     | 0b00000000  | true  | false | true  | "Shift to zero with carry"
    }
    
    @Unroll("LSR (Absolute) #Expected: #firstValue becomes #expectedMem")
    testLSR_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_ABS, 0x02, 0x20,
                                                LSR_ABS, 0x02, 0x20)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x220)
		testFlags(Z,N,C)
    
        where:
        firstValue | expectedMem | Z     | N     | C     | Expected
        0b01000000 | 0b00100000  | false | false | false | "Basic shift"
        0b00000000 | 0b00000000  | true  | false | false | "Shift to zero"
        0b00000011 | 0b00000001  | false | false | true  | "Shift with carry"
        0b00000001 | 0b00000000  | true  | false | true  | "Shift to zero with carry"
    }
    
    @Unroll("LSR (Absolute[X]) #Expected: #firstValue becomes #expectedMem")
    testLSR_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, firstValue,
                                                STA_ABS_IX, 0x02, 0x20,
                                                LSR_ABS_IX, 0x02, 0x20)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x220 + index)
		testFlags(Z,N,C)
    
        where:
        firstValue | index | expectedMem | Z     | N     | C     | Expected
        0b01000000 | 0     | 0b00100000  | false | false | false | "Basic shift"
        0b00000000 | 1     | 0b00000000  | true  | false | false | "Shift to zero"
        0b00000011 | 2     | 0b00000001  | false | false | true  | "Shift with carry"
        0b00000001 | 3     | 0b00000000  | true  | false | true  | "Shift to zero with carry"
    }
    
    @Unroll("JMP #expected: [#jmpLocationHi | #jmpLocationLow] -> #expectedPC")
    testJMP(){
        when:
        loadMemoryWithProgram(NOP, NOP, NOP, JMP_ABS, jmpLocationHi, jmpLocationLow, NOP, NOP, NOP)
        
        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        jmpLocationHi | jmpLocationLow | instructions | expectedPC | expected
        0b00000000    | 0b00000001     | 4            | 1          | "Standard jump back"
        0b00000000    | 0b00000000     | 5            | 1          | "Standard jump back then step"
        0b00000000    | 0b00000111     | 4            | 7          | "Standard jump forward"
        0b00000000    | 0b00000111     | 5            | 8          | "Standard jump forward then step"
        0b00000001    | 0b00000000     | 4            | 256        | "High byte jump"
        0b00000001    | 0b00000001     | 4            | 257        | "Double byte jump"
    }
    
    @Unroll("JMP (Indirect) #expected: [#jmpLocationHi | #jmpLocationLow] -> #expectedPC")
    testIndirectJMP(){
        when:
        loadMemoryWithProgram(  LDA_I, jmpLocationHi,
                                STA_ABS, 0x01, 0x40,
                                LDA_I, jmpLocationLow,
                                STA_ABS, 0x01, 0x41,
                                NOP,
                                NOP,
                                NOP,
                                JMP_IND, 0x01, 0x40,
                                NOP,
                                NOP,
                                NOP)

        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        jmpLocationHi | jmpLocationLow | instructions | expectedPC | expected
        0b00000000    | 0b00000001     | 8            | 1          | "Standard jump back"
        0b00000000    | 0b00000000     | 9            | 2          | "Standard jump back then step"
        0b00000000    | 0b00000111     | 8            | 7          | "Standard jump forward"
        0b00000000    | 0b00000111     | 9            | 10         | "Standard jump forward then step"
        0b00000001    | 0b00000000     | 8            | 256        | "High byte jump"
        0b00000001    | 0b00000001     | 8            | 257        | "Double byte jump"
    }
    
    @Unroll("BCC #expected: ending up at mem[#expectedPC) after #instructions steps")
    testBCC(){
        when:
        loadMemoryWithProgram(NOP, NOP, NOP, preInstr, BCC, jmpSteps, NOP, NOP, NOP, NOP)

        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        preInstr | jmpSteps   | instructions | expectedPC | expected
        SEC      | 4          | 5            | 0x6        | "No jump"
        CLC      | 4          | 5            | 0xA        | "Basic forward jump"
        CLC      | 1          | 6            | 0x8        | "Basic forward jump and step"
        CLC      | 0b11111011 | 5            | 0x2        | "Basic backward jump"
        CLC      | 0b11111011 | 6            | 0x3        | "Basic backward jump and step"
    }
    
    @Unroll("BCS #expected: ending up at mem[#expectedPC) after #instructions steps")
    testBCS(){
        when:
        loadMemoryWithProgram(NOP, NOP, NOP, preInstr, BCS, jmpSteps, NOP, NOP, NOP, NOP)

        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        preInstr | jmpSteps   | instructions | expectedPC | expected
        CLC      | 4          | 5            | 0x6        | "No jump"
        SEC      | 4          | 5            | 0xA        | "Basic forward jump"
        SEC      | 1          | 6            | 0x8        | "Basic forward jump and step"
        SEC      | 0b11111011 | 5            | 0x2        | "Basic backward jump"
        SEC      | 0b11111011 | 6            | 0x3        | "Basic backward jump and step"
    }
    
    @Unroll("ROL (Accumulator) #expected: #firstValue -> #expectedAccumulator")
    testROL_A(){
        when:
        Program program = loadMemoryWithProgram(preInstr, LDA_I, firstValue, ROL_A)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        [preInstr, firstValue, expectedAccumulator, Z, N, C, expected] << rolTestData()
    }
    
    @Unroll("ROL (Zero Page) #Expected: #firstValue -> #expectedMem")
    testROL_Z(){
        when:
        Program program = loadMemoryWithProgram(firstInstr,
                                                LDA_I, firstValue,
                                                STA_Z, 0x20,
                                                ROL_Z, 0x20)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x20)
		testFlags(Z,N,C)
    
        where:
        [firstInstr, firstValue, expectedMem, Z, N, C, Expected] << rolTestData()
    }
    
    @Unroll("ROL (Zero Page[X]) #Expected: #firstValue -> #expectedMem")
    testROL_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                firstInstr,
                                                LDA_I, firstValue,
                                                STA_Z_IX, 0x20,
                                                ROL_Z_IX, 0x20)

        and:
        processor.step(5)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte(0x20 + index)
		testFlags(Z,N,C)
    
        where:
        [index, firstInstr, firstValue, expectedMem, Z, N, C, Expected] << withIndex(rolTestData())
    }
    
    @Unroll("ROL (Absolute) #Expected: #firstValue -> #expectedMem")
    testROL_ABS(){
        when:
        Program program = loadMemoryWithProgram(firstInstr,
                                                LDA_I, firstValue,
                                                STA_ABS, 0x20, 0x07,
                                                ROL_ABS, 0x20, 0x07)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte( 0x2007 )
		testFlags(Z,N,C)
    
        where:
        [firstInstr, firstValue, expectedMem, Z, N, C, Expected] << rolTestData()
    }
    
    @Unroll("ROL (Absolute[X]) #Expected: #firstValue -> #expectedMem")
    testROL_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                firstInstr,
                                                LDA_I, firstValue,
                                                STA_ABS_IX, 0x20, 0x07,
                                                ROL_ABS_IX, 0x20, 0x07)

        and:
        processor.step(5)
    
        then:
        program.length == registers.getPC()
        expectedMem == memory.getByte( 0x2007 + index)
		testFlags(Z,N,C)
    
        where:
        [index, firstInstr, firstValue, expectedMem, Z, N, C, Expected] << withIndex(rolTestData())
    }
    
    @Unroll("ROR (Accumulator) #expected: #firstValue -> #expectedAccumulator")
    testROR_A(){
        when:
        Program program = loadMemoryWithProgram(preInstr,
                                                LDA_I, firstValue,
                                                ROR_A)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        preInstr | firstValue | expectedAccumulator | Z     | N     | C     | expected
        CLC      | 0b00000010 | 0b00000001          | false | false | false | "Standard rotate right"
        CLC      | 0b00000001 | 0b00000000          | true  | false | true  | "Rotate to zero"
        SEC      | 0b00000000 | 0b10000000          | false | true  | false | "Rotate to (carry in) negative"
        CLC      | 0b00000011 | 0b00000001          | false | false | true  | "Rotate to carry out"
        SEC      | 0b00000010 | 0b10000001          | false | true  | false | "Rotate with carry in, no carry out"
        SEC      | 0b00000001 | 0b10000000          | false | true  | true  | "Carry in then carry out"
        SEC      | 0b01111110 | 0b10111111          | false | true  | false | "Carry in to negative"
    }
    
    @Unroll("BNE #expected: With accumulator set to #accumulatorValue, we end up at mem[#expectedPC] after #instructions steps")
    testBNE(){
        when:
        loadMemoryWithProgram(  NOP,
                                NOP,
                                NOP,
                                LDA_I, accumulatorValue,
                                BNE, jumpSteps,
                                NOP,
                                NOP,
                                NOP)

        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        accumulatorValue | jumpSteps  | instructions | expectedPC | expected
        1                | 4          | 5            | 0xB        | "Standard forward jump"
        1                | 0b11111011 | 5            | 0x3        | "Standard backward jump"
        0                | 0b11111011 | 5            | 0x7        | "No jump"
    }
    
    @Unroll("BEQ #expected: With accumulator set to #accumulatorValue, we end up at mem[#expectedPC] after #instructions steps")
    testBEQ(){
        when:
        loadMemoryWithProgram(NOP, NOP, NOP,
                             LDA_I, accumulatorValue,
                             BEQ, jumpSteps,
                             NOP, NOP, NOP)

        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        accumulatorValue | jumpSteps  | instructions | expectedPC | expected
        0                | 4          | 5            | 0xB        | "Standard forward jump"
        0                | 0b11111011 | 5            | 0x3        | "Standard backward jump"
        1                | 0b11111011 | 5            | 0x7        | "No jump"
    }
    
    @Unroll("BMI #expected: With accumulator set to #accumulatorValue, we end up at mem[#expectedPC] after #instructions steps")
    testBMI(){
        when:
        loadMemoryWithProgram(NOP,
                              NOP,
                              NOP,
                              LDA_I, accumulatorValue,
                              BMI, jumpSteps,
                              NOP,
                              NOP,
                              NOP)

        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        accumulatorValue | jumpSteps  | instructions | expectedPC | expected
        0b11111110       | 4          | 5            | 0xB        | "Standard forward jump"
        0b11111100       | 0b11111011 | 5            | 0x3        | "Standard backward jump"
        0b00000001       | 0b11111011 | 5            | 0x7        | "No jump"
    }
    
    @Unroll("BPL #expected: With accumulator set to #accumulatorValue, we end up at mem[#expectedPC] after #instructions steps")
    testBPL(){
        when:
        loadMemoryWithProgram(  NOP,
                                NOP,
                                NOP,
                                LDA_I, accumulatorValue,
                                BPL, jumpSteps,
                                NOP,
                                NOP,
                                NOP)

        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        accumulatorValue | jumpSteps  | instructions | expectedPC | expected
        0b00000001       | 4          | 5            | 0xB        | "Standard forward jump"
        0b01001001       | 0b11111011 | 5            | 0x3        | "Standard backward jump"
        0b11111110       | 0b11111011 | 5            | 0x7        | "No jump"
    }
    
    @Unroll("BVC #expected: #accumulatorValue + #addedValue, checking overflow we end up at mem[#expectedPC] after #instructions steps")
    testBVC(){
        when:
        loadMemoryWithProgram(  NOP,
                                NOP,
                                NOP,
                                LDA_I, accumulatorValue,
                                ADC_I, addedValue,
                                BVC, jumpSteps,
                                NOP,
                                NOP,
                                NOP)

        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        accumulatorValue | addedValue | jumpSteps  | instructions | expectedPC | expected
        0                | 0          | 4          | 6            | 0xD        | "Standard forward jump"
        0                | 0          | 0b11111011 | 6            | 0x5        | "Standard backward jump"
        0x50             | 0x50       | 0b11111011 | 6            | 0x9        | "No jump"
    }
    
    @Unroll("BVC #expected: #accumulatorValue + #addedValue, checking overflow we end up at mem[#expectedPC] after #instructions steps")
    testBVS(){
        when:
        loadMemoryWithProgram(  NOP,
                                NOP,
                                NOP,
                                LDA_I, accumulatorValue,
                                ADC_I, addedValue,
                                BVS, jumpSteps,
                                NOP,
                                NOP,
                                NOP)

        and:
        processor.step(instructions)
    
        then:
        expectedPC == registers.getPC()
    
        where:
        accumulatorValue | addedValue | jumpSteps  | instructions | expectedPC | expected
        0x50             | 0x50       | 4          | 6            | 0xD        | "Standard forward jump"
        0x50             | 0x50       | 0b11111011 | 6            | 0x5        | "Standard backward jump"
        0                | 0          | 0b11111011 | 6            | 0x9        | "No jump"
    }
    
    @Unroll("TAX #expected: #loadedValue to X")
    testTAX(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, loadedValue, TAX)

        and:
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        X == registers.getRegister(Registers.REG_X_INDEX)
		testFlags(Z,N)
    
        where:
        loadedValue | expectedAccumulator | X          | N     | Z     | expected
        0x10        | 0x10                | 0x10       | false | false | "Basic transfer"
        0x0         | 0x0                 | 0x0        | false | true  | "Zero transferred"
        0b11111110  | 0b11111110          | 0b11111110 | true  | false | "Negative transferred"
    }
    
    @Unroll("TAY #expected: #loadedValue to Y")
    testTAY(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, loadedValue, TAY)

        and:
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        Y == registers.getRegister(Registers.REG_Y_INDEX)
		testFlags(Z,N)
    
        where:
        loadedValue | expectedAccumulator | Y          | N      | Z     | expected
        0x10        | 0x10                | 0x10       | false  | false | "Basic transfer"
        0x0         | 0x0                 | 0x0        | false  | true  | "Zero transferred"
        0b11111110  | 0b11111110          | 0b11111110 | true   | false | "Negative transferred"
    }
    
    @Unroll("TYA #expected: #loadedValue to Accumulator")
    testTYA(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, loadedValue, TYA)

        and:
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        Y == registers.getRegister(Registers.REG_Y_INDEX)
		testFlags(Z,N)
    
        where:
        loadedValue | expectedAccumulator | Y          | N      | Z     | expected
        0x10        | 0x10                | 0x10       | false  | false | "Basic transfer"
        0x0         | 0x0                 | 0x0        | false  | true  | "Zero transferred"
        0b11111110  | 0b11111110          | 0b11111110 | true   | false | "Negative transferred"
    }
    
    @Unroll("TXA #expected: #loadedValue to Accumulator")
    testTXA(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, loadedValue, TXA)

        and:
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        expectedAccumulator == registers.getRegister(Registers.REG_ACCUMULATOR)
        X == registers.getRegister(Registers.REG_X_INDEX)
		testFlags(Z,N)
    
        where:
        loadedValue | expectedAccumulator | X          | N      | Z     | expected
        0x10        | 0x10                | 0x10       | false  | false | "Basic transfer"
        0x0         | 0x0                 | 0x0        | false  | true  | "Zero transferred"
        0b11111110  | 0b11111110          | 0b11111110 | true   | false | "Negative transferred"
    }
    
    @Unroll("TSX #expected: load #SPValue in SP into X")
    testTSX(){
        when:
        Program program = loadMemoryWithProgram(TSX)

        and:
        registers.setRegister(Registers.REG_SP, SPValue)
        processor.step()
    
        then:
        program.length == registers.getPC()
        X == registers.getRegister(Registers.REG_X_INDEX)
        expectedSP == registers.getRegister(Registers.REG_SP)
		testFlags(Z,N)
    
        where:
        SPValue | expectedSP | X    | Z     | N     | expected
        0xFF    | 0xFF       | 0xFF | false | true  | "Empty stack"
        0x0F    | 0x0F       | 0x0F | false | false | "No flags set"
        0x0     | 0x0        | 0x0  | true  | false | "Zero stack"
    }
    
    @Unroll("BIT (Zero Page) #expected: #firstValue and #secondValue")
    testBIT(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_Z, memLoc,
                                                LDA_I, secondValue,
                                                BIT_Z, memLoc)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
		testFlags(Z,N, registers.getFlag(Registers.C), O)
    
        where:
        firstValue | secondValue | memLoc | O     | Z     | N     | expected
        0x01       | 0x01        | 0x20   | false | true  | false | "Equal values"
        0x01       | 0x12        | 0x20   | false | false | false | "Unequal values"
        0b10000000 | 0b00000000  | 0x20   | false | false | true  | "Negative flag on"
        0b01000000 | 0b00000000  | 0x20   | true  | false | false | "Overflow flag on"
        0b11000000 | 0b00000000  | 0x20   | true  | false | true  | "Negative & Overflow flag on"
    }
    
    @Unroll("BIT (Absolute) #expected: #firstValue and #secondValue")
    testBIT_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue,
                                                STA_ABS, memLocHi, memLocLo,
                                                LDA_I, secondValue,
                                                BIT_ABS, memLocHi, memLocLo)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
		testFlags(Z,N, registers.getFlag(Registers.C), O)
    
        where:
        firstValue | secondValue | memLocHi | memLocLo | O     | Z     | N     | expected
        0x01       | 0x01        | 1        | 0x20     | false | true  | false | "Equal values"
        0x01       | 0x12        | 2        | 0x20     | false | false | false | "Unequal values"
        0b10000000 | 0b00000000  | 3        | 0x20     | false | false | true  | "Negative flag on"
        0b01000000 | 0b00000000  | 4        | 0x20     | true  | false | false | "Overflow flag on"
        0b11000000 | 0b00000000  | 5        | 0x20     | true  | false | true  | "Negative & Overflow flag on"
    }
    
    @Unroll("STA (Zero Page[X]) #expected: Store #value at #location[#index]")
    testSTA_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, value,
                                                LDX_I, index,
                                                STA_Z_IX, location)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        value == memory.getByte(location+index)
    
        where:
        location | index | value | expected
        0x20     | 0     | 0x0F  | "Store with 0 index"
        0x20     | 1     | 0x0E  | "Store at index 1"
        0x20     | 2     | 0x0D  | "Store at index 2"
        0x20     | 3     | 0x0C  | "Store at index 3"
    }
    
    @Unroll("STA (Absolute) #expected: Store #value at [#locationHi|#locationLo]")
    testSTA_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, value,
                                                STA_ABS, locationHi, locationLo)

        and:
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        value == memory.getByte((locationHi << 8 | locationLo))
    
        where:
        locationHi | locationLo | value | expected
        0x20       |  0         | 0x0F  | "Store with 0 low byte"
        0x40       |  30        | 0x0E  | "Store at non zero low byte"
    }
    
    @Unroll("STA (Absolute[X]) #expected: Store #value at [#locationHi|#locationLo@#index]")
    testSTA_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, value,
                                                LDX_I, index,
                                                STA_ABS_IX, locationHi, locationLo)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        value == memory.getByte((locationHi << 8 | locationLo) + index)
    
        where:
        locationHi | locationLo | index | value | expected
        0x20       |  0         | 0     | 0x0F  | "Store with 0 index"
        0x20       |  30        | 1     | 0x0E  | "Store at index 1"
        0x20       |  9         | 2     | 0x0D  | "Store at index 2"
        0x20       |  1         | 3     | 0x0C  | "Store at index 3"
    }
    
    @Unroll("STA (Absolute[Y]) #expected: Store #value at [#locationHi|#locationLo@#index]")
    testSTA_ABS_IY(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, value,
                                                LDY_I, index,
                                                STA_ABS_IY, locationHi, locationLo)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        value == memory.getByte((locationHi << 8 | locationLo) + index)
    
        where:
        locationHi | locationLo | index | value | expected
        0x20       |  0         | 0     | 0x0F  | "Store with 0 index"
        0x20       |  30        | 1     | 0x0E  | "Store at index 1"
        0x20       |  9         | 2     | 0x0D  | "Store at index 2"
        0x20       |  1         | 3     | 0x0C  | "Store at index 3"
    }
    
    @Unroll("STY (Zero Page[X] #expected: Store #firstValue at #memLocation[#index]")
    testSTY_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index, LDY_I, firstValue, STY_Z_IX, memLocation)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        expectedValue == memory.getByte(memLocation + index)
    
        where:
        firstValue | index | memLocation | expectedValue | expected
        0x0F       | 0     | 0x20        | 0x0F          | "Standard copy to memory"
        0x0F       | 1     | 0x20        | 0x0F          | "Copy to memory with index"
    
    }
    
    @Unroll("CMP (Immediate) #Expected: #firstValue == #secondValue")
    testCMP_I(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, firstValue, CMP_I, secondValue)

        and: 'We set C to make sure we KNOW its been changed'
        !C ? registers.setFlag(Registers.C) : registers.clearFlag(Registers.C)

        and:
        processor.step(2)

        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        [firstValue, secondValue, Z, N, C, Expected] << cmpTestData()
    }
    
    @Unroll("CMP (Zero Page) #Expected: #firstValue == #secondValue")
    testCMP_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_Z, 0x20,
                                                LDA_I, firstValue,
                                                CMP_Z, 0x20)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        [firstValue, secondValue, Z, N, C, Expected] << cmpTestData()
    }
    
    @Unroll("CMP (Zero Page[X]) #Expected: #firstValue == #secondValue")
    testCMP_Z_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, secondValue,
                                                STA_Z_IX, 0x20,
                                                LDA_I, firstValue,
                                                CMP_Z_IX, 0x20)

        and:
        processor.step(5)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        [index, firstValue, secondValue, Z, N, C, Expected] << withIndex(cmpTestData())
    }
    
    @Unroll("CMP (Absolute) #Expected: #firstValue == #secondValue")
    testCMP_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_ABS, 0x01, 0x20,
                                                LDA_I, firstValue,
                                                CMP_ABS, 0x01, 0x20)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        [firstValue, secondValue, Z, N, C, Expected] << cmpTestData()
    }
    
    @Unroll("CMP (Absolute[X]) #Expected: #firstValue == #secondValue")
    testCMP_ABS_IX(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, index,
                                                LDA_I, secondValue,
                                                STA_ABS_IX, memHi, memLo,
                                                LDA_I, firstValue,
                                                CMP_ABS_IX, memHi, memLo)

        and:
        processor.step(5)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        [memHi, memLo, index, firstValue, secondValue, Z, N, C, Expected] << withWordPointer(withIndex(cmpTestData()))
    }
    
    @Unroll("CMP (Absolute[Y]) #Expected: #firstValue == #secondValue")
    testCMP_ABS_IY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDA_I, secondValue,
                                                STA_ABS_IY, memHi, memLo,
                                                LDA_I, firstValue,
                                                CMP_ABS_IY, memHi, memLo)

        and:
        processor.step(5)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_ACCUMULATOR)
		testFlags(Z,N,C)
    
        where:
        [memHi, memLo, index, firstValue, secondValue, Z, N, C, Expected] << withWordPointer(withIndex(cmpTestData()))
    }
    
    @Unroll("CMP (Indirect, X). #Expected: #firstValue == #secondValue")
    testCMP_IND_IX() {
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,    //Value at indirect address
                                                STA_ABS, pointerHi, pointerLo,
                                                LDX_I, index,
                                                LDA_I, pointerHi,  //Indirect address in memory
                                                STA_Z_IX, 0x30,
                                                LDA_I, pointerLo,
                                                STA_Z_IX, 0x31,
                                                LDA_I, firstValue,
                                                CMP_IND_IX, 0x30)

        processor.step(9)

        then:
        firstValue == registers.getRegister(Registers.REG_ACCUMULATOR)
        program.length == registers.getPC()
        testFlags(Z,N,C)
    
        where:
        [pointerHi, pointerLo, index, firstValue, secondValue, Z, N, C, Expected] << withWordPointer(withIndex(cmpTestData()))
    }

    @Unroll("CMP (Indirect, Y) #Expected: #firstValue == #secondValue")
    testCMP_IND_IY() {
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDA_I, secondValue,      //High order byte at pointer
                                                STA_ABS_IY, pointerHi, pointerLo,
                                                LDA_I, pointerHi,       //Pointer location
                                                STA_Z, pointerHiMem,
                                                LDA_I, pointerLo,       //Pointer location
                                                STA_Z, pointerLoMem,
                                                LDA_I, firstValue,
                                                CMP_IND_IY, pointerHiMem )

        processor.step(9)
        
    
        then:
        program.length == registers.getPC()
        Z == registers.getFlag(Registers.Z)
        N == registers.getFlag(Registers.N)
        C == registers.getFlag(Registers.C)
    
        where:
        pointerHiMem | pointerLoMem | pointerHi | pointerLo | firstValue | secondValue | index | Z     | N     | C     | Expected
        0x60         | 0x61         | 0x02      | 0x20      | 0x10       | 0x10        | 0     | true  | false | true  | "Basic compare"
        0x80         | 0x81         | 0x02      | 0x22      | 0x11       | 0x10        | 1     | false | false | true  | "Carry flag set"
        0x55         | 0x56         | 0x03      | 0x35      | 0x10       | 0x11        | 2     | false | true  | false | "Smaller value - larger"
        0xF0         | 0xF1         | 0x04      | 0x41      | 0xFF       | 0x01        | 3     | false | true  | true  | "Negative result"
    }
    
    @Unroll("CPY (Immediate) #Expected: #firstValue == #secondValue")
    testCPY_I(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, firstValue, CPY_I, secondValue)

        and:
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_Y_INDEX)
		testFlags(Z,N,C)
    
        where:
        [firstValue, secondValue, Z, N, C, Expected] << cmpTestData()
    }
    
    @Unroll("CPY (Zero Page) #Expected: #firstValue == #secondValue")
    testCPY_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_Z, 0x20,
                                                LDY_I, firstValue,
                                                CPY_Z, 0x20)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_Y_INDEX)
		testFlags(Z,N,C)
    
        where:
        [firstValue, secondValue, Z, N, C, Expected] << cmpTestData()
    }
    
    @Unroll("CPY (Absolute) #Expected: #firstValue == #secondValue")
    testCPY_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_ABS, 0x01, 0x20,
                                                LDY_I, firstValue,
                                                CPY_ABS, 0x01, 0x20)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_Y_INDEX)
		testFlags(Z,N,C)
    
        where:
        [firstValue, secondValue, Z, N, C, Expected] << cmpTestData()
    }
    
    @Unroll("CPX (Zero Page) #Expected: #firstValue == #secondValue")
    testCPX_Z(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_Z, 0x20,
                                                LDX_I, firstValue,
                                                CPX_Z, 0x20)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_X_INDEX)
		testFlags(Z,N,C)
    
        where:
        [firstValue, secondValue, Z, N, C, Expected] << cmpTestData()
    }
    
    @Unroll("CPX (Absolute) #Expected: #firstValue == #secondValue")
    testCPX_ABS(){
        when:
        Program program = loadMemoryWithProgram(LDA_I, secondValue,
                                                STA_ABS, 0x01, 0x20,
                                                LDX_I, firstValue,
                                                CPX_ABS, 0x01, 0x20)

        and:
        processor.step(4)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_X_INDEX)
		testFlags(Z,N,C)
    
        where:
        [firstValue, secondValue, Z, N, C, Expected] << cmpTestData()
    }
    
    @Unroll("CPX (Immediate) #Expected: #firstValue == #secondValue")
    testCPX_I(){
        when:
        Program program = loadMemoryWithProgram(LDX_I, firstValue, CPX_I, secondValue)

        and:
        processor.step(2)
    
        then:
        program.length == registers.getPC()
        firstValue == registers.getRegister(Registers.REG_X_INDEX)
		testFlags(Z,N,C)
    
        where:
        [firstValue, secondValue, Z, N, C, Expected] << cmpTestData()
    }
    
    @Unroll("STX (Zero Page[X] #expected: #firstValue -> #location[#index]")
    STX_Z_IY(){
        when:
        Program program = loadMemoryWithProgram(LDY_I, index,
                                                LDX_I, firstValue,
                                                STX_Z_IY, location)

        and:
        processor.step(3)
    
        then:
        program.length == registers.getPC()
        firstValue == memory.getByte(location + index)
    
        where:
        location | index | firstValue | expected
        0x20     | 0     | 0xA1       | "Basic store"
        0x20     | 1     | 0xA3       | "Store at index"
        0x60     | 2     | 0xA6       | "Store at higher location at index"
        0x20     | 50    | 0xAA       | "Store at higher index"
    }
    
    @Unroll("RTS #expected")
    testRTS(){
        when:
        loadMemoryWithProgram(  LDA_I, memHi,
                                PHA,
                                LDA_I, memLo,
                                PHA,
                                RTS)

        and:
        processor.step(5)
    
        then:
        expectedPC == registers.getPC()
        expectedSP == registers.getRegister(Registers.REG_SP)
    
        where:
        memHi | memLo | expectedPC | expectedSP | expected
        0x1   | 0x0   | 0x100      | 0xFF       | "Simple return from subroutine"
    }
    
    @Unroll("BRK #expected")
    testBRK(){
        when: 'vector values for PC are in memory'
        memory.setByteAt(0xFFFE, newPCHi)
        memory.setByteAt(0xFFFF, newPCLo)

        and: 'The status register is set to a value that will be pushed to stack'
        registers.setRegister(Registers.REG_STATUS, statusReg)
    
        and: 'the program is executed'
        loadMemoryWithProgram(BRK)
        processor.step(1)
    
        then: 'registers now contain the expect values'
        registers.getRegister(Registers.REG_PC_HIGH) == newPCHi
        registers.getRegister(Registers.REG_PC_LOW) == newPCLo
        registers.getRegister(Registers.REG_SP) == 0xFC

        and: 'the pushed status register has the break flag set'
        memory.getByte(0x1FD) == (statusReg | Registers.STATUS_FLAG_BREAK)

        and: 'the pushed program counter is correct'
        memory.getByte(0x1FE) == 0x03
        memory.getByte(0x1FF) == 0x00
    
        //XXX Refactor to test when PC overflows to high byte before loading to stack
    
        where:
        newPCHi | newPCLo | statusReg  | expected
        0x0     | 0x0     | 0b00000000 | "With empty status register and B not set"
        0x0     | 0x0     | 0b00100000 | "With empty status register and B already set"
        0x1     | 0x1     | 0b00100101 | "With loaded status register"
    }
    
    @Unroll("IRQ #expected #statusValue->#pushedStatus")
    testIRQ(){
        when:
        loadMemoryWithProgram(LDA_I, 1,
                              LDA_I, 2,
                              LDA_I, 3,
                              LDA_I, 4,
                              LDA_I, 5)
        
    
        and:
        memory.setBlock(0xFFFA, 1)
        memory.setBlock(0xFFFB, 2)

        and:
        processor.step(steps)
        registers.setRegister(Registers.REG_STATUS, statusValue)
        processor.irq()
    
        then: 'Three items have been added to stack'
        registers.getRegister(Registers.REG_SP) == 0xFC
    
        and: 'The PC on the stack is as expected'
        pushedPCLo == memory.getByte(0x1FE)
        pushedPCHi == memory.getByte(0x1FF)
    
        and: 'Status register is moved to stack with B set'
        pushedStatus == memory.getByte(0x1FD)
    
        and: 'The PC is set to 0xFFFE:0xFFFF'
        registers.getRegister(Registers.REG_PC_HIGH) == memory.getByte(0xFFFE)
        registers.getRegister(Registers.REG_PC_LOW)  == memory.getByte(0xFFFF)
    
        where:
        statusValue | steps | pushedStatus | pushedPCHi | pushedPCLo | expected
        0b00000000  | 1     | 0b00000100   | 0x00       | 0x02       | "Empty status register"
        0b11111111  | 1     | 0b11111111   | 0x00       | 0x02       | "Full status register"
        0b10101010  | 1     | 0b10101110   | 0x00       | 0x02       | "Random status register"
        0b00000000  | 2     | 0b00000100   | 0x00       | 0x04       | "Two steps"
        0b00000000  | 3     | 0b00000100   | 0x00       | 0x06       | "Three steps"
    }
    
    @Unroll("NMI #expected #statusValue->#pushedStatus")
    testNMI(){
        when:
        loadMemoryWithProgram(LDA_I, 1,
                              LDA_I, 2,
                              LDA_I, 3,
                              LDA_I, 4,
                              LDA_I, 5)
        
    
        and:
        memory.setBlock(0xFFFA, 1)
        memory.setBlock(0xFFFB, 2)

        and:
        processor.step(steps)
        registers.setRegister(Registers.REG_STATUS, statusValue)
        processor.nmi()
    
        then: 'Three items have been added to stack'
        0xFC == registers.getRegister(Registers.REG_SP)
    
        and: 'The PC on the stack is as expected'
        pushedPCLo == memory.getByte(0x1FE)
        pushedPCHi == memory.getByte(0x1FF)
    
        and: 'Status register is moved to stack with B set'
        pushedStatus == memory.getByte(0x1FD)
    
        and: 'The PC is set to 0xFFFE:0xFFFF'
        registers.getRegister(Registers.REG_PC_HIGH) == memory.getByte(0xFFFA)
        registers.getRegister(Registers.REG_PC_LOW)  == memory.getByte(0xFFFB)
    
        where:
        statusValue | steps | pushedStatus | pushedPCHi | pushedPCLo | expected
        0b00000000  | 1     | 0b00000100   | 0x00       | 0x02       | "Empty status register"
        0b11111111  | 1     | 0b11111111   | 0x00       | 0x02       | "Full status register"
        0b10101010  | 1     | 0b10101110   | 0x00       | 0x02       | "Random status register"
        0b00000000  | 2     | 0b00000100   | 0x00       | 0x04       | "Two steps"
        0b00000000  | 3     | 0b00000100   | 0x00       | 0x06       | "Three steps"
    }
    
    @Unroll("RTI #expected ")
    testRTI(){
        when: 'We have a programText which will be interrupted'
        loadMemoryWithProgram(  LDA_I, 1,
                                LDA_I, 2, //--> IRQ Here
                                LDA_I, 4, //<-- Return here
                                LDA_I, 5,
                                LDA_I, 6)
        
    
        and: 'An interrupt routine'
        Program irqRoutine = new Program().with(LDA_I, 3, RTI)
        memory.setBlock(0x100, irqRoutine.getProgramAsByteArray())
        memory.setByteAt(0xFFFE, 0x01)
        memory.setByteAt(0xFFFF, 0x00)

        and:
        processor.step(2)
        registers.setRegister(Registers.REG_STATUS, statusValue)
        processor.irq()
        processor.step(2)
    
        then: 'Stack is empty again'
        0xFF == registers.getRegister(Registers.REG_SP)
    
        and: 'The PC on the stack is as expected'
        restoredPCHi == registers.getRegister(Registers.REG_PC_HIGH)
        restoredPCLo == registers.getRegister(Registers.REG_PC_LOW)
    
        and: 'Status register is moved to stack with B set'
        restoredStatus == registers.getRegister(Registers.REG_STATUS)
    
        and: 'The PC is set to where it was before IRQ'
        0x00 == registers.getRegister(Registers.REG_PC_HIGH)
        0x04 == registers.getRegister(Registers.REG_PC_LOW)
    
        where:
        statusValue | restoredStatus | restoredPCHi | restoredPCLo | expected
        0b00000000  | 0b00000100     | 0x00         | 0x04         | "Empty status register"
        0b11111111  | 0b11111111     | 0x00         | 0x04         | "Full status register"
        0b10101010  | 0b10101110     | 0x00         | 0x04         | "Random status register"
    }
    
    //    @Ignore
    //    def exampleTest(){
    //        when:
    //        Memory memory = new SimpleMemory(65534)
    //        int[] programText = []
    //        memory.setBlock(0, programText)
    //
    //        and:
    //        
    //
    //        
    //
    //        and:
    //        processor.step(1)
    //
    //        then:
    //        registers.getPC() == programText.length
    //
    //        where:
    //        A | B
    //        A | B
    //    }
    }
