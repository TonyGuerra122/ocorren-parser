package com.tonyguerra.ocorrenparser.data;

import java.util.Map;

public final record RecordRow(String recordType, Map<String, String> fields) {

}
