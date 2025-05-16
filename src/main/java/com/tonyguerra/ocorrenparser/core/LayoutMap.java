package com.tonyguerra.ocorrenparser.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyguerra.ocorrenparser.data.FieldDefinition;

public final class LayoutMap {
    private final Map<String, Map<String, FieldDefinition>> records;

    public LayoutMap() {
        records = new HashMap<>();
    }

    public Map<String, FieldDefinition> getRecords(String recordType) {
        return records.get(recordType);
    }

    public static LayoutMap fromJsonFile(String path) throws IOException {
        final var mapper = new ObjectMapper();
        final var data = mapper.readValue(new File(path),
                new TypeReference<Map<String, Map<String, FieldDefinition>>>() {
                });

        final var map = new LayoutMap();
        map.records.putAll(data);
        return map;
    }

    public static LayoutMap fromJsonStream(InputStream is) {
        try {
            final var mapper = new ObjectMapper();
            final var data = mapper.readValue(is,
                    new TypeReference<Map<String, Map<String, FieldDefinition>>>() {
                    });

            final var map = new LayoutMap();
            map.records.putAll(data);

            return map;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read layout map from stream", ex);
        }
    }
}
