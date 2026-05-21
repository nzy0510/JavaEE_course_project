package com.rjgc.nzy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiQuestionRequest {

    @NotBlank(message = "问题不能为空")
    private String question;
}
