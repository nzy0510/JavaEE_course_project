package com.rjgc.nzy.service;

import com.rjgc.nzy.config.MarkitdownProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MarkdownChunker {

    private static final Pattern HEADING = Pattern.compile("^#{1,6}\\s+.+");
    private static final Pattern QUESTION = Pattern.compile("^.{4,160}[?？]$");

    private final MarkitdownProperties properties;

    public List<MarkdownSection> chunk(String markdown) {
        String normalized = clean(markdown);
        List<MarkdownSection> sections = splitSections(normalized);
        List<MarkdownSection> chunks = new ArrayList<>();
        for (MarkdownSection section : sections) {
            splitLongSection(section, chunks);
        }
        return chunks;
    }

    private String clean(String markdown) {
        String value = markdown == null ? "" : Normalizer.normalize(markdown, Normalizer.Form.NFKC);
        StringBuilder builder = new StringBuilder();
        for (String rawLine : value.replace("\r\n", "\n").replace('\r', '\n').split("\n")) {
            String line = rawLine.strip();
            if (line.isBlank()) {
                builder.append('\n');
                continue;
            }
            if (isNoiseLine(line)) {
                continue;
            }
            builder.append(line).append('\n');
        }
        return builder.toString().replaceAll("\\n{3,}", "\n\n").strip();
    }

    private boolean isNoiseLine(String line) {
        return line.equals("mianshiya.com")
                || line.startsWith("本资源来自面试鸭")
                || line.startsWith("推荐更多免费学编程资源")
                || line.contains("编程导航学习网站")
                || line.contains("企业高频面试题库")
                || line.contains("精选简历模板大全")
                || line.contains("AI 资源导航网站")
                || line.contains("1 对 1 模拟面试");
    }

    private List<MarkdownSection> splitSections(String markdown) {
        List<MarkdownSection> sections = new ArrayList<>();
        String currentTitle = "未命名片段";
        StringBuilder current = new StringBuilder();
        for (String line : markdown.split("\n")) {
            String trimmed = line.strip();
            boolean boundary = HEADING.matcher(trimmed).matches() || QUESTION.matcher(trimmed).matches();
            if (boundary && current.length() > 0) {
                addSection(sections, currentTitle, current.toString());
                current.setLength(0);
            }
            if (boundary) {
                currentTitle = trimmed.replaceFirst("^#{1,6}\\s+", "");
            }
            current.append(line).append('\n');
        }
        addSection(sections, currentTitle, current.toString());
        return sections;
    }

    private void addSection(List<MarkdownSection> sections, String title, String content) {
        String cleaned = content == null ? "" : content.strip();
        if (!cleaned.isBlank()) {
            sections.add(new MarkdownSection(title, cleaned));
        }
    }

    private void splitLongSection(MarkdownSection section, List<MarkdownSection> chunks) {
        String content = section.getContent();
        int target = Math.max(300, properties.getChunkTargetChars());
        int overlap = Math.max(0, Math.min(properties.getChunkOverlapChars(), target / 2));
        if (content.length() <= target) {
            chunks.add(section);
            return;
        }

        int start = 0;
        int part = 1;
        while (start < content.length()) {
            int end = Math.min(content.length(), start + target);
            if (end < content.length()) {
                int paragraphBreak = content.lastIndexOf("\n\n", end);
                if (paragraphBreak > start + target / 2) {
                    end = paragraphBreak;
                }
            }
            String title = section.getTitlePath() + " #" + part;
            chunks.add(new MarkdownSection(title, content.substring(start, end).strip()));
            if (end >= content.length()) {
                break;
            }
            start = Math.max(end - overlap, start + 1);
            part++;
        }
    }
}
