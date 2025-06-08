package com.example.intelliview.dto.archive;

import com.example.intelliview.domain.Question;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionSummaryDto {
    private Long id;
    private String question;
    private String modelAnswer;
    private String category;
    private Integer difficulty;

    public static QuestionSummaryDto from(Question entity) {
        return QuestionSummaryDto.builder()
                .id(entity.getId())
                .question(entity.getQuestion())
                .modelAnswer(entity.getModelAnswer())
                .category(entity.getCategory().name())
                .difficulty(entity.getDifficulty())
                .build();
    }
}
