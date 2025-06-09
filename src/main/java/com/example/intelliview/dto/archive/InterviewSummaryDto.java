package com.example.intelliview.dto.archive;

import com.example.intelliview.domain.Interview;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSummaryDto {
    private Long id;

    public static InterviewSummaryDto from(Interview interview) {
        return InterviewSummaryDto.builder()
                .id(interview.getId())
                .build();
    }
}
