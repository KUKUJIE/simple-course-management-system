package com.agiantii.backend.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvUtil {
    public static byte[] toCsvBytes(List<String[]> rows, String[] header) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
                // Write BOM for Excel compatibility
                writer.write('\uFEFF');
                if (header != null && header.length > 0) {
                    writeRow(writer, header);
                }
                for (String[] row : rows) {
                    writeRow(writer, row);
                }
                writer.flush();
            }
            return baos.toByteArray();
        }
    }

    private static void writeRow(OutputStreamWriter writer, String[] cols) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            String cell = cols[i] == null ? "" : cols[i];
            // Escape quotes
            if (cell.contains(",") || cell.contains("\n") || cell.contains("\r") || cell.contains("\"") ) {
                cell = cell.replace("\"", "\"\"");
                cell = "\"" + cell + "\"";
            }
            sb.append(cell);
            if (i < cols.length - 1) sb.append(',');
        }
        sb.append('\n');
        writer.write(sb.toString());
    }
}
