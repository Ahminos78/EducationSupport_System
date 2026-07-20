package com.whut.ai.rag;

import com.whut.ai.dto.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块服务（Text Splitter / Chunking）
 * 负责将长文本切分成多个合适大小的文本块（Chunk），
 * 是 RAG（检索增强生成）系统中的关键步骤之一。
 *
 * 为什么要做文本分块？
 * 1. 上下文窗口限制：大模型能处理的 Token 数量有限，不能把整篇长文档都塞进去
 * 2. 检索精度：小块的语义更集中，向量检索时更容易找到最相关的内容
 * 3. 性能效率：小块向量化更快，存储和检索也更高效
 *
 * 分块策略
 * 本服务实现的是最经典的固定大小分块 + 重叠策略：
 * - 按字符数切分，简单高效
 * - 支持重叠（Overlap），避免语义在切分点断裂
 * - 优先在段落、句子边界处切分，尽量保持语义完整
 */
@Service
public class TextSplitterService {

    private static final Logger log = LoggerFactory.getLogger(TextSplitterService.class);

    /**
     * 默认分块大小（字符数）
     */
    public static final int DEFAULT_CHUNK_SIZE = 500;

    /**
     * 默认重叠大小（字符数）
     */
    public static final int DEFAULT_CHUNK_OVERLAP = 50;

    /**
     * 将文本切分为多个块
     * 这是最核心的方法。使用"固定大小 + 重叠"的策略，
     * 并且尽量在自然的边界（段落、句子）处切分。
     *
     * 切分流程
     * 1. 先按段落分割（两个换行符 \n\n）
     * 2. 逐个段落累积，当累积长度接近 chunkSize 时切一刀
     * 3. 每个块包含 overlap 个字符的重叠内容
     * 4. 如果单个段落就超过 chunkSize，再按句子细分
     *
     * @param text           要切分的完整文本
     * @param chunkSize      每个块的目标大小（字符数）
     * @param chunkOverlap   相邻块的重叠大小（字符数）
     * @param sourceDocument 来源文档名（用于元数据）
     * @return 切分后的文档块列表
     */
    public List<DocumentChunk> splitText(String text, int chunkSize, int chunkOverlap, String sourceDocument) {
        // ========== 参数校验 ==========
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        if (chunkSize <= 0) {
            chunkSize = DEFAULT_CHUNK_SIZE;
        }
        if (chunkOverlap < 0) {
            chunkOverlap = DEFAULT_CHUNK_OVERLAP;
        }
        // 重叠不能大于块大小
        if (chunkOverlap >= chunkSize) {
            chunkOverlap = chunkSize / 10;
        }

        log.debug("开始切分文本 | 来源: {} | 总字符数: {} | 块大小: {} | 重叠: {}",
                sourceDocument, text.length(), chunkSize, chunkOverlap);

        List<DocumentChunk> chunks = new ArrayList<>();

        // ========== 第一步：先按段落分割 ==========
        // 段落之间用两个或以上换行符分隔
        String[] paragraphs = text.split("\n\\s*\n");

        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            // ========== 第二步：判断当前块加上这个段落会不会超限 ==========
            // 如果加上这个段落还没超限，就继续累积
            if (currentChunk.length() + paragraph.length() + 2 <= chunkSize) {
                // +2 是因为段落之间加两个换行（恢复段落结构）
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            } else {
                // ========== 第三步：超限了，先把当前块存起来 ==========
                if (currentChunk.length() > 0) {
                    DocumentChunk chunk = createChunk(currentChunk.toString(), chunkIndex, sourceDocument);
                    chunks.add(chunk);
                    chunkIndex++;

                    // ========== 第四步：计算重叠部分，作为下一个块的开头 ==========
                    String overlapText = getOverlapText(currentChunk.toString(), chunkOverlap);
                    currentChunk = new StringBuilder(overlapText);
                }

                // ========== 第五步：处理超长段落 ==========
                // 如果单个段落就超过了块大小，需要把这个段落再细分
                if (paragraph.length() > chunkSize) {
                    List<DocumentChunk> subChunks = splitLongParagraph(
                            paragraph, chunkSize, chunkOverlap, sourceDocument, chunkIndex);
                    chunks.addAll(subChunks);
                    chunkIndex += subChunks.size();

                    // 最后一个子块的重叠部分作为下一块的开头
                    if (!subChunks.isEmpty()) {
                        String lastContent = subChunks.get(subChunks.size() - 1).getContent();
                        String overlapText = getOverlapText(lastContent, chunkOverlap);
                        currentChunk = new StringBuilder(overlapText);
                    } else {
                        currentChunk = new StringBuilder();
                    }
                } else {
                    // 段落不超长，直接加进去
                    if (currentChunk.length() > 0) {
                        currentChunk.append("\n\n");
                    }
                    currentChunk.append(paragraph);
                }
            }
        }

