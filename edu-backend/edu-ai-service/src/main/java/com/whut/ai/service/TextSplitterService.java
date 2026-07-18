package com.whut.ai.service;

import com.whut.ai.dto.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextSplitterService {

    private static final Logger log = LoggerFactory.getLogger(TextSplitterService.class);
    public static final int DEFAULT_CHUNK_SIZE = 500;
    public static final int DEFAULT_CHUNK_OVERLAP = 50;

    public List<DocumentChunk> splitText(String text, int chunkSize, int chunkOverlap, String sourceDocument) {
        if (text == null || text.isEmpty()) return new ArrayList<>();
        if (chunkSize <= 0) chunkSize = DEFAULT_CHUNK_SIZE;
        if (chunkOverlap < 0) chunkOverlap = DEFAULT_CHUNK_OVERLAP;
        if (chunkOverlap >= chunkSize) chunkOverlap = chunkSize / 10;

        log.debug("开始分块文本 | 来源: {} | 总字符数: {} | 块大小: {} | 重叠: {}",
                sourceDocument, text.length(), chunkSize, chunkOverlap);

        List<DocumentChunk> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            if (currentChunk.length() + paragraph.length() + 2 <= chunkSize) {
                if (currentChunk.length() > 0) currentChunk.append("\n\n");
                currentChunk.append(paragraph);
            } else {
                if (currentChunk.length() > 0) {
                    chunks.add(createChunk(currentChunk.toString(), chunkIndex++, sourceDocument));
                    currentChunk = new StringBuilder(getOverlapText(currentChunk.toString(), chunkOverlap));
                }
                if (paragraph.length() > chunkSize) {
                    List<DocumentChunk> subChunks = splitHard(paragraph, chunkSize, chunkOverlap, sourceDocument, chunkIndex);
                    chunks.addAll(subChunks);
                    chunkIndex += subChunks.size();
                    if (!subChunks.isEmpty()) {
                        currentChunk = new StringBuilder(getOverlapText(subChunks.get(subChunks.size() - 1).getContent(), chunkOverlap));
                    }
                } else {
                    if (currentChunk.length() > 0) currentChunk.append("\n\n");
                    currentChunk.append(paragraph);
                }
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(createChunk(currentChunk.toString(), chunkIndex, sourceDocument));
        }

        log.info("文本分块完成 | 来源: {} | 总块数: {}", sourceDocument, chunks.size());
        return chunks;
    }

    public List<DocumentChunk> splitText(String text, String sourceDocument) {
        return splitText(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP, sourceDocument);
    }

    private List<DocumentChunk> splitHard(String text, int chunkSize, int chunkOverlap, String sourceDocument, int startIndex) {
        List<DocumentChunk> chunks = new ArrayList<>();
        int position = 0;
        int chunkIndex = startIndex;
        while (position < text.length()) {
            int end = Math.min(position + chunkSize, text.length());
            chunks.add(createChunk(text.substring(position, end), chunkIndex++, sourceDocument));
            if (end >= text.length()) break;
            int newPosition = end - chunkOverlap;
            if (newPosition <= position) break;
            position = newPosition;
        }
        return chunks;
    }

    private String getOverlapText(String text, int overlap) {
        if (text == null || text.isEmpty() || overlap <= 0) return "";
        if (text.length() <= overlap) return text;
        return text.substring(text.length() - overlap);
    }

    private DocumentChunk createChunk(String content, int chunkIndex, String sourceDocument) {
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent(content.trim());
        chunk.setChunkIndex(chunkIndex);
        chunk.setSourceDocument(sourceDocument);
        return chunk;
    }
}
