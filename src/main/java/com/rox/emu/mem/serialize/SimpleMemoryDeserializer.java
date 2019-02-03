package com.rox.emu.mem.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.rox.emu.env.RoxByte;
import com.rox.emu.env.RoxWord;
import com.rox.emu.mem.SimpleMemory;

import java.io.IOException;
import java.util.Map;

public class SimpleMemoryDeserializer extends StdDeserializer<SimpleMemory> {
    public SimpleMemoryDeserializer(){
        this(null);
    }

    protected SimpleMemoryDeserializer(Class<SimpleMemory> vc) {
        super(vc);
    }

    @Override
    public SimpleMemory deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        assert node.get("class").textValue().equals(SimpleMemory.class.getName());

        final IntNode sizeNode = (IntNode)node.get("size");
        final Integer size = sizeNode.asInt();

        final SimpleMemory memory = new SimpleMemory(size);
        final ArrayNode data = (ArrayNode) node.get("data");

        for (int entryIndex=0; entryIndex<data.size(); entryIndex++){
            final JsonNode jsonNode = data.get(entryIndex);
            final Map.Entry<String, JsonNode> entry = jsonNode.fields().next();

            final Integer address = Integer.parseInt(entry.getKey());
            final RoxWord addressWord = RoxWord.fromLiteral(address);

            deserializeByte(jsonParser, memory, entry, addressWord);
        }

        return memory;
    }

    private void deserializeByte(JsonParser jsonParser, SimpleMemory memory, Map.Entry<String, JsonNode> entry, RoxWord addressWord) throws IOException {
        final JsonParser jsonSubParser = entry.getValue().traverse();
        jsonSubParser.setCodec(jsonParser.getCodec());
        final RoxByte memoryByte = jsonSubParser.readValueAs(RoxByte.class);

        memory.setByteAt(addressWord, memoryByte);
    }
}
