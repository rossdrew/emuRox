package com.rox.emu.processor.mos6502;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.env.serialize.RoxByteDeserializer;
import com.rox.emu.env.serialize.RoxByteSerializer;
import com.rox.emu.env.serialize.RoxWordDeserializer;
import com.rox.emu.env.serialize.RoxWordSerializer;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.mem.serialize.SimpleMemoryDeserializer;
import com.rox.emu.mem.serialize.SimpleMemorySerializer;
import com.rox.emu.processor.mos6502.serialize.Mos6502Serializer;
import com.rox.emu.processor.mos6502.serialize.RegistersDeserializer;
import com.rox.emu.processor.mos6502.serialize.RegistersSerializer;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SerializationTest {
    private ObjectMapper objectMapper;

    @Before
    public void setup(){
        objectMapper = new ObjectMapper();
        SimpleModule customSerializations = new SimpleModule();

        customSerializations.addSerializer(RoxByte.class, new RoxByteSerializer());
        customSerializations.addDeserializer(RoxByte.class, new RoxByteDeserializer());

        customSerializations.addSerializer(RoxWord.class, new RoxWordSerializer());
        customSerializations.addDeserializer(RoxWord.class, new RoxWordDeserializer());

        customSerializations.addSerializer(SimpleMemory.class, new SimpleMemorySerializer());
        customSerializations.addDeserializer(SimpleMemory.class, new SimpleMemoryDeserializer());

        customSerializations.addSerializer(Registers.class, new RegistersSerializer());
        customSerializations.addDeserializer(Registers.class, new RegistersDeserializer());

        customSerializations.addSerializer(Mos6502.class, new Mos6502Serializer());

        objectMapper.registerModule(customSerializations);
    }

    @Test
    public void roxByteTest() throws IOException {
        final RoxByte byteValue = RoxByte.fromLiteral(42);

        final String serializedToJson = objectMapper.writeValueAsString(byteValue);
        final RoxByte deserializeByte = objectMapper.readValue(serializedToJson, RoxByte.class);

        assertEquals("{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":42}", serializedToJson);
        assertEquals(byteValue, deserializeByte);
    }

    @Test
    public void roxWordTest() throws IOException {
        final RoxWord wordValue = RoxWord.fromLiteral(541);

        final String serializedToJson = objectMapper.writeValueAsString(wordValue);
        final RoxWord deserializeByte = objectMapper.readValue(serializedToJson, RoxWord.class);

        assertEquals("{\"class\":\"com.rox.emu.env.RoxWord\",\"value\":541}", serializedToJson);
        assertEquals(wordValue, deserializeByte);
    }

    @Test
    public void simpleMemoryTest() throws IOException {
        final Memory mem = new SimpleMemory();
        mem.setBlock(RoxWord.fromLiteral(10), new RoxByte[] {RoxByte.fromLiteral(1), RoxByte.fromLiteral(2), RoxByte.fromLiteral(3), RoxByte.fromLiteral(4), RoxByte.ZERO, RoxByte.fromLiteral(5)});

        final String serializedToJson = objectMapper.writeValueAsString(mem);
        final SimpleMemory deserializedMemory = objectMapper.readValue(serializedToJson, SimpleMemory.class);

        assertEquals("{\"class\":\"com.rox.emu.mem.SimpleMemory\",\"size\":65536,\"data\":[{\"10\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":1}},{\"11\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":2}},{\"12\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":3}},{\"13\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":4}},{\"15\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":5}}]}", serializedToJson);
        assertEquals(mem, deserializedMemory);
    }

    @Test
    public void registersTest() throws IOException {
        final Registers reg = new Registers();

        final String serializedToJson = objectMapper.writeValueAsString(reg);
        final Registers deserializedRegisters = objectMapper.readValue(serializedToJson, Registers.class);

        assertEquals("{\"class\":\"com.rox.emu.processor.mos6502.Registers\",\"PC\":{\"class\":\"com.rox.emu.env.RoxWord\",\"value\":0},\"A\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"X\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"Y\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"SP Hi\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"SP Lo\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":255},\"S\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0}}", serializedToJson);
        assertEquals(reg, deserializedRegisters);
    }

    @Test
    public void mos6502Test() throws JsonProcessingException {
        final Registers reg = new Registers();
        final Memory mem = new SimpleMemory();
        mem.setBlock(RoxWord.fromLiteral(10), new RoxByte[] {RoxByte.fromLiteral(1), RoxByte.fromLiteral(2), RoxByte.fromLiteral(3), RoxByte.fromLiteral(4), RoxByte.ZERO, RoxByte.fromLiteral(5)});
        final Mos6502 cpu = new Mos6502(mem, reg);

        final String serializedToJson = objectMapper.writeValueAsString(cpu);

        assertEquals("{\"class\":\"com.rox.emu.processor.mos6502.Mos6502\",\"registers\":{\"class\":\"com.rox.emu.processor.mos6502.Registers\",\"PC\":{\"class\":\"com.rox.emu.env.RoxWord\",\"value\":0},\"A\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"X\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"Y\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"SP Hi\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"SP Lo\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":255},\"S\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0}}}", serializedToJson);
    }
}
