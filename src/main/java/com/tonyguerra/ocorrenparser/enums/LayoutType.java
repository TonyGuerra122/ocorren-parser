package com.tonyguerra.ocorrenparser.enums;

public enum LayoutType {
    VERSION_3_1("3_1.json"),
    VERSION_5_0("5_0.json");

    private final String fileName;

    LayoutType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
