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
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

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
        LambdaQueryWrapper<KnowledgeAtom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeAtom::getStatus, "ACTIVE");
        List<String> terms = extractSearchTerms(keyword);
        if (!terms.isEmpty()) {
            wrapper.and(w -> {
                w.like(KnowledgeAtom::getSubject, keyword)
                        .or().like(KnowledgeAtom::getPrinciples, keyword)
                        .or().like(KnowledgeAtom::getTags, keyword);
                for (int i = 0; i < terms.size(); i++) {
                    String term = terms.get(i);
                    w.or().like(KnowledgeAtom::getSubject, term)
                            .or().like(KnowledgeAtom::getPrinciples, term)
                            .or().like(KnowledgeAtom::getTags, term);
                }
            });
        }
        wrapper.orderByDesc(KnowledgeAtom::getCreateTime);
        wrapper.last("LIMIT " + limit);
        return knowledgeAtomMapper.selectList(wrapper);
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
                .replaceAll("[，。！？；：、,.!?;:()（）\\[\\]【】\"']", " ")
                .replace("什么是", " ")
                .replace("请解释", " ")
                .replace("请说明", " ")
                .replace("原理", " 原理 ")
                .replace("的", " ");
        for (String term : normalized.split("\\s+")) {
            String trimmed = term.trim();
            if (trimmed.length() >= 2 && terms.size() < 5 && !terms.contains(trimmed)) {
                terms.add(trimmed);
            }
        }
        return terms;
    }
}
