package com.example.intelliview.dto.archive;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSummaryDto {
    private Long id;
    private String title;
}
