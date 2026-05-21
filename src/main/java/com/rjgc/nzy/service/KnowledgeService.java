package com.rjgc.nzy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rjgc.nzy.entity.KnowledgeAtom;
import com.rjgc.nzy.mapper.KnowledgeAtomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeAtomMapper knowledgeAtomMapper;

    public KnowledgeAtom add(KnowledgeAtom atom) {
        atom.setStatus("ACTIVE");
        knowledgeAtomMapper.insert(atom);
        return atom;
    }

    @Transactional
    public int batchAdd(List<KnowledgeAtom> atoms) {
        int count = 0;
        for (KnowledgeAtom atom : atoms) {
            atom.setStatus("ACTIVE");
            knowledgeAtomMapper.insert(atom);
            count++;
        }
        return count;
    }

    public void update(KnowledgeAtom atom) {
        knowledgeAtomMapper.updateById(atom);
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
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(KnowledgeAtom::getSubject, keyword)
                    .or().like(KnowledgeAtom::getPrinciples, keyword));
        }
        wrapper.orderByDesc(KnowledgeAtom::getCreateTime);
        wrapper.last("LIMIT " + limit);
        return knowledgeAtomMapper.selectList(wrapper);
    }
}
