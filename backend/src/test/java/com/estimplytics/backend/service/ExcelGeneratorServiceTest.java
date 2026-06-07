package com.estimplytics.backend.service;

import com.estimplytics.backend.entity.Estimation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelGeneratorServiceTest {

    private final ExcelGeneratorService service = new ExcelGeneratorService();

    @Test
    void exportEstimation_shouldUseGenericJustification_whenFeedbackDiffersAndJustificationMissing() throws Exception {
        Estimation estimation = Estimation.builder()
            .versionNumber(1)
            .fiability(60)
            .hoursPlanning(4)
            .hoursAnalysis(4)
            .hoursDevelopment(12)
            .hoursTesting(4)
            .totalHours(24)
            .actualHoursFeedback(30)
            .build();

        byte[] content = service.exportEstimation(estimation, "REQ-001", 42);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            String justification = workbook.getSheetAt(0).getRow(1).getCell(10).getStringCellValue();
            assertThat(justification).isEqualTo("Horas ajustadas por necesidad");
        }
    }

    @Test
    void exportEstimation_shouldKeepStoredJustification_whenPresent() throws Exception {
        Estimation estimation = Estimation.builder()
            .versionNumber(1)
            .totalHours(24)
            .actualHoursFeedback(30)
            .justification("Desviación por pruebas adicionales")
            .build();

        byte[] content = service.exportEstimation(estimation, "REQ-001", 42);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            String justification = workbook.getSheetAt(0).getRow(1).getCell(10).getStringCellValue();
            assertThat(justification).isEqualTo("Desviación por pruebas adicionales");
        }
    }
}
