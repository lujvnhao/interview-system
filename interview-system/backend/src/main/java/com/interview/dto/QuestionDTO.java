package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 题目请求 DTO
 */
@Data
public class QuestionDTO {

    @NotBlank(message = "问题内容不能为空")
    private String question;

    private String answer;
    private String category;
    private String tags;
    private Boolean mastered = false;
    private Boolean favorite = false;
    private Integer weight = 10;
}
