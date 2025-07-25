package com.tonyguerra.ocorrenparser.errors;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class IllegalFieldsException extends IllegalArgumentException {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final long serialVersionUID = 1L;

    private final Map<String, List<String>> fieldNCause;

    public IllegalFieldsException(Map<String, List<String>> fieldNCause) {
        super(formatMessage(fieldNCause));
        this.fieldNCause = fieldNCause;
    }

    public IllegalFieldsException(Map<String, List<String>> fieldNCause, Throwable cause) {
        super(formatMessage(fieldNCause), cause);
        this.fieldNCause = fieldNCause;
    }

    public Map<String, List<String>> getFieldNCause() {
        return fieldNCause;
    }

    public String getErrorsAsJson() {
        try {
            return JSON_MAPPER.writeValueAsString(fieldNCause);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private static String formatMessage(Map<String, List<String>> fieldNCause) {
        return "Campos inválidos com as seguintes causas: " +
                fieldNCause.entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + String.join("; ", entry.getValue()))
                        .collect(Collectors.joining(", "));
    }
}
