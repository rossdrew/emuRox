package com.rox.emu.processor.mos6502.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.processor.mos6502.Registers;

import java.io.IOException;

public class RegistersDeserializer extends StdDeserializer<Registers> {
    public RegistersDeserializer(){
        this(null);
    }

    protected RegistersDeserializer(Class<Registers> vc) {
        super(vc);
    }

    @Override
    public Registers deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = parser.getCodec().readTree(parser);
        //assert node.get("class").textValue().equals(Registers.class.getName());

        final Registers registers = new Registers();

        final RoxWord pc = deserializeWord(node, "PC", parser);
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_HI, pc.getHighByte());
        registers.setRegister(Registers.Register.PROGRAM_COUNTER_LOW, pc.getLowByte());

        registers.setRegister(Registers.Register.ACCUMULATOR, deserializeByte(node, "A", parser));
        registers.setRegister(Registers.Register.X_INDEX, deserializeByte(node, "X", parser));
        registers.setRegister(Registers.Register.Y_INDEX, deserializeByte(node, "Y", parser));
        registers.setRegister(Registers.Register.STACK_POINTER_HI, deserializeByte(node, "SP Hi", parser));
        registers.setRegister(Registers.Register.STACK_POINTER_LOW, deserializeByte(node, "SP Lo", parser));
        registers.setRegister(Registers.Register.STATUS_FLAGS, deserializeByte(node, "S", parser));

        return registers;
    }

    private RoxByte deserializeByte(final JsonNode node, final String entryName, final JsonParser parser) throws IOException {
        final JsonParser subParser = node.get(entryName).traverse();
        subParser.setCodec(parser.getCodec());
        return subParser.readValueAs(RoxByte.class);
    }

    private RoxWord deserializeWord(final JsonNode node, final String entryName, final JsonParser parser) throws IOException {
        final JsonParser subParser = node.get(entryName).traverse();
        subParser.setCodec(parser.getCodec());
        return subParser.readValueAs(RoxWord.class);
    }
}
