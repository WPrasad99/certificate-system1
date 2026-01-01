package com.certificate.util;

import com.certificate.dto.ParticipantDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class FileParserUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public List<ParticipantDTO> parseFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new RuntimeException("Invalid file");
        }

        if (filename.endsWith(".csv")) {
            return parseCSV(file);
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return parseExcel(file);
        } else {
            throw new RuntimeException("Unsupported file format. Please upload CSV or Excel file.");
        }
    }

    private List<ParticipantDTO> parseCSV(MultipartFile file) throws Exception {
        List<ParticipantDTO> participants = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(reader,
                        CSVFormat.DEFAULT.builder()
                                .setHeader()
                                .setSkipHeaderRecord(true)
                                .setIgnoreHeaderCase(true)
                                .setTrim(true)
                                .build())) {

            for (CSVRecord record : csvParser) {
                String name = record.get("Name");
                String email = record.get("Email");

                validateParticipant(name, email);

                ParticipantDTO dto = new ParticipantDTO();
                dto.setName(name);
                dto.setEmail(email);
                participants.add(dto);
            }
        }

        return participants;
    }

    private List<ParticipantDTO> parseExcel(MultipartFile file) throws Exception {
        List<ParticipantDTO> participants = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            boolean isHeader = true;
            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (row.getCell(0) == null || row.getCell(1) == null) {
                    continue;
                }

                String name = row.getCell(0).getStringCellValue();
                String email = row.getCell(1).getStringCellValue();

                validateParticipant(name, email);

                ParticipantDTO dto = new ParticipantDTO();
                dto.setName(name);
                dto.setEmail(email);
                participants.add(dto);
            }
        }

        return participants;
    }

    private void validateParticipant(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }

        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("Invalid email: " + email);
        }
    }
}
