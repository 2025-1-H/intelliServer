package com.example.intelliview.dto.archive;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ArchiveSummaryDto {
    private int totalCount;
    private Double averageScore;
    private Integer maxScore;
    private List<DayArchiveDto> days;
}
