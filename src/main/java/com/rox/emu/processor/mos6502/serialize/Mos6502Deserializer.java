package com.rox.emu.processor.mos6502.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.rox.emu.mem.SimpleMemory;
import com.rox.emu.processor.mos6502.Mos6502;
import com.rox.emu.processor.mos6502.Registers;

import java.io.IOException;

public class Mos6502Deserializer extends StdDeserializer<Mos6502> {

    public Mos6502Deserializer(){
        this(null);
    }

    protected Mos6502Deserializer(Class<Mos6502> vc) {
        super(vc);
    }

    @Override
    public Mos6502 deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = parser.getCodec().readTree(parser);
        //assert node.get("class").textValue().equals(Mos6502.class.getName());

        final Registers registers = deserializeRegisters(node, "registers", parser);
        final SimpleMemory memory = deserializeMemory(node, "memory", parser);

        return new Mos6502(memory, registers);
    }

    private Registers deserializeRegisters(final JsonNode node, final String entryName, final JsonParser parser) throws IOException {
        final JsonParser subParser = node.get(entryName).traverse();
        subParser.setCodec(parser.getCodec());
        return subParser.readValueAs(Registers.class);
    }


    private SimpleMemory deserializeMemory(final JsonNode node, final String entryName, final JsonParser parser) throws IOException {
        final JsonParser subParser = node.get(entryName).traverse();
        subParser.setCodec(parser.getCodec());
        return subParser.readValueAs(SimpleMemory.class);
    }
}
