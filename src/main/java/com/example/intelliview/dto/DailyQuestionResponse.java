package com.example.intelliview.dto;

import com.example.intelliview.domain.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DailyQuestionResponse {
    private Long questionId;
    private String questionText;

    public DailyQuestionResponse(Question question) {
        this.questionId = question.getId();
        this.questionText = question.getQuestion();
    }
}
