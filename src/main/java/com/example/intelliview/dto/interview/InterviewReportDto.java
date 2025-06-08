package com.example.intelliview.dto.interview;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewReportDto {
    private Long id;
    private Long interviewId;
    private String content;
    private String videoUrl;

    private LocalDateTime createdAt;
}
