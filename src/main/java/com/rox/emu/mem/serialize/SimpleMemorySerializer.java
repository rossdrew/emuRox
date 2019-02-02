package com.rox.emu.mem.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rox.emu.com.rox.serialize.ClassSerialization;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.SimpleMemory;

import java.io.IOException;

public class SimpleMemorySerializer extends ClassSerialization<SimpleMemory> {
    public void customSerializations(SimpleMemory memory, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeFieldName("data");

        serializeMemoryArray(memory, gen);
    }

    private void serializeMemoryArray(SimpleMemory simpleMemory, JsonGenerator gen) throws IOException {
        final RoxByte[] memory = simpleMemory.getBlock(RoxWord.ZERO, RoxWord.fromLiteral(simpleMemory.getSize()-1));

        gen.writeStartArray();
        //XXX Why is the -1 required here?!?!
        for (int memoryAddress = 0; memoryAddress < simpleMemory.getSize()-1; memoryAddress++){
            if (!RoxByte.ZERO.equals(memory[memoryAddress])){
                gen.writeStartObject();
                gen.writeObjectField("" + memoryAddress, memory[memoryAddress]);
                gen.writeEndObject();
            }
        }
        gen.writeEndArray();
    }
}