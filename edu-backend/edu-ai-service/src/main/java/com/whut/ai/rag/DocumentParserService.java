package com.whut.ai.rag;

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

/**
 * 文档解析服务
 * 负责将各种格式的企业文档解析为纯文本，为后续的分块和向量化做准备。
 *
 * 支持的文档格式
 * - .txt：纯文本文件（最简单，直接读取）
 * - .pdf：PDF 文档（使用 Apache PDFBox 解析）
 * - .docx：Word 文档（使用 Apache POI 解析）
 *
 * 什么是文档解析？
 * 企业里的文档格式五花八门（PDF、Word、PPT、Excel...），每种格式都有自己的二进制结构，
 * 不能直接拿来做向量化。文档解析的工作就是把这些不同格式的文档中的文字内容提取出来，
 * 变成统一的纯文本字符串。
 * 就像你要做沙拉，得先把不同包装（PDF/纸盒/塑料袋）拆掉，拿出里面的食材（文字）。
 */
@Service
public class DocumentParserService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserService.class);

    /**
     * 解析单个文档
     * <p>
     * 根据文件扩展名自动选择合适的解析器，提取纯文本内容。
     * </p>
     *
     * @param filePath 文档的绝对路径
     * @return 解析后的纯文本内容
     * @throws IOException 文件读取或解析失败时抛出
     */
    public String parseDocument(String filePath) throws IOException {
        File file = new File(filePath);

        // ========== 第一步：检查文件是否存在 ==========
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        if (!file.isFile()) {
            throw new IOException("路径不是一个文件: " + filePath);
        }

        String fileName = file.getName().toLowerCase();
        log.info("开始解析文档: {}", fileName);

        // ========== 第二步：根据文件扩展名选择解析方式 ==========
        String content;
        if (fileName.endsWith(".txt")) {
            content = parseTxt(file);
        } else if (fileName.endsWith(".pdf")) {
            content = parsePdf(file);
        } else if (fileName.endsWith(".docx")) {
            content = parseDocx(file);
        } else {
            // 不支持的格式，抛出异常
            throw new IllegalArgumentException("不支持的文档格式: " + fileName +
                    "，目前支持 .txt、.pdf、.docx");
        }

        log.info("文档解析完成 | 文件名: {} | 字符数: {}", fileName, content.length());
        return content;
    }

    /**
     * 批量解析目录下的所有文档
     * <p>
     * 遍历指定目录，解析所有支持格式的文件，返回文件名和内容的映射。
     * </p>
     *
     * @param dirPath 目录路径
     * @return 解析结果列表，每项包含文件名和内容
     * @throws IOException 目录读取失败时抛出
     */
    public List<ParsedDocument> parseDirectory(String dirPath) throws IOException {
        Path dir = Paths.get(dirPath);

        if (!Files.exists(dir)) {
            throw new IOException("目录不存在: " + dirPath);
        }
        if (!Files.isDirectory(dir)) {
            throw new IOException("路径不是目录: " + dirPath);
        }

        List<ParsedDocument> results = new ArrayList<>();

        // 遍历目录下的所有文件
        try (var stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                try {
                    String fileName = path.getFileName().toString();
                    // 只处理支持的格式
                    if (isSupportedFormat(fileName)) {
                        String content = parseDocument(path.toString());
                        results.add(new ParsedDocument(fileName, content));
                        log.info("已解析: {} ({} 字符)", fileName, content.length());
                    }
                } catch (Exception e) {
                    log.warn("解析文件失败: {} | 原因: {}", path.getFileName(), e.getMessage());
                }
            });
        }

        log.info("目录解析完成 | 共解析 {} 个文件", results.size());
        return results;
    }

    /**
     * 判断文件是否为支持的格式
     */
    private boolean isSupportedFormat(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".txt") || lower.endsWith(".pdf") || lower.endsWith(".docx");
    }

    /**
     * 解析 TXT 纯文本文件
     * <p>
     * 最简单的格式，直接读取文件内容即可。
     * 使用 UTF-8 编码读取，避免中文乱码。
     * </p>
     */
    private String parseTxt(File file) throws IOException {
        // Files.readString 是 Java 11+ 提供的便捷方法，
        // 一次性读取整个文件为字符串
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * 解析 PDF 文档
     * 使用 Apache PDFBox 库提取 PDF 中的文本。
     *
     * PDFBox 工作原理
     * 1. 加载 PDF 文件到内存（PDDocument）
     * 2. 创建文本提取器（PDFTextStripper）
     * 3. 逐页提取文本内容
     * 4. 关闭资源
     *
     * 注意事项
     * - 扫描版 PDF（图片型 PDF）无法提取文字，需要 OCR
     * - 复杂排版的 PDF 可能提取顺序不对
     * - 加密的 PDF 需要密码才能打开
     */
    private String parsePdf(File file) throws IOException {
        // try-with-resources 语法：自动关闭资源，无需手动写 finally 关闭
        try (PDDocument document = PDDocument.load(file)) {
            // 创建文本提取器
            PDFTextStripper stripper = new PDFTextStripper();

            // 设置排序：按照阅读顺序提取文本（对于多栏排版的 PDF 很重要）
            stripper.setSortByPosition(true);

            // 提取所有页面的文本
            // 也可以通过 setStartPage()/setEndPage() 指定页码范围
            String text = stripper.getText(document);

            // 去除多余的空行和空白字符
            return cleanText(text);
        }
    }

    /**
     * 解析 Word 文档（.docx）
     * 使用 Apache POI 库提取 Word 文档中的文本。
     * 注意：只支持 .docx 格式（Office 2007+），不支持旧版 .doc 格式。
     */
    private String parseDocx(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            // 提取全部文本内容
            String text = extractor.getText();
            return cleanText(text);
        }
    }

    /**
     * 清理文本
     * 去除多余的空白字符、空行，规范化文本格式。
     * 这一步很重要，因为 PDF 和 Word 提取的文本经常有大量无用的空格和换行。
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 1. 把 Windows 换行(\r\n) 和旧 Mac 换行(\r) 统一为 Unix 换行(\n)
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        // 2. 把连续多个空行压缩成一个空行
        // (3个以上换行变成2个换行)
        text = text.replaceAll("\n{3,}", "\n\n");

        // 3. 把行首行尾的空格去掉（每行）
        // 先按行分割，去掉每行首尾空白，再拼回去
        StringBuilder result = new StringBuilder();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.append(trimmed).append("\n");
            }
        }

        // 4. 去掉首尾多余的空白
        return result.toString().trim();
    }

    /**
     * 解析结果内部类
     * 封装文件名和对应的文本内容。
     * 用 record 是 Java 16+ 的新特性，自动生成构造函数、getter、equals、hashCode、toString。
     */
    public record ParsedDocument(String fileName, String content) {
    }
}