        // ========== 第六步：处理最后剩下的内容 ==========
        if (currentChunk.length() > 0) {
            DocumentChunk chunk = createChunk(currentChunk.toString(), chunkIndex, sourceDocument);
            chunks.add(chunk);
        }

        log.info("文本切分完成 | 来源: {} | 总块数: {}", sourceDocument, chunks.size());
        return chunks;
    }

    /**
     * 使用默认参数切分文本
     */
    public List<DocumentChunk> splitText(String text, String sourceDocument) {
        return splitText(text, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP, sourceDocument);
    }

    /**
     * 切分超长段落
     * 当单个段落就超过 chunkSize 时，需要更细粒度的切分。
     * 先按句子切，如果句子还太长，就按字符硬切。
     */
    private List<DocumentChunk> splitLongParagraph(String paragraph, int chunkSize, int chunkOverlap,
                                                    String sourceDocument, int startIndex) {
        List<DocumentChunk> chunks = new ArrayList<>();

        // 先按句子分割（中文用句号、问号、感叹号，英文用 . ? !）
        String[] sentences = paragraph.split("(?<=[。！？.!?])");

        StringBuilder currentChunk = new StringBuilder();
        int chunkIndex = startIndex;

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) {
                continue;
            }

            if (currentChunk.length() + sentence.length() <= chunkSize) {
                currentChunk.append(sentence);
            } else {
                // 保存当前块
                if (currentChunk.length() > 0) {
                    DocumentChunk chunk = createChunk(currentChunk.toString(), chunkIndex, sourceDocument);
                    chunks.add(chunk);
                    chunkIndex++;

                    // 计算重叠
                    String overlapText = getOverlapText(currentChunk.toString(), chunkOverlap);
                    currentChunk = new StringBuilder(overlapText);
                }

                // 如果单个句子都比块大，只能按字符硬切了
                if (sentence.length() > chunkSize) {
                    List<DocumentChunk> hardChunks = splitHard(sentence, chunkSize, chunkOverlap,
                            sourceDocument, chunkIndex);
                    chunks.addAll(hardChunks);
                    chunkIndex += hardChunks.size();

                    if (!hardChunks.isEmpty()) {
                        String lastContent = hardChunks.get(hardChunks.size() - 1).getContent();
                        String overlapText = getOverlapText(lastContent, chunkOverlap);
                        currentChunk = new StringBuilder(overlapText);
                    } else {
                        currentChunk = new StringBuilder();
                    }
                } else {
                    currentChunk.append(sentence);
                }
            }
        }

        // 处理最后一块
        if (currentChunk.length() > 0) {
            DocumentChunk chunk = createChunk(currentChunk.toString(), chunkIndex, sourceDocument);
            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * 按字符硬切分（最后手段）
     * 当句子都超长时，只能按字符数硬切。
     * 这种情况比较少见，但对于无标点的长文本需要处理。
     */
    private List<DocumentChunk> splitHard(String text, int chunkSize, int chunkOverlap,
                                           String sourceDocument, int startIndex) {
        List<DocumentChunk> chunks = new ArrayList<>();
        int position = 0;
        int chunkIndex = startIndex;

        while (position < text.length()) {
            int end = Math.min(position + chunkSize, text.length());
            String chunkContent = text.substring(position, end);

            DocumentChunk chunk = createChunk(chunkContent, chunkIndex, sourceDocument);
            chunks.add(chunk);
            chunkIndex++;

            if (end >= text.length()) {
                break;
            }

            position = end - chunkOverlap;
            if (position >= end) {
                break;
            }
        }

        return chunks;
    }

    /**
     * 获取文本末尾的重叠部分
     * 从一段文本的末尾截取指定长度的内容，作为下一个块的开头。
     * 这样两个相邻块之间就有共享的重叠内容，保证语义连贯性。
     *
     * @param text    原文本
     * @param overlap 要截取的重叠长度
     * @return 重叠部分文本
     */
    private String getOverlapText(String text, int overlap) {
        if (text == null || text.isEmpty() || overlap <= 0) {
            return "";
        }
        if (text.length() <= overlap) {
            return text;
        }
        // 从末尾截取
        return text.substring(text.length() - overlap);
    }

    /**
     * 创建一个文档块对象
     * 封装创建 DocumentChunk 的逻辑，设置基本元数据。
     */
    private DocumentChunk createChunk(String content, int chunkIndex, String sourceDocument) {
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent(content.trim());
        chunk.setChunkIndex(chunkIndex);
        chunk.setSourceDocument(sourceDocument);
        return chunk;
    }
}
