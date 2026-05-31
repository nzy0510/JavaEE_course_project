package com.rjgc.nzy.service;

import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final KnowledgeService knowledgeService;
    private final ObjectProvider<OpenAiChatModel> chatModelProvider;

    public String ask(String question) {
        List<KnowledgeSearchResult> results = knowledgeService.searchForAiWithScores(question, 5);

        String context;
        if (results.isEmpty()) {
            context = "知识库中暂无相关内容。";
        } else {
            context = results.stream()
                    .map(result -> "【" + result.getAtom().getSubject() + "】"
                            + "（匹配分：" + result.getScore() + "）"
                            + result.getAtom().getPrinciples())
                    .collect(Collectors.joining("\n\n"));
        }

        String prompt = """
                你是一个知识库问答助手。请根据以下知识库内容回答用户的问题。
                如果知识库中没有相关内容，请如实告知用户。

                【知识库内容】
                %s

                【用户问题】
                %s

                请用中文回答，简洁明了。""".formatted(context, question);

        OpenAiChatModel chatModel = chatModelProvider.getIfAvailable();
        if (chatModel == null) {
            return "AI 服务未配置：请设置环境变量 DEEPSEEK_API_KEY 后重启应用。"
                    + "\n\n当前检索到的知识库内容：\n" + context;
        }

        try {
            return chatModel.generate(prompt);
        } catch (RuntimeException e) {
            return "AI 服务调用失败，请检查 DEEPSEEK_API_KEY、网络或模型额度配置。"
                    + "\n\n当前检索到的知识库内容：\n" + context;
        }
    }
}
