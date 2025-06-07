package com.example.intelliview.dto.archive;
import com.example.intelliview.domain.InterviewAnswer;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewAnswerArchiveDto {
    private Long id;
    private String answer;
    private Integer score;
    private String feedback;
    private LocalDateTime createdAt;
    private QuestionSummaryDto question;
    private InterviewSummaryDto interview;

    public static InterviewAnswerArchiveDto from(InterviewAnswer entity) {
        return InterviewAnswerArchiveDto.builder()
                .id(entity.getId())
                .answer(entity.getAnswer())
                .score(entity.getScore())
                .feedback(entity.getFeedback())
                .createdAt(entity.getCreatedAt())
                .question(QuestionSummaryDto.from(entity.getQuestion()))
                // .interview(InterviewSummaryDto.from(entity.getInterview()))
                .build();
    }
}

