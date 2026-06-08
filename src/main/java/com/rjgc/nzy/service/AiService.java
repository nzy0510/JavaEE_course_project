package com.rjgc.nzy.service;

import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final int RETRIEVAL_LIMIT = 3;

    private final KnowledgeDocumentService knowledgeDocumentService;
    private final ObjectProvider<OpenAiChatModel> chatModelProvider;

    public String ask(String question) {
        return ask(question, null);
    }

    public String ask(String question, String knowledgeCategory) {
        OpenAiChatModel chatModel = chatModelProvider.getIfAvailable();
        List<String> queries = rewriteQueries(question, chatModel);
        List<ChunkSearchResult> results = knowledgeDocumentService.searchForAi(queries, RETRIEVAL_LIMIT, knowledgeCategory);
        String context = buildContext(results);
        String scope = normalizeScope(knowledgeCategory);

        if (chatModel == null) {
            return "AI 服务未配置：请设置环境变量 DEEPSEEK_API_KEY 后重启应用。"
                    + "\n\n当前限定分类：" + scope
                    + "\n\n当前检索查询：\n" + String.join("\n", queries)
                    + "\n\n当前检索到的知识库内容：\n" + context;
        }

        String prompt = """
                你是一个严谨的知识库问答助手。请只根据【知识库片段】回答用户问题。
                如果片段中没有足够依据，请明确说明“当前知识库资料不足以回答”。
                回答时优先综合多个片段，不要编造片段外事实。

                【知识库片段】
                %s

                【限定知识分类】
                %s

                【用户原始问题】
                %s

                请用中文回答，结构清晰、简洁明了。""".formatted(context, scope, question);

        try {
            return chatModel.generate(prompt);
        } catch (RuntimeException e) {
            return "AI 服务调用失败，请检查 DEEPSEEK_API_KEY、网络或模型额度配置。"
                    + "\n\n当前限定分类：" + scope
                    + "\n\n当前检索查询：\n" + String.join("\n", queries)
                    + "\n\n当前检索到的知识库内容：\n" + context;
        }
    }

    private List<String> rewriteQueries(String question, OpenAiChatModel chatModel) {
        List<String> fallback = List.of(question);
        if (chatModel == null) {
            return fallback;
        }
        String prompt = """
                请将用户问题改写成 2 到 3 个适合在技术文档中做关键词检索的中文查询。
                要求：
                1. 每行一个查询；
                2. 保留关键技术名词；
                3. 不要解释，不要编号。

                用户问题：%s""".formatted(question);
        try {
            String generated = chatModel.generate(prompt);
            List<String> queries = new ArrayList<>();
            for (String line : generated.split("\\R")) {
                String query = line.replaceFirst("^\\s*\\d+[.、)]\\s*", "").trim();
                if (!query.isBlank() && queries.size() < 3) {
                    queries.add(query);
                }
            }
            if (!queries.contains(question)) {
                queries.add(0, question);
            }
            return queries.stream().filter(value -> !value.isBlank()).distinct().limit(3).toList();
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private String buildContext(List<ChunkSearchResult> results) {
        if (results == null || results.isEmpty()) {
            return "知识库中暂无相关内容。";
        }
        return results.stream()
                .map(result -> {
                    String filename = result.getDocument() == null
                            ? "未知文档"
                            : result.getDocument().getOriginalFilename();
                    String category = result.getDocument() == null
                            ? "通用知识"
                            : nullToDefault(result.getDocument().getKnowledgeCategory(), "通用知识");
                    return "【来源】" + filename
                            + "\n【分类】" + category
                            + "\n【片段】#" + result.getChunk().getChunkIndex()
                            + " " + nullToEmpty(result.getChunk().getTitlePath())
                            + "\n【匹配分】" + result.getScore()
                            + "\n【内容】\n" + result.getChunk().getContent();
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String normalizeScope(String knowledgeCategory) {
        return knowledgeCategory == null || knowledgeCategory.isBlank() ? "全部分类" : knowledgeCategory.trim();
    }
}
