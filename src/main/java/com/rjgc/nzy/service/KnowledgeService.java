package com.rjgc.nzy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson2.JSON;
import com.rjgc.nzy.entity.KnowledgeAtom;
import com.rjgc.nzy.mapper.KnowledgeAtomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private static final int MIN_AI_MATCH_SCORE = 3;
    private static final int MAX_SEARCH_TERMS = 10;

    private final KnowledgeAtomMapper knowledgeAtomMapper;

    public KnowledgeAtom add(KnowledgeAtom atom) {
        prepareForSave(atom, false);
        atom.setStatus("ACTIVE");
        knowledgeAtomMapper.insert(atom);
        return atom;
    }

    @Transactional
    public int batchAdd(List<KnowledgeAtom> atoms) {
        atoms.forEach(atom -> prepareForSave(atom, false));
        atoms.forEach(atom -> atom.setStatus("ACTIVE"));
        atoms.forEach(knowledgeAtomMapper::insert);
        return atoms.size();
    }

    public void update(KnowledgeAtom atom) {
        if (atom.getId() == null) {
            throw new RuntimeException("知识条目ID不能为空");
        }
        if (knowledgeAtomMapper.selectById(atom.getId()) == null) {
            throw new RuntimeException("知识条目不存在");
        }
        boolean tagsProvided = atom.getTags() != null;
        prepareForSave(atom, true);
        LambdaUpdateWrapper<KnowledgeAtom> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(KnowledgeAtom::getId, atom.getId());
        if (atom.getSubject() != null) {
            wrapper.set(KnowledgeAtom::getSubject, atom.getSubject());
        }
        if (atom.getCategory() != null) {
            wrapper.set(KnowledgeAtom::getCategory, atom.getCategory());
        }
        if (atom.getDifficulty() != null) {
            wrapper.set(KnowledgeAtom::getDifficulty, atom.getDifficulty());
        }
        if (tagsProvided) {
            wrapper.set(KnowledgeAtom::getTags, atom.getTags());
        }
        if (atom.getPrinciples() != null) {
            wrapper.set(KnowledgeAtom::getPrinciples, atom.getPrinciples());
        }
        if (atom.getPitfalls() != null) {
            wrapper.set(KnowledgeAtom::getPitfalls, atom.getPitfalls());
        }
        knowledgeAtomMapper.update(null, wrapper);
    }

    public void archive(Long id) {
        knowledgeAtomMapper.update(null,
                new LambdaUpdateWrapper<KnowledgeAtom>()
                        .eq(KnowledgeAtom::getId, id)
                        .set(KnowledgeAtom::getStatus, "ARCHIVED"));
    }

    public void restore(Long id) {
        knowledgeAtomMapper.update(null,
                new LambdaUpdateWrapper<KnowledgeAtom>()
                        .eq(KnowledgeAtom::getId, id)
                        .set(KnowledgeAtom::getStatus, "ACTIVE"));
    }

    public KnowledgeAtom getById(Long id) {
        return knowledgeAtomMapper.selectById(id);
    }

    public Page<KnowledgeAtom> page(int pageNum, int pageSize, String keyword, String category, String status) {
        LambdaQueryWrapper<KnowledgeAtom> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(KnowledgeAtom::getSubject, keyword)
                    .or().like(KnowledgeAtom::getPrinciples, keyword)
                    .or().like(KnowledgeAtom::getTags, keyword));
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(KnowledgeAtom::getCategory, category);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(KnowledgeAtom::getStatus, status);
        } else {
            wrapper.eq(KnowledgeAtom::getStatus, "ACTIVE");
        }
        wrapper.orderByDesc(KnowledgeAtom::getCreateTime);
        return knowledgeAtomMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<KnowledgeAtom> searchForAi(String keyword, int limit) {
        return searchForAiWithScores(keyword, limit).stream()
                .map(KnowledgeSearchResult::getAtom)
                .collect(Collectors.toList());
    }

    public List<KnowledgeSearchResult> searchForAiWithScores(String keyword, int limit) {
        List<String> terms = extractSearchTerms(keyword);
        if (terms.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<KnowledgeAtom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeAtom::getStatus, "ACTIVE");
        wrapper.and(w -> {
            w.like(KnowledgeAtom::getSubject, keyword)
                    .or().like(KnowledgeAtom::getPrinciples, keyword)
                    .or().like(KnowledgeAtom::getTags, keyword);
            for (String term : terms) {
                w.or().like(KnowledgeAtom::getSubject, term)
                        .or().like(KnowledgeAtom::getPrinciples, term)
                        .or().like(KnowledgeAtom::getTags, term);
            }
        });
        wrapper.orderByDesc(KnowledgeAtom::getCreateTime);
        wrapper.last("LIMIT " + Math.max(limit * 10, 20));
        return knowledgeAtomMapper.selectList(wrapper).stream()
                .map(atom -> new KnowledgeSearchResult(atom, calculateMatchScore(keyword, terms, atom)))
                .filter(result -> result.getScore() >= MIN_AI_MATCH_SCORE)
                .sorted(Comparator.comparingInt(KnowledgeSearchResult::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private void prepareForSave(KnowledgeAtom atom, boolean partialUpdate) {
        if (!partialUpdate || atom.getSubject() != null) {
            atom.setSubject(required(atom.getSubject(), "标题不能为空"));
        }
        if (!partialUpdate || atom.getCategory() != null) {
            atom.setCategory(required(atom.getCategory(), "分类不能为空"));
        }
        if (!partialUpdate || atom.getPrinciples() != null) {
            atom.setPrinciples(required(atom.getPrinciples(), "核心内容不能为空"));
        }
        if (!partialUpdate && (atom.getDifficulty() == null || atom.getDifficulty().isBlank())) {
            atom.setDifficulty("中等");
        } else if (atom.getDifficulty() != null && atom.getDifficulty().isBlank()) {
            atom.setDifficulty("中等");
        }
        atom.setTags(normalizeTags(atom.getTags()));
        if (atom.getPitfalls() != null && atom.getPitfalls().isBlank()) {
            atom.setPitfalls(null);
        }
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
        return value.trim();
    }

    private String normalizeTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return null;
        }
        try {
            JSON.parseArray(tags);
            return tags.trim();
        } catch (Exception e) {
            throw new RuntimeException("标签必须是 JSON 数组格式，如 [\"Java\", \"Spring\"]");
        }
    }

    private List<String> extractSearchTerms(String keyword) {
        List<String> terms = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) {
            return terms;
        }
        String normalized = keyword
                .toLowerCase(Locale.ROOT)
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
                .replace("怎么办", " ")
                .replace("原理", " 原理 ")
                .replace("思路", " 思路 ")
                .replace("流程", " 流程 ")
                .replace("排查", " 排查 ")
                .replace("的", " ")
                .replace("吗", " ")
                .replace("呢", " ")
                .replace("？", " ");

        List<String> longChineseTerms = new ArrayList<>();
        for (String term : normalized.split("\\s+")) {
            String trimmed = term.trim();
            if (trimmed.length() >= 2) {
                addSearchTerm(terms, trimmed);
            }
            if (isChineseText(trimmed) && trimmed.length() >= 5) {
                longChineseTerms.add(trimmed);
            }
        }
        for (String term : longChineseTerms) {
            for (int i = 0; i <= term.length() - 2 && terms.size() < MAX_SEARCH_TERMS; i++) {
                addSearchTerm(terms, term.substring(i, i + 2));
            }
        }
        return terms;
    }

    private void addSearchTerm(List<String> terms, String term) {
        if (terms.size() < MAX_SEARCH_TERMS && !terms.contains(term)) {
            terms.add(term);
        }
    }

    private boolean isChineseText(String value) {
        return value != null && value.matches(".*[\\u4e00-\\u9fa5].*");
    }

    private int calculateMatchScore(String keyword, List<String> terms, KnowledgeAtom atom) {
        int score = 0;
        String normalizedQuestion = normalizeForScore(keyword);
        String subject = normalizeForScore(atom.getSubject());
        String tags = normalizeForScore(atom.getTags());
        String principles = normalizeForScore(atom.getPrinciples());
        String pitfalls = normalizeForScore(atom.getPitfalls());

        if (!normalizedQuestion.isEmpty()) {
            if (subject.contains(normalizedQuestion)) {
                score += 10;
            }
            if (principles.contains(normalizedQuestion)) {
                score += 6;
            }
        }

        for (String term : terms) {
            String normalizedTerm = normalizeForScore(term);
            if (normalizedTerm.isEmpty()) {
                continue;
            }
            if (subject.contains(normalizedTerm)) {
                score += 5;
            }
            if (tags.contains(normalizedTerm)) {
                score += 4;
            }
            if (principles.contains(normalizedTerm)) {
                score += 3;
            }
            if (pitfalls.contains(normalizedTerm)) {
                score += 2;
            }
        }
        return score;
    }

    private String normalizeForScore(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT);
    }
}
