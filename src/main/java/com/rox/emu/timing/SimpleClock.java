package com.rox.emu.timing;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.processor.mos6502.util.Mos6502Compiler;
import com.rox.emu.processor.mos6502.util.Program;
import com.rox.emu.timing.tmp.MicroOperation;
import com.rox.emu.timing.tmp.State;

import java.util.*;

import static com.rox.emu.timing.SimpleClock.tmp_AddressingMode.*;
import static com.rox.emu.timing.SimpleClock.MicroOperationType.*;
import static com.rox.emu.timing.SimpleClock.tmp_Operation.LDA;


public class SimpleClock {
    private State currentState;

    public SimpleClock(final Program program) {
        final Memory memory = generateProgramMemory(program);
        currentState = new State(256, null, 0, 0, memory, 10, 0,0, new LinkedList<>()); //Start at 256 to skip over any zero page data
    }

    private Memory generateProgramMemory(final Program program){
        final RoxByte[] programAsByteArray = program.getProgramAsByteArray();
        //XXX: Why did this stop working?
        //System.out.println("Program: " + Arrays.stream(programAsByteArray).map(RoxByte::getRawValue).collect(Collectors.toList()));

        //Set up some simple memory with only the program in it
        final SimpleMemory memory = new SimpleMemory(programAsByteArray.length + 256); //+256 to allow for zero page addressing
        /*TEST*/memory.setByteAt(RoxWord.fromLiteral(177), RoxByte.fromLiteral(42));

        memory.setBlock(RoxWord.fromLiteral(256), programAsByteArray);
        return memory;
    }

    private enum MemoryMode { READ, WRITE}

//    private void setAddressBus(final int ADH, final int ADL, final MemoryMode mode) {
//        this.addressBus = 16 * ADH + ADL;
//        if (mode == MemoryMode.WRITE) {
//            System.out.println("Setting address bus to " + addressBus + " for WRITE operation");
//            //TODO you need a source to set the data bus to
//        } else {
//            System.out.println("Setting address bus to " + addressBus + " for READ operation");
//            //TODO you need a destination to write the data bus to
//        }
//    }

    //XXX This name indicates bad design
    private State fetchAndDecode(final State currentState) {
        State tmpState = currentState;
        if (currentState.getMicroOperations().isEmpty()){ //No opcode current being processed
            //Fetch opcode from memory & decode
            final TMP_OpCode nextOpCode = TMP_OpCode.getByValue(currentState.getByteInMemory().getRawValue());
            tmpState = currentState.withOpcode(nextOpCode).withMicroOperations(new LinkedList<>(nextOpCode.getSteps()));
            /*DEBUG*/System.out.println(" [PC:" + currentState.getPc() + "] Loaded opcode: " + tmpState.getLoadedOpCode() + " (Operation: " + tmpState.getLoadedOpCode().operation + ", Addressing Mode: " + tmpState.getLoadedOpCode().addressingMode + ")");
        }

        return tmpState;
    }

    private void clockTick() {
        // Simulate a clock tick
        System.out.println("[TICK]");

        currentState = fetchAndDecode(currentState);
        final MicroOperationType microOp = currentState.getMicroOperations().poll();
        final MicroOperation executableMicroOp = microOp.getOperation();
        /*DEBUG*/System.out.println("Executing '" + microOp + "'");
        currentState = executableMicroOp.execute(currentState);
    }

//TODO should use address bus and data bus instead of operands to store data
    public enum MicroOperationType {
        FETCH_OPCODE((originalState) -> originalState
                .withOpcode(TMP_OpCode.getByValue(originalState.getByteInMemory().getRawValue()))
                .withIncrementedPc()
        ),
        FETCH_OPERAND_1((originalState) -> originalState
                .withOperand1(originalState.getByteInMemory().getRawValue())
                .withIncrementedPc()
        ),
        FETCH_OPERAND_2((originalState) -> originalState
                .withOperand2(originalState.getByteInMemory().getRawValue())
                .withIncrementedPc()
        ),
        //This is equivalence to read zero page address?
        TEST((s) -> s.addressing(s.getLoadedOperand1()).postRead().withIncrementedPc()),
        READ_ZERO_PAGE_ADDRESS((originalState) -> originalState
                //TODO put address (from s.getLoadedOperand1()) on the address bus
                //TODO fetch the operand from the data bus
                //Get the value at zero page provided in s.getLoadedOperand1(),
                .withData(originalState.getMemory().getByte(RoxWord.from(RoxByte.fromLiteral(originalState.getLoadedOperand1()))).getAsInt())
                .withIncrementedPc()),
        ADD_Y_REGISTER((origingalState) -> origingalState
                //Add Y register to operand
                .withData(origingalState.getYRegister() +  origingalState.getLoadedOperand1())
                .withIncrementedPc()
        ),
        LOAD_ACCUMULATOR((s) -> s
                .withAccumulator(s.getLoadedOperand1())
                .withIncrementedPc()
        );

        private final MicroOperation operation;

        public MicroOperation getOperation() {
            return operation;
        }

        MicroOperationType(final MicroOperation o) {
            this.operation = o;
        }
    }

