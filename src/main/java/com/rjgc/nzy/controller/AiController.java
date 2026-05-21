package com.rjgc.nzy.controller;

import com.rjgc.nzy.common.Result;
import com.rjgc.nzy.dto.AiQuestionRequest;
import com.rjgc.nzy.service.AiService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/ask")
    public Result<String> ask(@Valid @RequestBody AiQuestionRequest request) {
        String answer = aiService.ask(request.getQuestion());
        return Result.ok(answer);
    }
}
