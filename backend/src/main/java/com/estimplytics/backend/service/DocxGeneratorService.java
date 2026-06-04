package com.estimplytics.backend.service;

import com.estimplytics.backend.dto.ImpactAnalysisDocumentDTO;
import com.estimplytics.backend.exception.ReportGenerationException;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class DocxGeneratorService {

    private static final String TESTS_INTRO = "El objetivo de las pruebas es verificar el correcto funcionamiento de la aplicación.";

    public byte[] exportImpactAnalysis(ImpactAnalysisDocumentDTO document) {
        try (XWPFDocument wordDocument = new XWPFDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writeTitle(wordDocument, "%s | %s".formatted(document.projectCode(), document.requestCode()), true, 32);
            writeTitle(wordDocument, "Análisis de Impacto - %s - %s".formatted(document.demandType(), document.priority()), false, 24);
            writeTitle(wordDocument, "%s - %s".formatted(document.documentAuthor(), document.date()), false, 18);

            writeSpaces(wordDocument, 3);

            writeSection(wordDocument, "Descripción abreviada de la Petición", document.briefDescription());
            writeSection(wordDocument, "Descripción del Impacto y consideraciones/decisiones de interés (Elementos Afectados)", document.impactDescription());
            writeSection(wordDocument, "Descripción de la Solución (Actividades de Desarrollo a realizar)", document.solutionDescription());
            writeSection(wordDocument, "Requisitos funcionales", document.functionalRequirements());
            writeSection(wordDocument, "Pruebas", TESTS_INTRO);
            writeParagraph(wordDocument, document.tests(), false);

            writeFooter(wordDocument, document);
            wordDocument.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new ReportGenerationException("Error while generating DOCX", exception);
        }
    }

    private void writeTitle(XWPFDocument wordDocument, String content, boolean bold, int fontSize) {
        XWPFParagraph paragraph = wordDocument.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        run.setBold(bold);
        run.setFontSize(fontSize);
        run.setText(content);
    }

    private void writeSection(XWPFDocument wordDocument, String title, String content) {
        writeParagraph(wordDocument, title, true);
        writeParagraph(wordDocument, content, false);
        wordDocument.createParagraph();
    }

    private void writeParagraph(XWPFDocument wordDocument, String content, boolean bold) {
        XWPFParagraph paragraph = wordDocument.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setBold(bold);
        run.setText(content);
    }

    private void writeFooter(XWPFDocument wordDocument, ImpactAnalysisDocumentDTO document) {
        XWPFFooter footer = wordDocument.createFooter(HeaderFooterType.DEFAULT);
        XWPFParagraph paragraph = footer.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        paragraph.createRun().setText("%s     Análisis de Impacto     Versión: %s     ".formatted(document.projectCode(), document.version()));
        paragraph.getCTP().addNewFldSimple().setInstr(" PAGE ");
    }

    private void writeSpaces(XWPFDocument wordDocument, int numberOfSpaces) {
        for (int i = 0; i < numberOfSpaces; i++) {
            writeParagraph(wordDocument, "", false);
        }
    }
}
