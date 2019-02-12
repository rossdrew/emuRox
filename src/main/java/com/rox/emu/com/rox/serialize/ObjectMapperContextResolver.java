package com.rox.emu.com.rox.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.env.serialize.RoxByteDeserializer;
import com.rox.emu.env.serialize.RoxByteSerializer;
import com.rox.emu.env.serialize.RoxWordDeserializer;
import com.rox.emu.env.serialize.RoxWordSerializer;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.mem.serialize.SimpleMemoryDeserializer;
import com.rox.emu.mem.serialize.SimpleMemorySerializer;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.mos6502.Registers;
import com.rox.emu.processor.mos6502.serialize.Mos6502Deserializer;
import com.rox.emu.processor.mos6502.serialize.Mos6502Serializer;
import com.rox.emu.processor.mos6502.serialize.RegistersDeserializer;
import com.rox.emu.processor.mos6502.serialize.RegistersSerializer;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

    private ObjectMapper mapper;

    public ObjectMapperContextResolver() {
        mapper = new ObjectMapper();

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
        customSerializations.addDeserializer(Mos6502.class, new Mos6502Deserializer());

        mapper.registerModule(customSerializations);
    }

    @Override
    public ObjectMapper getContext(Class<?> arg0) {
        return mapper;
    }

}