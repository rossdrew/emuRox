package com.rox.emu.processor.mos6502;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.env.serialize.RoxByteSerializer;
import com.rox.emu.env.serialize.RoxWordSerializer;
import com.rox.emu.mem.Memory;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.mem.serialize.SimpleMemorySerializer;
import com.rox.emu.processor.mos6502.serialize.Mos6502Serializer;
import com.rox.emu.processor.mos6502.serialize.RegistersSerializer;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class SerializationTest {
    private ObjectMapper objectMapper;

    @Before
    public void setup(){
        objectMapper = new ObjectMapper();
        SimpleModule customSerializations = new SimpleModule();
        customSerializations.addSerializer(RoxByte.class, new RoxByteSerializer());
        customSerializations.addSerializer(RoxWord.class, new RoxWordSerializer());
        customSerializations.addSerializer(SimpleMemory.class, new SimpleMemorySerializer());
        customSerializations.addSerializer(Registers.class, new RegistersSerializer());
        customSerializations.addSerializer(Mos6502.class, new Mos6502Serializer());
        objectMapper.registerModule(customSerializations);
    }

    @Test
    public void roxByteTest() throws JsonProcessingException {
        final RoxByte byteValue = RoxByte.fromLiteral(42);

        final String serializedToJson = objectMapper.writeValueAsString(byteValue);

        assertEquals("{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":42}", serializedToJson);
    }

    @Test
    public void roxWordTest() throws JsonProcessingException {
        final RoxWord wordValue = RoxWord.fromLiteral(541);

        final String serializedToJson = objectMapper.writeValueAsString(wordValue);

        assertEquals("{\"class\":\"com.rox.emu.env.RoxWord\",\"value\":541}", serializedToJson);
    }

    @Test
    public void simpleMemoryTest() throws JsonProcessingException {
        final Memory mem = new SimpleMemory();
        mem.setBlock(RoxWord.fromLiteral(10), new RoxByte[] {RoxByte.fromLiteral(1), RoxByte.fromLiteral(2), RoxByte.fromLiteral(3), RoxByte.fromLiteral(4), RoxByte.ZERO, RoxByte.fromLiteral(5)});

        final String serializedToJson = objectMapper.writeValueAsString(mem);

        assertEquals("{\"class\":\"com.rox.emu.mem.SimpleMemory\",\"data\":[{\"10\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":1}},{\"11\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":2}},{\"12\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":3}},{\"13\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":4}},{\"15\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":5}}]}", serializedToJson);
    }

    @Test
    public void registersTest() throws JsonProcessingException {
        final Registers reg = new Registers();

        final String serializedToJson = objectMapper.writeValueAsString(reg);

        assertEquals("{\"class\":\"com.rox.emu.processor.mos6502.Registers\",\"PC\":{\"class\":\"com.rox.emu.env.RoxWord\",\"value\":0},\"A\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"X\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"Y\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"SP Hi\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"SP Lo\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":255}}", serializedToJson);
    }

    @Test
    public void mos6502Test() throws JsonProcessingException {
        final Registers reg = new Registers();
        final Memory mem = new SimpleMemory();
        mem.setBlock(RoxWord.fromLiteral(10), new RoxByte[] {RoxByte.fromLiteral(1), RoxByte.fromLiteral(2), RoxByte.fromLiteral(3), RoxByte.fromLiteral(4), RoxByte.ZERO, RoxByte.fromLiteral(5)});
        final Mos6502 cpu = new Mos6502(mem, reg);

        final String serializedToJson = objectMapper.writeValueAsString(cpu);

        assertEquals("{\"class\":\"com.rox.emu.processor.mos6502.Mos6502\",\"registers\":{\"class\":\"com.rox.emu.processor.mos6502.Registers\",\"PC\":{\"class\":\"com.rox.emu.env.RoxWord\",\"value\":0},\"A\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"X\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"Y\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"SP Hi\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":0},\"SP Lo\":{\"class\":\"com.rox.emu.env.RoxByte\",\"value\":255}}}", serializedToJson);
    }
}
