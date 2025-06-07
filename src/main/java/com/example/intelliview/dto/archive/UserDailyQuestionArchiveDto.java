package com.example.intelliview.dto.archive;

import com.example.intelliview.domain.UserDailyQuestion;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDailyQuestionArchiveDto {
    private Long id;
    private String answer;
    private Integer attemptCount;
    private LocalDateTime createdAt;
    private QuestionSummaryDto question;

    public static UserDailyQuestionArchiveDto from(UserDailyQuestion entity) {
        return UserDailyQuestionArchiveDto.builder()
                .id(entity.getId())
                .answer(entity.getAnswer())
                .attemptCount(entity.getAttemptCount())
                .createdAt(entity.getCreatedAt())
                .question(QuestionSummaryDto.from(entity.getQuestion()))
                .build();
    }
}
