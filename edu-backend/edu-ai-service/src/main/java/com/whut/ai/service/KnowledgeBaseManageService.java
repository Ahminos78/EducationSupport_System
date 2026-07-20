package com.whut.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whut.ai.entity.KbDocument;
import com.whut.ai.entity.KbKnowledgeBase;
import com.whut.ai.mapper.KbDocumentMapper;
import com.whut.ai.mapper.KbKnowledgeBaseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeBaseManageService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseManageService.class);

    private final KbKnowledgeBaseMapper kbMapper;
    private final KbDocumentMapper docMapper;

    public KnowledgeBaseManageService(KbKnowledgeBaseMapper kbMapper, KbDocumentMapper docMapper) {
        this.kbMapper = kbMapper;
        this.docMapper = docMapper;
    }

    public KbKnowledgeBase createKnowledgeBase(String name, String description, Long createdBy) {
        KbKnowledgeBase kb = new KbKnowledgeBase();
        kb.setName(name);
        kb.setDescription(description);
        kb.setCollectionName("kb_" + System.currentTimeMillis());
        kb.setStatus(1);
        kb.setCreatedBy(createdBy);
        kbMapper.insert(kb);
        log.info("知识库创建成功 | id={} | name={}", kb.getId(), name);
        return kb;
    }

    public List<KbKnowledgeBase> listKnowledgeBases() {
        return kbMapper.selectList(
                new LambdaQueryWrapper<KbKnowledgeBase>()
                        .eq(KbKnowledgeBase::getStatus, 1)
                        .orderByDesc(KbKnowledgeBase::getCreatedAt)
        );
    }

    public KbKnowledgeBase getKnowledgeBase(Long kbId) {
        KbKnowledgeBase kb = kbMapper.selectById(kbId);
        if (kb == null) {
            throw new IllegalArgumentException("知识库不存在: " + kbId);
        }
        return kb;
    }

    public void updateKnowledgeBase(Long kbId, String name, String description) {
        KbKnowledgeBase kb = getKnowledgeBase(kbId);
        if (name != null && !name.isBlank()) {
            kb.setName(name);
        }
        if (description != null) {
            kb.setDescription(description);
        }
        kbMapper.updateById(kb);
        log.info("知识库更新成功 | id={}", kbId);
    }

    public void deleteKnowledgeBase(Long kbId) {
        KbKnowledgeBase kb = getKnowledgeBase(kbId);
        kb.setStatus(0);
        kbMapper.updateById(kb);
        log.info("知识库已停用 | id={}", kbId);
    }

    public List<KbDocument> listDocuments(Long kbId) {
        return docMapper.selectList(
                new LambdaQueryWrapper<KbDocument>()
                        .eq(KbDocument::getKbId, kbId)
                        .orderByDesc(KbDocument::getCreatedAt)
        );
    }

    public void deleteDocument(Long docId) {
        KbDocument doc = docMapper.selectById(docId);
        if (doc == null) {
            throw new IllegalArgumentException("文档不存在: " + docId);
        }
        docMapper.deleteById(docId);
        log.info("文档已删除 | docId={} | kbId={}", docId, doc.getKbId());
    }
}
