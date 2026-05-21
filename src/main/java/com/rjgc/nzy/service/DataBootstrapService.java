package com.rjgc.nzy.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rjgc.nzy.entity.KnowledgeAtom;
import com.rjgc.nzy.mapper.KnowledgeAtomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataBootstrapService {

    private final KnowledgeAtomMapper knowledgeAtomMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void seedKnowledgeBase() {
        Long count = knowledgeAtomMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            log.info("知识库已有 {} 条数据，跳过种子数据加载", count);
            return;
        }

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:knowledge_base/*.json");
            int total = 0;
            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JSONArray array = JSON.parseArray(content);
                    List<KnowledgeAtom> atoms = new ArrayList<>();
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        KnowledgeAtom atom = new KnowledgeAtom();
                        atom.setSubject(obj.getString("subject"));
                        atom.setCategory(obj.getString("category"));
                        atom.setDifficulty(obj.getString("difficulty"));
                        atom.setTags(obj.getString("tags"));
                        atom.setPrinciples(obj.getString("principles"));
                        atom.setPitfalls(obj.getString("pitfalls"));
                        atom.setStatus("ACTIVE");
                        atoms.add(atom);
                    }
                    atoms.forEach(knowledgeAtomMapper::insert);
                    total += atoms.size();
                }
            }
            log.info("种子数据加载完成，共 {} 条", total);
        } catch (Exception e) {
            log.warn("种子数据加载失败，可忽略: {}", e.getMessage());
        }
    }
}
