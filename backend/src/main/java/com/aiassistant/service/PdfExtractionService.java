package com.aiassistant.service;

import com.aiassistant.exception.InvalidDocumentException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExtractionService.class);
    private static final int MAX_PAGE_LIMIT = 20;

    static {
        System.setProperty("java.awt.headless", "true");
        System.setProperty("org.apache.pdfbox.fontcache", System.getProperty("java.io.tmpdir", "."));
    }

    public static class ExtractedPage {
        private final int pageNumber;
        private final String text;

        public ExtractedPage(int pageNumber, String text) {
            this.pageNumber = pageNumber;
            this.text = text;
        }

        public int getPageNumber() { return pageNumber; }
        public String getText() { return text; }
    }

    public static class ExtractedChunk {
        private final int chunkIndex;
        private final int pageNumber;
        private final String content;

        public ExtractedChunk(int chunkIndex, int pageNumber, String content) {
            this.chunkIndex = chunkIndex;
            this.pageNumber = pageNumber;
            this.content = content;
        }

        public int getChunkIndex() { return chunkIndex; }
        public int getPageNumber() { return pageNumber; }
        public String getContent() { return content; }
    }

    public static class PdfExtractionResult {
        private final int pageCount;
        private final List<ExtractedChunk> chunks;

        public PdfExtractionResult(int pageCount, List<ExtractedChunk> chunks) {
            this.pageCount = pageCount;
            this.chunks = chunks;
        }

        public int getPageCount() { return pageCount; }
        public List<ExtractedChunk> getChunks() { return chunks; }
    }

    public PdfExtractionResult processPdf(byte[] pdfBytes, String fileName) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new InvalidDocumentException("Uploaded file is empty.");
        }

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            int pageCount = document.getNumberOfPages();
            logger.info("PDF '{}' loaded successfully. Total pages: {}", fileName, pageCount);

            if (pageCount > MAX_PAGE_LIMIT) {
                throw new InvalidDocumentException("Document exceeds the maximum limit of " + MAX_PAGE_LIMIT + " pages. (Found: " + pageCount + " pages)");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            List<ExtractedChunk> allChunks = new ArrayList<>();
            int chunkIndex = 0;

            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageText = stripper.getText(document);

                if (pageText == null || pageText.trim().isEmpty()) {
                    continue;
                }

                // Chunk page text into paragraphs or semantic segments of approx 1000 characters
                List<String> pageSegments = splitIntoSegments(pageText.trim(), 1000);
                for (String segment : pageSegments) {
                    if (!segment.trim().isEmpty()) {
                        allChunks.add(new ExtractedChunk(chunkIndex++, page, segment.trim()));
                    }
                }
            }

            if (allChunks.isEmpty()) {
                throw new InvalidDocumentException("No readable text could be extracted from PDF '" + fileName + "'. Please ensure the PDF is not an image scan.");
            }

            return new PdfExtractionResult(pageCount, allChunks);

        } catch (InvalidDocumentException ide) {
            throw ide;
        } catch (IOException e) {
            logger.error("Failed to parse PDF document {}: {}", fileName, e.getMessage());
            throw new InvalidDocumentException("Failed to read or parse PDF file. The file may be corrupt or encrypted: " + e.getMessage());
        }
    }

    private List<String> splitIntoSegments(String text, int targetChunkSize) {
        List<String> segments = new ArrayList<>();
        String[] paragraphs = text.split("\n\n+");
        StringBuilder current = new StringBuilder();

        for (String para : paragraphs) {
            if (current.length() + para.length() > targetChunkSize && current.length() > 0) {
                segments.add(current.toString().trim());
                current.setLength(0);
            }
            if (current.length() > 0) {
                current.append("\n\n");
            }
            current.append(para);
        }

        if (current.length() > 0) {
            segments.add(current.toString().trim());
        }

        return segments;
    }
}
