package com.example.intelliview.dto.archive;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DayArchiveDto {
    private LocalDate date;
    private UserDailyQuestionArchiveDto dailyQuestion;
    private List<InterviewReportArchiveDto> interviews;
}
