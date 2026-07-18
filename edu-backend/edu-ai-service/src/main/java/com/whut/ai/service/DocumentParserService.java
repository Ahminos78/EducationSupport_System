package com.whut.ai.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentParserService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserService.class);

    public String parseDocument(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) throw new IOException("文件不存在: " + filePath);
        if (!file.isFile()) throw new IOException("路径不是一个文件: " + filePath);

        String fileName = file.getName().toLowerCase();
        log.info("开始解析文档: {}", fileName);

        String content;
        if (fileName.endsWith(".txt")) {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } else if (fileName.endsWith(".pdf")) {
            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                content = cleanText(stripper.getText(document));
            }
        } else if (fileName.endsWith(".docx")) {
            try (FileInputStream fis = new FileInputStream(file);
                 XWPFDocument document = new XWPFDocument(fis);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                content = cleanText(extractor.getText());
            }
        } else {
            throw new IllegalArgumentException("不支持的文件格式: " + fileName + "（目前支持 .txt、.pdf、.docx）");
        }

        log.info("文档解析完成 | 文件名: {} | 字符数: {}", fileName, content.length());
        return content;
    }

    public List<ParsedDocument> parseDirectory(String dirPath) throws IOException {
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) throw new IOException("目录不存在: " + dirPath);
        if (!Files.isDirectory(dir)) throw new IOException("路径不是一个目录: " + dirPath);

        List<ParsedDocument> results = new ArrayList<>();
        try (var stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    String lower = fileName.toLowerCase();
                    if (lower.endsWith(".txt") || lower.endsWith(".pdf") || lower.endsWith(".docx")) {
                        String content = parseDocument(path.toString());
                        results.add(new ParsedDocument(fileName, content));
                        log.info("已解析: {} ({} 字符)", fileName, content.length());
                    }
                } catch (Exception e) {
                    log.warn("解析文件失败: {} | 原因: {}", path.getFileName(), e.getMessage());
                }
            });
        }
        log.info("目录解析完成 | 共处理 {} 个文件", results.size());
        return results;
    }

    private String cleanText(String text) {
        if (text == null || text.isEmpty()) return "";
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        text = text.replaceAll("\\n{3,}", "\n\n");
        StringBuilder result = new StringBuilder();
        for (String line : text.split("\\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) result.append(trimmed).append("\n");
        }
        return result.toString().trim();
    }

    public record ParsedDocument(String fileName, String content) {}
}
