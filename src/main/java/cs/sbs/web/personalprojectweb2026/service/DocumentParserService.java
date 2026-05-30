package cs.sbs.web.personalprojectweb2026.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class DocumentParserService {

    /**
     * Parse a document file and extract plain text.
     */
    public String parse(Path filePath, String fileType) throws IOException {
        return switch (fileType) {
            case "PDF" -> parsePdf(filePath);
            case "MARKDOWN" -> parseMarkdown(filePath);
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }

    private String parsePdf(Path filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            log.info("Parsed PDF: {} pages, {} chars", document.getNumberOfPages(), text.length());
            return text;
        }
    }

    private String parseMarkdown(Path filePath) throws IOException {
        String markdown = Files.readString(filePath);
        // Render markdown to plain text (strip formatting but keep structure)
        Parser parser = Parser.builder().build();
        TextContentRenderer renderer = TextContentRenderer.builder().build();
        String text = renderer.render(parser.parse(markdown));
        log.info("Parsed Markdown: {} chars (original: {} chars)", text.length(), markdown.length());
        return text;
    }
}
