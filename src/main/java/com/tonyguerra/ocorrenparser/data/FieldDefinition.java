package com.tonyguerra.ocorrenparser.data;

public final record FieldDefinition(boolean alphanumeric, int length, int position, boolean mandatory,
        String description) {

}
