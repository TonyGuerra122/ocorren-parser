package com.tonyguerra.ocorrenparser.enums;

public enum LayoutType {
    LFG_OCORREN("lfg_ocorren.json");

    private final String fileName;

    LayoutType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
