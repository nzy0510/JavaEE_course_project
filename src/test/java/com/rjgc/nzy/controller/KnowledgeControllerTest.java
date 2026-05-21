package com.rjgc.nzy.controller;

import com.rjgc.nzy.common.Result;
import com.rjgc.nzy.entity.KnowledgeAtom;
import com.rjgc.nzy.service.KnowledgeService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class KnowledgeControllerTest {

    private final KnowledgeService knowledgeService = mock(KnowledgeService.class);
    private final KnowledgeController controller = new KnowledgeController(knowledgeService);

    @Test
    void batchImportAcceptsSingleObjectJson() {
        when(knowledgeService.batchAdd(anyList())).thenReturn(1);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "single.json",
                "application/json",
                """
                {
                  "subject": "依赖注入",
                  "category": "Spring",
                  "difficulty": "中等",
                  "tags": ["Spring", "DI"],
                  "principles": "依赖注入由容器负责创建和注入对象依赖。"
                }
                """.getBytes(StandardCharsets.UTF_8));

        Result<String> result = controller.batchImport(file);

        assertThat(result.getCode()).isEqualTo(200);
        ArgumentCaptor<List<KnowledgeAtom>> captor = ArgumentCaptor.forClass(List.class);
        verify(knowledgeService).batchAdd(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getTags()).isEqualTo("[\"Spring\",\"DI\"]");
    }

    @Test
    void batchImportAcceptsWrappedArrayJson() {
        when(knowledgeService.batchAdd(anyList())).thenReturn(1);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "wrapped.json",
                "application/json",
                """
                {
                  "atoms": [
                    {
                      "subject": "IoC",
                      "category": "Spring",
                      "principles": "控制反转将对象创建交给容器。"
                    }
                  ]
                }
                """.getBytes(StandardCharsets.UTF_8));

        Result<String> result = controller.batchImport(file);

        assertThat(result.getCode()).isEqualTo(200);
        verify(knowledgeService).batchAdd(anyList());
    }
}
