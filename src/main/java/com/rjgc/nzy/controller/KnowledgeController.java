package com.rjgc.nzy.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rjgc.nzy.common.Result;
import com.rjgc.nzy.entity.KnowledgeChunk;
import com.rjgc.nzy.entity.KnowledgeDocument;
import com.rjgc.nzy.service.ChunkSearchResult;
import com.rjgc.nzy.service.KnowledgeDocumentService;
import com.rjgc.nzy.service.KnowledgeStats;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeDocumentService knowledgeDocumentService;

    @PostMapping("/documents/import")
    public Result<KnowledgeDocument> importDocument(@RequestParam("file") MultipartFile file) {
        return Result.ok("导入成功", knowledgeDocumentService.importDocument(file));
    }

    @GetMapping("/documents")
    public Result<Page<KnowledgeDocument>> documents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.ok(knowledgeDocumentService.pageDocuments(page, size, keyword, status));
    }

    @GetMapping("/documents/{id}")
    public Result<KnowledgeDocument> document(@PathVariable Long id) {
        return Result.ok(knowledgeDocumentService.getDocument(id));
    }

    @GetMapping("/documents/{id}/chunks")
    public Result<List<KnowledgeChunk>> chunks(@PathVariable Long id) {
        return Result.ok(knowledgeDocumentService.listChunks(id));
    }

    @GetMapping(value = "/chunks/search", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public Result<List<ChunkSearchResult>> searchChunks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(knowledgeDocumentService.searchChunks(keyword, limit));
    }

    @PostMapping("/documents/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        knowledgeDocumentService.archiveDocument(id);
        return Result.ok();
    }

    @PostMapping("/documents/{id}/restore")
    public Result<Void> restore(@PathVariable Long id) {
        knowledgeDocumentService.restoreDocument(id);
        return Result.ok();
    }

    @DeleteMapping("/documents/{id}")
    public Result<Void> deleteDocument(@PathVariable Long id) {
        knowledgeDocumentService.deleteDocument(id);
        return Result.ok();
    }

    @GetMapping("/stats")
    public Result<KnowledgeStats> stats() {
        return Result.ok(knowledgeDocumentService.stats());
    }
}
