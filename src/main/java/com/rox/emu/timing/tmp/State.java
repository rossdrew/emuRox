package com.rox.emu.timing.tmp;

import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.Memory;
import com.rox.emu.timing.SimpleClock;
import com.rox.emu.timing.SimpleClock.TMP_OpCode;

import java.util.LinkedList;
import java.util.Queue;

public class State {
    private int pc = 0; //Program Counter
    private TMP_OpCode loadedOpCode = null;
    private int loadedOperand1 = 0;
    private int loadedOperand2 = 0;
    private final Memory memory;
    private final int yRegister;
    private final int dataBus;
    private final int addressBus;
    private final Queue<SimpleClock.MicroOperationType> microOperations;

    public State(final int pc,
                 final TMP_OpCode loadedOpCode, //XXX Should we call this the instruction register?
                 final int loadedOperand1,
                 final int loadedOperand2,
                 final Memory memory,
                 final int yRegister,
                 final int addressBus,
                 final int dataBus,
                 final Queue<SimpleClock.MicroOperationType> microOperations
    ) {
        this.pc = pc;
        this.loadedOpCode = loadedOpCode;
        this.loadedOperand1 = loadedOperand1;
        this.loadedOperand2 = loadedOperand2;
        this.memory = memory;
        this.yRegister = yRegister;
        this.addressBus = addressBus;
        this.dataBus = dataBus;
        this.microOperations = new LinkedList<>(microOperations);
    }

    public int getPc() {
        return pc;
    }

    public State withIncrementedPc() {
        return new State(pc+1, loadedOpCode, loadedOperand1, loadedOperand2, memory, yRegister, addressBus, dataBus, microOperations);
    }

    public TMP_OpCode getLoadedOpCode() {
        return loadedOpCode;
    }

    public State withOpcode(final TMP_OpCode opCode) {
        return new State(pc, opCode, loadedOperand1, loadedOperand2, memory, yRegister,  addressBus, dataBus, microOperations);
    }

    public int getLoadedOperand1() {
        return loadedOperand1;
    }

    public State withOperand1(final int operand1) {
        return new State(pc, loadedOpCode, operand1, loadedOperand2, memory, yRegister,  addressBus, dataBus, microOperations);
    }

    public int getLoadedOperand2() {
        return loadedOperand2;
    }

    public State withOperand2(final int operand2) {
        return new State(pc, loadedOpCode, loadedOperand1, loadedOperand2, memory, yRegister,  addressBus, dataBus, microOperations);
    }

    public Memory getMemory() {
        return memory; //XXX Should maybe be a copy?
    }

    public int getYRegister() {
        return yRegister; //XXX Should maybe be a copy?
    }

    public State withYRegister(int yRegister) {
        return new State(pc, loadedOpCode, loadedOperand1, loadedOperand2, memory, yRegister,  addressBus, dataBus, microOperations);
    }

    public State withAccumulator(int loadedOperand1) {
        //TODO Accumulator is not part of this state, but should be
        return new State(pc, loadedOpCode, loadedOperand1, loadedOperand2, memory, yRegister,  addressBus, dataBus, microOperations);
    }

    //BYTE (op1), WORD (op1, op2), PC
    public State addressing(int addressBus) {
        return new State(pc, loadedOpCode, loadedOperand1, loadedOperand2, memory, yRegister,  addressBus, dataBus, microOperations);
    }

    public State withData(int dataBus) {
        System.out.println("Data bus set to " + dataBus);
        return new State(pc, loadedOpCode, loadedOperand1, loadedOperand2, memory, yRegister,  addressBus, dataBus, microOperations);
    }

    public int getData(){
        return dataBus;
    }

    //XXX Is this breaking single responsibility?
    public State postRead() {
        final RoxByte data = memory.getByte(RoxWord.fromLiteral(addressBus));
        return new State(pc, loadedOpCode, loadedOperand1, loadedOperand2, memory, yRegister,  addressBus, data.getRawValue(), microOperations);
    }

    //XXX Is this breaking single responsibility?
    public State postWrite() {
        memory.setByteAt(RoxWord.fromLiteral(addressBus), RoxByte.fromLiteral(dataBus));
        //TODO Should return a new memory instance
        return this;
    }

    /**
     * @return the current byte in memory at the program counter location
     */
    public RoxByte getByteInMemory() {
        return memory.getByte(RoxWord.fromLiteral(pc));
    }

    /**
     * @param location the {@link RoxWord} location in memory to get the byte from
     * @return the byte in memory at the specified location
     */
    public RoxByte getByteInMemory(final RoxWord location) {
        return memory.getByte(location);
    }

    public Queue<SimpleClock.MicroOperationType> getMicroOperations() {
        //XXX Should maybe be a copy?
        return microOperations;
    }

    public State withMicroOperations(Queue<SimpleClock.MicroOperationType> microOperations) {
        return new State(pc, loadedOpCode, loadedOperand1, loadedOperand2, memory, yRegister,  addressBus, dataBus, microOperations);
    }

    public String toString() {
        return "6502 Snapshot {" +
                "pc=" + pc +
                ", loadedOpCode=" + loadedOpCode +
                ", loadedOperand1=" + loadedOperand1 +
                ", loadedOperand2=" + loadedOperand2 +
                ", yRegister=" + yRegister +
                ", addressBus=" + addressBus +
                ", dataBus=" + dataBus +
                ", microOperationsQueued=" + microOperations.size() + "/" + (loadedOpCode != null ? loadedOpCode.getSteps().size() : "-") +
                '}';

    }
}