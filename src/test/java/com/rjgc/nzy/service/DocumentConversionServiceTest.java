package com.rjgc.nzy.service;

import com.rjgc.nzy.config.MarkitdownProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentConversionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void convertKeepsStdErrWarningsOutOfMarkdownAndKeepsChineseReadable() throws Exception {
        Path fakeMarkitdown = tempDir.resolve("fake-markitdown.cmd");
        String markdownBase64 = Base64.getEncoder()
                .encodeToString("RAG 流程包括检索召回和生成答案。".getBytes(StandardCharsets.UTF_8));
        Files.writeString(fakeMarkitdown, """
                @echo off
                powershell -NoProfile -Command "$s=[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String('%s')); [Console]::OutputEncoding=[Text.Encoding]::UTF8; [Console]::Error.WriteLine('Could not get FontBBox from font descriptor because None cannot be parsed as 4 floats'); [Console]::Out.WriteLine($s)"
                """.formatted(markdownBase64), StandardCharsets.US_ASCII);

        MarkitdownProperties properties = new MarkitdownProperties();
        properties.setPythonPath(fakeMarkitdown.toString());
        properties.setUploadDir(tempDir.resolve("uploads").toString());
        DocumentConversionService service = new DocumentConversionService(properties);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "AI大模型题库.pdf",
                "application/pdf",
                "ignored".getBytes(StandardCharsets.UTF_8));

        DocumentConversionService.ConvertedDocument result = service.convert(file, "pdf");

        assertThat(result.markdown()).contains("RAG 流程包括检索召回和生成答案。");
        assertThat(result.markdown()).doesNotContain("Could not get FontBBox");
        assertThat(result.markdown()).doesNotContain("��");
    }
}
