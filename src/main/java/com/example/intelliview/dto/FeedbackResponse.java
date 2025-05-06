package com.example.intelliview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private String answer;
    private String modelAnswer;
    private Integer attemptCount;
}