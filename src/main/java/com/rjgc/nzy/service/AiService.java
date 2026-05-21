package com.rjgc.nzy.service;

import com.rjgc.nzy.entity.KnowledgeAtom;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private final KnowledgeService knowledgeService;
    private final OpenAiChatModel chatModel;

    public String ask(String question) {
        List<KnowledgeAtom> atoms = knowledgeService.searchForAi(question, 5);

        String context;
        if (atoms.isEmpty()) {
            context = "知识库中暂无相关内容。";
        } else {
            context = atoms.stream()
                    .map(a -> "【" + a.getSubject() + "】" + a.getPrinciples())
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

        return chatModel.generate(prompt);
    }
}
