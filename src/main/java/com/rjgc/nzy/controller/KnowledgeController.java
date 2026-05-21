package com.rjgc.nzy.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rjgc.nzy.common.Result;
import com.rjgc.nzy.entity.KnowledgeAtom;
import com.rjgc.nzy.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/add")
    public Result<KnowledgeAtom> add(@RequestBody KnowledgeAtom atom) {
        return Result.ok(knowledgeService.add(atom));
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody KnowledgeAtom atom) {
        knowledgeService.update(atom);
        return Result.ok();
    }

    @PostMapping("/archive/{id}")
    public Result<Void> archive(@PathVariable Long id) {
        knowledgeService.archive(id);
        return Result.ok();
    }

    @PostMapping("/restore/{id}")
    public Result<Void> restore(@PathVariable Long id) {
        knowledgeService.restore(id);
        return Result.ok();
    }

    @GetMapping("/detail/{id}")
    public Result<KnowledgeAtom> detail(@PathVariable Long id) {
        return Result.ok(knowledgeService.getById(id));
    }

    @GetMapping("/list")
    public Result<Page<KnowledgeAtom>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return Result.ok(knowledgeService.page(page, size, keyword, category, status));
    }

    @PostMapping("/batch-import")
    public Result<String> batchImport(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes());
            JSONArray array = JSON.parseArray(content);
            List<KnowledgeAtom> atoms = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String subject = obj.getString("subject");
                String principles = obj.getString("principles");

                if (subject == null || subject.isBlank()) {
                    errors.add("第" + (i + 1) + "条: 标题不能为空");
                    continue;
                }
                if (principles == null || principles.isBlank()) {
                    errors.add("第" + (i + 1) + "条: 核心内容不能为空");
                    continue;
                }

                KnowledgeAtom atom = new KnowledgeAtom();
                atom.setSubject(subject);
                atom.setCategory(obj.getString("category"));
                atom.setDifficulty(obj.getString("difficulty"));
                atom.setTags(obj.getString("tags"));
                atom.setPrinciples(principles);
                atom.setPitfalls(obj.getString("pitfalls"));
                atoms.add(atom);
            }

            if (!errors.isEmpty()) {
                return Result.error("校验失败: " + String.join("; ", errors));
            }

            int count = knowledgeService.batchAdd(atoms);
            return Result.ok("成功导入 " + count + " 条知识条目");
        } catch (Exception e) {
            return Result.error("文件解析失败: " + e.getMessage());
        }
    }
}
