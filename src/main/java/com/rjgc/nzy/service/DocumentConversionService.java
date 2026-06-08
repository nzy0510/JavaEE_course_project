package com.rjgc.nzy.service;

import com.rjgc.nzy.config.MarkitdownProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DocumentConversionService {

    private final MarkitdownProperties properties;

    public ConvertedDocument convert(MultipartFile file, String extension) {
        Path uploadRoot = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
            Path tempFile = uploadRoot.resolve(UUID.randomUUID() + "." + extension.toLowerCase(Locale.ROOT)).normalize();
            if (!tempFile.startsWith(uploadRoot)) {
                throw new RuntimeException("非法上传路径");
            }
            file.transferTo(tempFile);
            try {
                return runMarkitdown(tempFile);
            } finally {
                Files.deleteIfExists(tempFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("文档保存或转换失败: " + e.getMessage(), e);
        }
    }

    private ConvertedDocument runMarkitdown(Path tempFile) {
        ProcessBuilder builder = new ProcessBuilder(
                properties.getPythonPath(),
                "-m",
                "markitdown",
                tempFile.toString());
        builder.redirectErrorStream(true);
        try {
            Instant started = Instant.now();
            Process process = builder.start();
            CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> readOutput(process));
            boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("MarkItDown 转换超时，超过 " + properties.getTimeoutSeconds() + " 秒");
            }
            String output = outputFuture.get();
            if (process.exitValue() != 0) {
                throw new RuntimeException("MarkItDown 转换失败: " + abbreviate(output));
            }
            if (output.length() > properties.getMaxMarkdownChars()) {
                throw new RuntimeException("转换后的 Markdown 超过限制: " + output.length());
            }
            return new ConvertedDocument(output, Duration.between(started, Instant.now()));
        } catch (IOException e) {
            throw new RuntimeException("无法启动 MarkItDown，请检查 MARKITDOWN_PYTHON: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("MarkItDown 转换被中断", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("读取 MarkItDown 输出失败: " + e.getMessage(), e);
        }
    }

    private String readOutput(Process process) {
        try {
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    public record ConvertedDocument(String markdown, Duration timeout) {
    }
}
