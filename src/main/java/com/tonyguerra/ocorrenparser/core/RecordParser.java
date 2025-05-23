package com.tonyguerra.ocorrenparser.core;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyguerra.ocorrenparser.data.RecordRow;
import com.tonyguerra.ocorrenparser.enums.LayoutType;

public final class RecordParser {
    private final LayoutMap layout;

    public RecordParser(LayoutMap layout) {
        this.layout = layout;
    }

    public List<RecordRow> parseFile(String filePath) {
        if (!Files.exists(Paths.get(filePath))) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        try {
            final List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

            return lines.stream()
                    .filter(line -> !line.trim().isEmpty())
                    .map(this::parseRow)
                    .collect(Collectors.toList());

        } catch (final Exception e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    public String parseFileToJSON(String filePath) {
        final var records = parseFile(filePath);

        // Agrupa os registros por tipo
        final var grouped = new LinkedHashMap<String, List<Map<String, String>>>();

        records.forEach(row -> {
            grouped
                    .computeIfAbsent(row.recordType(), k -> new ArrayList<>())
                    .add(row.fields());
        });

        final var mapper = new ObjectMapper();

        try {
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(grouped);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to convert grouped records to JSON", e);
        }
    }

    private RecordRow parseRow(String row) {
        if (row.length() < 3) {
            throw new IllegalArgumentException("Row too short to contain a record type");
        }

        final String recordType = row.substring(0, 3);
        final var fields = layout.getRecords(recordType);

        if (fields == null) {
            throw new IllegalArgumentException("Unknown record type: " + recordType);
        }

        final var result = new LinkedHashMap<String, String>();

        final int expectedLength = fields.values().stream()
                .mapToInt(f -> f.position() + f.length())
                .max().orElse(0);

        if (row.length() < expectedLength) {
            throw new IllegalArgumentException("Row too short for record type " + recordType + ": expected at least "
                    + expectedLength + " characters");
        }

        for (final var entry : fields.entrySet()) {
            final String fieldName = entry.getKey();
            final var def = entry.getValue();

            if (def.position() <= 0) {
                throw new IllegalArgumentException(
                        "Field '" + fieldName + "' has an invalid position: " + def.position());
            }

            final int start = def.position() - 1;
            final int end = Math.min(start + def.length(), row.length());
            final String value = row.substring(start, end).trim();

            if (!value.isEmpty()) {
                if (def.alphanumeric() && !value.matches("[\\p{L}\\p{N}\\s]+")) {
                    throw new IllegalArgumentException("Field '" + fieldName + "' must be alphanumeric");
                }
                if (!def.alphanumeric() && !value.matches("\\d+")) {
                    throw new IllegalArgumentException("Field '" + fieldName + "' must be numeric");
                }
            }

            if (def.mandatory() && value.isEmpty()) {
                throw new IllegalArgumentException("Mandatory field '" + fieldName + "' is empty");
            }

            result.put(fieldName, value);
        }

        return new RecordRow(recordType, result);
    }

    public static RecordParser fromLayoutType(LayoutType layoutType) {
        try {
            final var inputStream = RecordParser.class.getClassLoader()
                    .getResourceAsStream("layouts/" + layoutType.getFileName());

            if (inputStream == null) {
                throw new FileNotFoundException("Layout file not found: " + layoutType.getFileName());
            }

            final var layout = LayoutMap.fromJsonStream(inputStream);
            return new RecordParser(layout);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to load layout: " + layoutType.getFileName(), e);
        }
    }

}
