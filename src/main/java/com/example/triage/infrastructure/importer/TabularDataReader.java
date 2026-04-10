package com.example.triage.infrastructure.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class TabularDataReader {

    public List<Map<String, String>> read(MultipartFile file) throws Exception {
        return read(file, null);
    }

    public List<Map<String, String>> read(MultipartFile file, String sheetName) throws Exception {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (filename.endsWith(".xlsx")) {
            return readExcel(file, sheetName);
        }
        return readCsv(file);
    }

    private List<Map<String, String>> readCsv(MultipartFile file) throws Exception {
        try (CSVParser parser = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            List<Map<String, String>> rows = new ArrayList<>();
            parser.forEach(record -> {
                Map<String, String> row = new HashMap<>();
                record.toMap().forEach((k, v) -> row.put(normalizeHeader(k), v == null ? "" : v.trim()));
                rows.add(row);
            });
            return rows;
        }
    }

    private List<Map<String, String>> readExcel(MultipartFile file, String sheetName) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            var sheet = sheetName == null || sheetName.isBlank()
                    ? workbook.getSheetAt(0)
                    : workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }
            Iterator<org.apache.poi.ss.usermodel.Row> iterator = sheet.iterator();
            if (!iterator.hasNext()) {
                return List.of();
            }
            DataFormatter formatter = new DataFormatter();
            org.apache.poi.ss.usermodel.Row headerRow = iterator.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(normalizeHeader(formatter.formatCellValue(cell)));
            }
            List<Map<String, String>> rows = new ArrayList<>();
            while (iterator.hasNext()) {
                org.apache.poi.ss.usermodel.Row rowRef = iterator.next();
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String value = formatter.formatCellValue(rowRef.getCell(i));
                    row.put(headers.get(i), value == null ? "" : value.trim());
                }
                rows.add(row);
            }
            return rows;
        }
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase();
    }
}
