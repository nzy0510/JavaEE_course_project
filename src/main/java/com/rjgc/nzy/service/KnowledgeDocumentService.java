package com.rjgc.nzy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rjgc.nzy.entity.KnowledgeChunk;
import com.rjgc.nzy.entity.KnowledgeDocument;
import com.rjgc.nzy.mapper.KnowledgeChunkMapper;
import com.rjgc.nzy.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeDocumentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "html", "htm", "md", "txt");
    private static final Set<String> GENERIC_INTENT_TERMS = Set.of(
            "原理", "流程", "策略", "区别", "思路", "步骤", "方法", "场景", "应用");
    private static final int MIN_MATCH_SCORE = 3;
    private static final int MAX_TERMS_PER_QUERY = 10;
    private static final int CANDIDATE_LIMIT = 200;

    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final DocumentConversionService conversionService;
    private final MarkdownChunker chunker;
    private final KnowledgeCategoryClassifier categoryClassifier;

    @Transactional
    public KnowledgeDocument importDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文档");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("不支持的文件类型: " + extension);
        }

        KnowledgeDocument document = new KnowledgeDocument();
        document.setOriginalFilename(originalFilename == null || originalFilename.isBlank() ? "未命名文档" : originalFilename);
        document.setStoredFilename(null);
        document.setFileExtension(extension);
        document.setFileSize(file.getSize());
        document.setKnowledgeCategory(categoryClassifier.classify(originalFilename, ""));
        document.setStatus("PROCESSING");
        document.setChunkCount(0);
        documentMapper.insert(document);

        try {
            String markdown = conversionService.convert(file, extension).markdown();
            List<MarkdownSection> sections = chunker.chunk(markdown);
            if (sections.isEmpty()) {
                throw new RuntimeException("文档未转换出有效文本内容");
            }
            int index = 1;
            for (MarkdownSection section : sections) {
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocumentId(document.getId());
                chunk.setChunkIndex(index++);
                chunk.setTitlePath(section.getTitlePath());
                chunk.setContent(section.getContent());
                chunk.setContentHash(sha256(section.getContent()));
                chunk.setTokenEstimate(estimateTokens(section.getContent()));
                chunk.setStatus("ACTIVE");
                chunkMapper.insert(chunk);
            }
            document.setStatus("ACTIVE");
            document.setChunkCount(sections.size());
            documentMapper.updateById(document);
            return document;
        } catch (RuntimeException e) {
            document.setStatus("FAILED");
            document.setErrorMessage(abbreviate(e.getMessage()));
            documentMapper.updateById(document);
            throw e;
        }
    }

    public Page<KnowledgeDocument> pageDocuments(int pageNum, int pageSize, String keyword, String status) {
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(KnowledgeDocument::getOriginalFilename, keyword.trim());
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(KnowledgeDocument::getStatus, status);
        }
        wrapper.orderByDesc(KnowledgeDocument::getCreateTime);
        return documentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public KnowledgeDocument getDocument(Long id) {
        return documentMapper.selectById(id);
    }

    public List<KnowledgeChunk> listChunks(Long documentId) {
        return chunkMapper.selectList(new LambdaQueryWrapper<KnowledgeChunk>()
                .eq(KnowledgeChunk::getDocumentId, documentId)
                .orderByAsc(KnowledgeChunk::getChunkIndex));
    }

    public void archiveDocument(Long id) {
        documentMapper.update(null, new LambdaUpdateWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getId, id)
                .set(KnowledgeDocument::getStatus, "ARCHIVED"));
        chunkMapper.update(null, new LambdaUpdateWrapper<KnowledgeChunk>()
                .eq(KnowledgeChunk::getDocumentId, id)
                .set(KnowledgeChunk::getStatus, "ARCHIVED"));
    }

    public void restoreDocument(Long id) {
        documentMapper.update(null, new LambdaUpdateWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getId, id)
                .set(KnowledgeDocument::getStatus, "ACTIVE"));
        chunkMapper.update(null, new LambdaUpdateWrapper<KnowledgeChunk>()
                .eq(KnowledgeChunk::getDocumentId, id)
                .set(KnowledgeChunk::getStatus, "ACTIVE"));
    }

    public void deleteDocument(Long id) {
        if (id == null) {
            throw new RuntimeException("文档ID不能为空");
        }
        documentMapper.deleteById(id);
    }

    public KnowledgeStats stats() {
        long documents = documentMapper.selectCount(new LambdaQueryWrapper<>());
        long activeDocuments = documentMapper.selectCount(new LambdaQueryWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getStatus, "ACTIVE"));
        long archivedDocuments = documentMapper.selectCount(new LambdaQueryWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getStatus, "ARCHIVED"));
        long chunks = chunkMapper.selectCount(new LambdaQueryWrapper<KnowledgeChunk>()
                .eq(KnowledgeChunk::getStatus, "ACTIVE"));
        return new KnowledgeStats(documents, activeDocuments, archivedDocuments, chunks,
                documentMapper.selectActiveCategoryStats());
    }

    public List<ChunkSearchResult> searchForAi(List<String> queries, int limit) {
        return searchForAi(queries, limit, null);
    }

    public List<ChunkSearchResult> searchForAi(List<String> queries, int limit, String knowledgeCategory) {
        List<String> normalizedQueries = queries == null ? List.of() : queries.stream()
                .filter(query -> query != null && !query.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (normalizedQueries.isEmpty()) {
            return List.of();
        }

        Set<String> terms = new HashSet<>();
        normalizedQueries.forEach(query -> terms.addAll(extractTerms(query)));
        if (terms.isEmpty()) {
            return List.of();
        }
        Set<String> specificTerms = terms.stream()
                .filter(term -> !isGenericIntentTerm(term))
                .collect(Collectors.toSet());
        Set<String> searchableTerms = specificTerms.isEmpty() ? terms : specificTerms;

        List<KnowledgeDocument> scopedDocuments = findScopedDocuments(knowledgeCategory);
        if (knowledgeCategory != null && !knowledgeCategory.isBlank() && scopedDocuments.isEmpty()) {
            return List.of();
        }
        Set<Long> scopedDocumentIds = scopedDocuments.stream()
                .map(KnowledgeDocument::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<KnowledgeChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeChunk::getStatus, "ACTIVE");
        if (!scopedDocumentIds.isEmpty()) {
            wrapper.in(KnowledgeChunk::getDocumentId, scopedDocumentIds);
        }
        wrapper.and(w -> {
            boolean first = true;
            for (String query : normalizedQueries) {
                if (first) {
                    w.like(KnowledgeChunk::getTitlePath, query)
                            .or().like(KnowledgeChunk::getContent, query);
                    first = false;
                } else {
                    w.or().like(KnowledgeChunk::getTitlePath, query)
                            .or().like(KnowledgeChunk::getContent, query);
                }
            }
            for (String term : searchableTerms) {
                w.or().like(KnowledgeChunk::getTitlePath, term)
                        .or().like(KnowledgeChunk::getContent, term);
            }
        });
        wrapper.orderByDesc(KnowledgeChunk::getCreateTime);
        wrapper.last("LIMIT " + Math.max(limit * 50, CANDIDATE_LIMIT));

        List<KnowledgeChunk> chunks = chunkMapper.selectList(wrapper);
        if (!scopedDocumentIds.isEmpty()) {
            chunks = chunks.stream()
                    .filter(chunk -> scopedDocumentIds.contains(chunk.getDocumentId()))
                    .toList();
        }
        Map<Long, KnowledgeDocument> documents = scopedDocuments.isEmpty()
                ? loadDocuments(chunks)
                : scopedDocuments.stream()
                .collect(Collectors.toMap(KnowledgeDocument::getId, Function.identity(), (left, right) -> left));
        return chunks.stream()
                .map(chunk -> new ChunkSearchResult(chunk, documents.get(chunk.getDocumentId()),
                        calculateScore(chunk, normalizedQueries, terms)))
                .filter(result -> result.getScore() >= MIN_MATCH_SCORE)
                .sorted(Comparator.comparingInt(ChunkSearchResult::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<ChunkSearchResult> searchChunks(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return searchForAi(List.of(keyword.trim()), Math.max(1, Math.min(limit, 50)));
    }

    private List<KnowledgeDocument> findScopedDocuments(String knowledgeCategory) {
        if (knowledgeCategory == null || knowledgeCategory.isBlank()) {
            return List.of();
        }
        return documentMapper.selectList(new LambdaQueryWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getStatus, "ACTIVE")
                .eq(KnowledgeDocument::getKnowledgeCategory, knowledgeCategory.trim()));
    }

    private Map<Long, KnowledgeDocument> loadDocuments(List<KnowledgeChunk> chunks) {
        Set<Long> documentIds = chunks.stream()
                .map(KnowledgeChunk::getDocumentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (documentIds.isEmpty()) {
            return Map.of();
        }
        return documentMapper.selectBatchIds(documentIds).stream()
                .collect(Collectors.toMap(KnowledgeDocument::getId, Function.identity(), (left, right) -> left));
    }

    private int calculateScore(KnowledgeChunk chunk, List<String> queries, Set<String> terms) {
        int score = 0;
        String title = normalize(chunk.getTitlePath());
        String content = normalize(chunk.getContent());
        for (String query : queries) {
            String normalizedQuery = normalize(query);
            score += boundedOccurrences(title, normalizedQuery, 2) * 12;
            score += boundedOccurrences(content, normalizedQuery, 2) * 8;
        }
        for (String term : terms) {
            String normalizedTerm = normalize(term);
            if (normalizedTerm.isBlank()) {
                continue;
            }
            int titleWeight = isGenericIntentTerm(normalizedTerm) ? 1 : 5;
            int contentWeight = isGenericIntentTerm(normalizedTerm) ? 1 : 3;
            score += boundedOccurrences(title, normalizedTerm, 3) * titleWeight;
            score += boundedOccurrences(content, normalizedTerm, 5) * contentWeight;
        }
        if (content.length() > 1500) {
            score -= 1;
        }
        return score;
    }

    private int boundedOccurrences(String source, String needle, int max) {
        if (source == null || source.isBlank() || needle == null || needle.isBlank() || max <= 0) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while (count < max) {
            index = source.indexOf(needle, index);
            if (index < 0) {
                break;
            }
            count++;
            index += needle.length();
        }
        return count;
    }

    private List<String> extractTerms(String query) {
        List<String> terms = new ArrayList<>();
        String normalized = normalize(query)
                .replaceAll("[，。！？；：、,.!?;:()（）\\[\\]【】\"']", " ")
                .replace("什么是", " ")
                .replace("请解释", " ")
                .replace("请说明", " ")
                .replace("请问", " ")
                .replace("该如何", " ")
                .replace("如何", " ")
                .replace("怎么", " ")
                .replace("怎样", " ")
                .replace("为什么", " ")
                .replace("原理", " 原理 ")
                .replace("流程", " 流程 ")
                .replace("策略", " 策略 ")
                .replace("区别", " 区别 ")
                .replace("的", " ")
                .replace("吗", " ")
                .replace("呢", " ");
        for (String raw : normalized.split("\\s+")) {
            String term = raw.trim();
            if (term.length() >= 2 && terms.size() < MAX_TERMS_PER_QUERY) {
                terms.add(term);
            }
            if (isChinese(term) && term.length() >= 5) {
                for (int i = 0; i <= term.length() - 2 && terms.size() < MAX_TERMS_PER_QUERY; i++) {
                    terms.add(term.substring(i, i + 2));
                }
            }
        }
        return terms;
    }

    private boolean isChinese(String value) {
        return value != null && value.matches(".*[\\u4e00-\\u9fa5].*");
    }

    private boolean isGenericIntentTerm(String value) {
        return value != null && GENERIC_INTENT_TERMS.contains(value.trim().toLowerCase(Locale.ROOT));
    }

    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            throw new RuntimeException("文件必须带有扩展名");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private int estimateTokens(String content) {
        return Math.max(1, content == null ? 0 : content.length() / 2);
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((content == null ? "" : content).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String abbreviate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
