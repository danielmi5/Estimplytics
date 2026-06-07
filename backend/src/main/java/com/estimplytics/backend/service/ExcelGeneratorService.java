package com.estimplytics.backend.service;

import com.estimplytics.backend.entity.Estimation;
import com.estimplytics.backend.exception.ReportGenerationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExcelGeneratorService {

    private static final String[] HEADERS = {
        "Código petición origen",
        "Número de versión",
        "Número peticiones basadas",
        "Porcentaje de fiabilidad",
        "Horas estimadas de planificación",
        "Horas estimadas de análisis",
        "Horas estimadas de desarrollo",
        "Horas estimadas de testing",
        "Horas totales",
        "Horas reales (feedback)",
        "Justificación del feedback"
    };

    public byte[] exportEstimation(Estimation estimation, String requestCode, Integer similarRequestsCount) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Estimation");
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int columnIndex = 0; columnIndex < HEADERS.length; columnIndex++) {
                Cell cell = headerRow.createCell(columnIndex);
                cell.setCellValue(HEADERS[columnIndex]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(columnIndex, (HEADERS[columnIndex].length() + 2) * 256);
            }

            Row dataRow = sheet.createRow(1);
            setCellValue(dataRow, 0, requestCode);
            setCellValue(dataRow, 1, estimation.getVersionNumber());
            setCellValue(dataRow, 2, similarRequestsCount);
            setCellValue(dataRow, 3, estimation.getFiability());
            setCellValue(dataRow, 4, estimation.getHoursPlanning());
            setCellValue(dataRow, 5, estimation.getHoursAnalysis());
            setCellValue(dataRow, 6, estimation.getHoursDevelopment());
            setCellValue(dataRow, 7, estimation.getHoursTesting());
            setCellValue(dataRow, 8, estimation.getTotalHours());
            setCellValue(dataRow, 9, estimation.getActualHoursFeedback());
            setCellValue(dataRow, 10, resolveJustification(estimation));

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new ReportGenerationException("Error while generating Excel", exception);
        }
    }

    private String resolveJustification(Estimation estimation) {
        String justification = estimation.getJustification();
        if (justification != null && !justification.isBlank()) {
            return justification.trim();
        }

        Integer totalHours = estimation.getTotalHours();
        Integer feedbackHours = estimation.getActualHoursFeedback();
        if (totalHours != null && feedbackHours != null && !totalHours.equals(feedbackHours)) {
            return "Horas ajustadas por necesidad";
        }

        return "";
    }

    private void setCellValue(Row row, int columnIndex, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Number number) {
            row.createCell(columnIndex).setCellValue(number.doubleValue());
        } else {
            row.createCell(columnIndex).setCellValue(value.toString());
        }
    }
}
