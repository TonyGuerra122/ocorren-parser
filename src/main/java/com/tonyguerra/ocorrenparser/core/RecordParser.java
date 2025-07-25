package com.tonyguerra.ocorrenparser.core;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonyguerra.ocorrenparser.data.RecordRow;
import com.tonyguerra.ocorrenparser.enums.LayoutType;
import com.tonyguerra.ocorrenparser.errors.IllegalFieldsException;

public final class RecordParser {
    private static final List<String> VERSION_3_1_LINES = List.of("000", "340", "341", "342");
    private static final List<String> VERSION_5_0_LINES = List.of(
            "000",
            "540",
            "541",
            "542",
            "543",
            "544",
            "545",
            "549");

    private final Map<String, List<String>> errorsFieldNCauses;

    private final LayoutMap layout;

    public RecordParser(LayoutMap layout) {
        this.layout = layout;
        this.errorsFieldNCauses = new HashMap<>();
    }

    private void addFieldError(String fieldName, String cause) {
        errorsFieldNCauses.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(cause);
    }

    public List<RecordRow> parseFile(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        try {
            final List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

            errorsFieldNCauses.clear();

            final var parsedLines = IntStream.range(0, lines.size())
                    .mapToObj(i -> parseRow(lines.get(i), i + 1))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!errorsFieldNCauses.isEmpty()) {
                throw new IllegalFieldsException(errorsFieldNCauses);
            }

            return parsedLines;

        } catch (final Exception e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    public String parseFileToJSON(Path filePath) {
        final var records = parseFile(filePath);

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

    private RecordRow parseRow(String row, int lineNumber) {
        if (row.length() < 3) {
            addFieldError("LINHA_" + lineNumber, "Muito curta para conter um tipo de registro");
            return null;
        }

        final String recordType = row.substring(0, 3);
        final var fields = layout.getRecords(recordType);

        if (fields == null) {
            addFieldError("LINHA_" + lineNumber, "Tipo de registro desconhecido: " + recordType);
            return null;
        }

        final var result = new LinkedHashMap<String, String>();

        final int expectedLength = fields.entrySet().stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase("FILLER"))
                .mapToInt(entry -> entry.getValue().position() - 1 + entry.getValue().length())
                .max()
                .orElse(0);

        if (row.length() < expectedLength) {
            addFieldError("LINHA_" + lineNumber, "Muito curta para tipo " + recordType + ": esperado no mínimo "
                    + expectedLength + " caracteres");
            return null;
        }

        for (final var entry : fields.entrySet()) {
            final String fieldName = entry.getKey();
            final var def = entry.getValue();

            if (def.position() <= 0) {
                addFieldError("LINHA_" + lineNumber + " > " + fieldName, "Posição inválida: " + def.position());
                continue;
            }

            final int start = def.position() - 1;
            final int end = Math.min(start + def.length(), row.length());
            final String value = row.substring(start, end).trim();

            if (!value.isEmpty()) {
                if (def.alphanumeric() && !value.matches("[\\p{L}\\p{N}\\s.,;:]+")) {
                    if (!fieldName.equalsIgnoreCase("FILLER") || !value.isBlank()) {
                        addFieldError("LINHA_" + lineNumber + " > " + fieldName,
                                "Deve conter apenas caracteres alfanuméricos");
                        continue;
                    }
                }
                if (!def.alphanumeric() && !value.matches("\\d+")) {
                    addFieldError("LINHA_" + lineNumber + " > " + fieldName, "Deve ser numérico");
                    continue;
                }
            }

            if (def.mandatory() && value.isEmpty()) {
                addFieldError("LINHA_" + lineNumber + " > " + fieldName, "Campo obrigatório está vazio");
                continue;
            }

            result.put(fieldName, value);
        }

        return new RecordRow(recordType, result);
    }

    private static LayoutType detectLayoutType(Path filePath) {
        try {
            final List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

            final Set<String> recordTypes = lines.stream()
                    .filter(line -> line.length() >= 3)
                    .map(line -> line.substring(0, 3))
                    .collect(Collectors.toSet());

            if (recordTypes.containsAll(VERSION_3_1_LINES)) {
                return LayoutType.VERSION_3_1;
            }

            if (recordTypes.containsAll(VERSION_5_0_LINES)) {
                return LayoutType.VERSION_5_0;
            }

            throw new IllegalArgumentException("Layout não reconhecido. Tipos de registro encontrados: " + recordTypes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
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

    public static String parseFromFilePathToJSON(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        final var layoutType = detectLayoutType(filePath);

        final var parser = fromLayoutType(layoutType);

        return parser.parseFileToJSON(filePath);
    }

}
