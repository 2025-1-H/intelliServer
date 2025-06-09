package com.example.intelliview.dto.archive;
import com.example.intelliview.domain.InterviewReport;
import com.example.intelliview.service.ArchiveService;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewReportArchiveDto {
    private Long id;
    private String videoUrl;
    private Integer score;
    private String content;
    private LocalDateTime createdAt;
    private InterviewSummaryDto interview;

    public static InterviewReportArchiveDto from(InterviewReport entity) {
        Integer score = ArchiveService.extractTotalScore(entity.getContent());
        return InterviewReportArchiveDto.builder()
                .id(entity.getId())
                .videoUrl(entity.getVideoUrl())
                .content(entity.getContent())
                .score(score)
                .createdAt(entity.getCreatedAt())
                .interview(InterviewSummaryDto.from(entity.getInterview()))
                .build();
    }
}

