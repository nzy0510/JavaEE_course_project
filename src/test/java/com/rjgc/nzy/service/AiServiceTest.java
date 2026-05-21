package com.rjgc.nzy.service;

import com.rjgc.nzy.entity.KnowledgeAtom;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiServiceTest {

    @Test
    void askReturnsConfigurationHintWhenModelIsUnavailable() {
        KnowledgeService knowledgeService = mock(KnowledgeService.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<OpenAiChatModel> provider = mock(ObjectProvider.class);
        KnowledgeAtom atom = new KnowledgeAtom();
        atom.setSubject("依赖注入");
        atom.setPrinciples("依赖注入由容器负责创建和注入对象依赖。");
        when(knowledgeService.searchForAi(eq("什么是依赖注入？"), eq(5))).thenReturn(List.of(atom));
        when(provider.getIfAvailable()).thenReturn(null);

        AiService service = new AiService(knowledgeService, provider);

        String answer = service.ask("什么是依赖注入？");

        assertThat(answer).contains("DEEPSEEK_API_KEY");
        assertThat(answer).contains("依赖注入由容器负责创建和注入对象依赖");
    }
}
