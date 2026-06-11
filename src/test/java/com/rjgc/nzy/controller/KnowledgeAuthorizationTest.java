package com.rjgc.nzy.controller;

import com.rjgc.nzy.config.AuthInterceptor;
import com.rjgc.nzy.entity.KnowledgeChunk;
import com.rjgc.nzy.entity.KnowledgeDocument;
import com.rjgc.nzy.entity.User;
import com.rjgc.nzy.service.AiService;
import com.rjgc.nzy.service.ChunkSearchResult;
import com.rjgc.nzy.service.KnowledgeDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class KnowledgeAuthorizationTest {

    private final KnowledgeDocumentService knowledgeService = mock(KnowledgeDocumentService.class);
    private final AiService aiService = mock(AiService.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new KnowledgeController(knowledgeService), new AiController(aiService))
            .addInterceptors(new AuthInterceptor())
            .build();

    @Test
    void regularUserCannotImportDocumentAndServiceIsNotCalled() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ai.md",
                "text/markdown",
                "RAG".getBytes(StandardCharsets.UTF_8));

        int status = mvc.perform(multipart("/api/knowledge/documents/import")
                        .file(file)
                        .sessionAttr("user", regularUser()))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isEqualTo(403);
        verifyNoInteractions(knowledgeService);
    }

    @Test
    void regularUserCannotArchiveDocumentAndServiceIsNotCalled() throws Exception {
        int status = mvc.perform(post("/api/knowledge/documents/{id}/archive", 7L)
                        .sessionAttr("user", regularUser()))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isEqualTo(403);
        verifyNoInteractions(knowledgeService);
    }

    @Test
    void regularUserCannotRestoreDocumentAndServiceIsNotCalled() throws Exception {
        int status = mvc.perform(post("/api/knowledge/documents/{id}/restore", 7L)
                        .sessionAttr("user", regularUser()))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isEqualTo(403);
        verifyNoInteractions(knowledgeService);
    }

    @Test
    void regularUserCannotDeleteDocumentAndServiceIsNotCalled() throws Exception {
        int status = mvc.perform(delete("/api/knowledge/documents/{id}", 7L)
                        .sessionAttr("user", regularUser()))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isEqualTo(403);
        verifyNoInteractions(knowledgeService);
    }

    @Test
    void adminCanImportDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ai.md",
                "text/markdown",
                "RAG".getBytes(StandardCharsets.UTF_8));
        KnowledgeDocument document = new KnowledgeDocument();
        document.setOriginalFilename("ai.md");
        when(knowledgeService.importDocument(file)).thenReturn(document);

        int status = mvc.perform(multipart("/api/knowledge/documents/import")
                        .file(file)
                        .sessionAttr("user", adminUser()))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isEqualTo(200);
        verify(knowledgeService).importDocument(file);
    }

    @Test
    void adminCanArchiveRestoreAndDeleteDocument() throws Exception {
        assertThat(mvc.perform(post("/api/knowledge/documents/{id}/archive", 7L)
                        .sessionAttr("user", adminUser()))
                .andReturn()
                .getResponse()
                .getStatus()).isEqualTo(200);
        assertThat(mvc.perform(post("/api/knowledge/documents/{id}/restore", 7L)
                        .sessionAttr("user", adminUser()))
                .andReturn()
                .getResponse()
                .getStatus()).isEqualTo(200);
        assertThat(mvc.perform(delete("/api/knowledge/documents/{id}", 7L)
                        .sessionAttr("user", adminUser()))
                .andReturn()
                .getResponse()
                .getStatus()).isEqualTo(200);

        verify(knowledgeService).archiveDocument(7L);
        verify(knowledgeService).restoreDocument(7L);
        verify(knowledgeService).deleteDocument(7L);
    }

    @Test
    void regularUserCanAskAiQuestion() throws Exception {
        when(aiService.ask(eq("RAG 是什么？"), eq("大模型"))).thenReturn("RAG 答案");

        int status = mvc.perform(post("/api/ai/ask")
                        .contentType("application/json")
                        .content("{\"question\":\"RAG 是什么？\",\"knowledgeCategory\":\"大模型\"}")
                        .sessionAttr("user", regularUser()))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isEqualTo(200);
        verify(aiService).ask("RAG 是什么？", "大模型");
    }

    @Test
    void regularUserCanSearchKnowledgeChunks() throws Exception {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setContent("RAG 包括数据切分、检索召回、生成答案。");
        when(knowledgeService.searchChunks("RAG", 5))
                .thenReturn(List.of(new ChunkSearchResult(chunk, new KnowledgeDocument(), 12)));

        int status = mvc.perform(get("/api/knowledge/chunks/search")
                        .param("keyword", "RAG")
                        .param("limit", "5")
                        .sessionAttr("user", regularUser()))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isEqualTo(200);
        verify(knowledgeService).searchChunks("RAG", 5);
    }

    private User regularUser() {
        return user("alice", "USER");
    }

    private User adminUser() {
        return user("nzy333", "ADMIN");
    }

    private User user(String username, String role) {
        User user = new User();
        user.setUsername(username);
        setRoleWhenIdentityModelExists(user, role);
        return user;
    }

    private void setRoleWhenIdentityModelExists(User user, String role) {
        for (Method method : User.class.getMethods()) {
            if (method.getName().equals("setRole") && method.getParameterCount() == 1) {
                try {
                    method.invoke(user, roleValue(method.getParameterTypes()[0], role));
                    return;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Failed to set test user role", e);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object roleValue(Class<?> targetType, String role) {
        if (targetType.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) targetType.asSubclass(Enum.class), role);
        }
        return role;
    }
}
