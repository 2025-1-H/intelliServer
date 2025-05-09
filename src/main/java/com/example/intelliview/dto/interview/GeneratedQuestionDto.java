package com.example.intelliview.dto.interview;

import com.example.intelliview.domain.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GeneratedQuestionDto {
    private String question;
    private String modelAnswer;
    private Category category;
    private Integer difficulty;
}