    /**
     * LDA Indexed (Y) Addressing Mode
     * LDA ($00),Y
     *
     * Cycle 1: Fetch opcode
     * Cycle 2: Fetch operand
     * Cycle 3: Read base address
     * Cycle 4: Add Y register
     * Cycle 5: Fetch final value
     */

    int valueAtZeroPAage(final int address) {
        return 10; //Placeholder
    }

    public enum TMP_OpCode {
        LDA_Z(0xA5, LDA, ZERO_PAGE, new MicroOperationType[]{}),
        LDA_I(0xA9, LDA, IMMEDIATE, new MicroOperationType[]{}),
        LDA_A(0xAD, LDA, ABSOLUTE, new MicroOperationType[]{}),
        LDA_Z_IX(0xB5, LDA, ZERO_PAGE_INDEXED_X, new MicroOperationType[]{}),
        LDA_AX(0xBD, LDA, ABSOLUTE_INDEXED_X, new MicroOperationType[]{}),
        LDA_IX(0xA1, LDA, INDIRECT_INDEXED_X, new MicroOperationType[]{}),
        LDA_AY(0xB9, LDA, ABSOLUTE_INDEXED_Y, new MicroOperationType[]{}),
        LDA_IY(0xB1, LDA, INDIRECT_INDEXED_Y, new MicroOperationType[]{
                //Automatic micro operation
                //FETCH_OPCODE,
                //Microoperations from Addressing Mode
//                FETCH_OPERAND_1,
//                READ_ZERO_PAGE_ADDRESS, //XXX Does having no argument here not make it a general case solution?
//                ADD_Y_REGISTER,
                LOAD_ACCUMULATOR
        });

        private final int opcode;
        private final tmp_Operation operation;
        private final tmp_AddressingMode addressingMode;
        private final List<MicroOperationType> opCodeSteps;

        public List<MicroOperationType> getSteps() {
            final List<MicroOperationType> allSteps = new ArrayList<>(addressingMode.getSteps());
            allSteps.addAll(opCodeSteps);
            return Collections.unmodifiableList(allSteps);
        }

        public static TMP_OpCode getByValue(final int opecode_id) throws RuntimeException {
            return Arrays.stream(TMP_OpCode.values())
                    .filter(opCode -> opCode.opcode == opecode_id)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unknown opcode: " + opecode_id)); //TODO Use custom exception
        }

        TMP_OpCode(final int opcode,
                   final tmp_Operation operation,
                   final tmp_AddressingMode addressingMode,
                   final MicroOperationType[] steps) {
            this.opcode = opcode;
            this.operation = operation;
            this.addressingMode = addressingMode;
            this.opCodeSteps = Arrays.asList(steps);
        }

        @Override
        public String toString() {
            return this.name();
        }
    }

    public enum tmp_Operation {
        /**
         *         r.setFlagsBasedOn(v);
         *         r.setRegister(ACCUMULATOR, v);
         */
        LDA  // Load Accumulator
    }

    public enum tmp_AddressingMode {
        IMMEDIATE(new MicroOperationType[]{}),
        ABSOLUTE(new MicroOperationType[]{}),
        ZERO_PAGE(new MicroOperationType[]{}),
        ZERO_PAGE_INDEXED_X(new MicroOperationType[]{}),
        ZERO_PAGE_INDEXED_Y(new MicroOperationType[]{}),
        INDIRECT_INDEXED_X(new MicroOperationType[]{}),
        /** <i>Indexed indirect</i>: Expects a one byte argument and an offset in the X Register added together they
         *  give an address in Zero Page that itself contains a two byte address to be used in the operation */
        INDIRECT_INDEXED_Y(new MicroOperationType[]{
                FETCH_OPERAND_1,
                READ_ZERO_PAGE_ADDRESS, //XXX Does having no argument here not make it a general case solution?
                ADD_Y_REGISTER
        }),
        ABSOLUTE_INDEXED_X(new MicroOperationType[]{}),
        ABSOLUTE_INDEXED_Y(new MicroOperationType[]{});

        private final List<MicroOperationType> steps;

        public List<MicroOperationType> getSteps() {
            return Collections.unmodifiableList(steps);
        }

        tmp_AddressingMode(final MicroOperationType[] steps) {
            this.steps = Arrays.asList(steps);
        }
    }

    public static void main(String[] args) {
        final String code = "LDA ($01),Y";
        System.out.println("Code: \"" + code + "\"");
        final Mos6502Compiler compiler = new Mos6502Compiler(code);
        final Program program = compiler.compileProgram();
        System.out.println("Program compiled successfully: " + program.getLength() + " bytes.");

        final SimpleClock clock = new SimpleClock(program);

        System.out.println(clock.currentState);
        for (int i=0; i<4; i++){
            clock.clockTick();
            System.out.println(clock.currentState);
        }

//        while (i++ < 20) {
//            clock.clockTick();
//            try {
//                /* ~1ms delay (1000Hz - simulating slower clock)
//                 * This isn't ideal as 1ms in Java time is not guaranteed to be exactly 1ms,
//                 */
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                break;
//            }
//        }
    }
}



