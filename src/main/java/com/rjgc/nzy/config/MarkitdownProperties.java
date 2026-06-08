package com.rjgc.nzy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "markitdown")
public class MarkitdownProperties {

    private String pythonPath = "E:/Develop/tools/.venv/Scripts/python.exe";
    private String uploadDir = "target/markitdown-uploads";
    private int timeoutSeconds = 60;
    private int maxMarkdownChars = 300000;
    private int chunkTargetChars = 1000;
    private int chunkOverlapChars = 150;
}
