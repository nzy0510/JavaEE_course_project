package com.rjgc.nzy.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class KnowledgeCategoryClassifier {

    private static final String DEFAULT_CATEGORY = "通用知识";
    private static final Map<String, List<String>> FILENAME_KEYWORDS = new LinkedHashMap<>();

    static {
        FILENAME_KEYWORDS.put("大模型", List.of("大模型", "大语言模型", "llm", "rag", "gpt", "chatgpt", "deepseek", "transformer", "lora", "prompt"));
        FILENAME_KEYWORDS.put("SpringBoot", List.of("springboot", "spring boot"));
        FILENAME_KEYWORDS.put("Spring", List.of("spring"));
        FILENAME_KEYWORDS.put("MyBatis", List.of("mybatis", "mybatis plus"));
        FILENAME_KEYWORDS.put("MySQL", List.of("mysql"));
        FILENAME_KEYWORDS.put("Redis", List.of("redis"));
        FILENAME_KEYWORDS.put("JVM", List.of("jvm"));
        FILENAME_KEYWORDS.put("并发编程", List.of("并发", "线程池", "多线程"));
        FILENAME_KEYWORDS.put("数据结构与算法", List.of("数据结构", "算法"));
        FILENAME_KEYWORDS.put("计算机网络", List.of("计算机网络", "网络协议"));
        FILENAME_KEYWORDS.put("操作系统", List.of("操作系统"));
        FILENAME_KEYWORDS.put("前端技术", List.of("前端", "javascript", "vue", "react"));
        FILENAME_KEYWORDS.put("软件工程", List.of("软件工程", "课程设计"));
        FILENAME_KEYWORDS.put("Java基础", List.of("java基础", "java", "jdk"));
    }

    public String classify(String filename, String markdown) {
        String source = normalize(filename);
        for (Map.Entry<String, List<String>> entry : FILENAME_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (source.contains(normalize(keyword))) {
                    return entry.getKey();
                }
            }
        }
        return DEFAULT_CATEGORY;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[_\\-]+", " ").trim();
    }
}
