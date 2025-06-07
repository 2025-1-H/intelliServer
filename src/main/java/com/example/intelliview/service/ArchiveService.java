package com.example.intelliview.service;

import com.example.intelliview.domain.InterviewAnswer;
import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.Question;
import com.example.intelliview.domain.UserDailyQuestion;
import com.example.intelliview.dto.archive.ArchiveSummaryDto;
import com.example.intelliview.dto.archive.DayArchiveDto;
import com.example.intelliview.dto.archive.InterviewAnswerArchiveDto;
import com.example.intelliview.dto.archive.UserDailyQuestionArchiveDto;
import com.example.intelliview.repository.DailyQuestionRepository;
import com.example.intelliview.repository.MemberRepository;
import com.example.intelliview.repository.UserDailyQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ArchiveService {

    private final UserDailyQuestionRepository userDailyQuestionRepository;

    public ArchiveSummaryDto getMonthArchiveSummary(Member member, int year, int month) {
        List<DayArchiveDto> result = getMonthArchive(member, year, month); // 기존 로직 재활용

        int totalCount = result.stream()
                .mapToInt(day -> {
                    int dq = day.getDailyQuestion() != null ? 1 : 0;
                    int ia = day.getInterviews() != null ? day.getInterviews().size() : 0;
                    return dq + ia;
                })
                .sum();

        List<Integer> allScores = result.stream()
                .flatMap(day -> day.getInterviews().stream())
                .map(InterviewAnswerArchiveDto::getScore)
                .filter(Objects::nonNull)
                .toList();

        Double averageScore = allScores.isEmpty() ? null
                : allScores.stream().mapToInt(Integer::intValue).average().orElse(0);

        Integer maxScore = allScores.isEmpty() ? null
                : allScores.stream().max(Integer::compare).orElse(null);

        return ArchiveSummaryDto.builder()
                .totalCount(totalCount)
                .averageScore(averageScore)
                .maxScore(maxScore)
                .days(result)
                .build();
    }

    public List<DayArchiveDto> getMonthArchive(Member member, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<UserDailyQuestion> dailyQuestions = userDailyQuestionRepository
                .findAllByMemberAndCreatedAtBetweenAndAnswerIsNotNull(
                        member, start.atStartOfDay(), end.atTime(LocalTime.MAX));

        //TODO: 인터뷰 피드백은 추후 개발. 우선은 빈 리스트 할당.
//        List<InterviewAnswer> interviewAnswers = interviewAnswerRepository
//                .findAllByMemberAndCreatedAtBetween(
//                        member, start.atStartOfDay(), end.atTime(LocalTime.MAX));
        List<InterviewAnswer> interviewAnswers = new ArrayList<>();

        Map<LocalDate, UserDailyQuestion> dailyMap = dailyQuestions.stream()
                .collect(Collectors.toMap(
                        dq -> dq.getCreatedAt().toLocalDate(),
                        dq -> dq,
                        (oldVal, newVal) -> newVal
                ));

        Map<LocalDate, List<InterviewAnswer>> interviewMap = interviewAnswers.stream()
                .collect(Collectors.groupingBy(ia -> ia.getCreatedAt().toLocalDate()));

        List<DayArchiveDto> result = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            UserDailyQuestion dq = dailyMap.get(date);
            List<InterviewAnswer> interviews = interviewMap.getOrDefault(date, List.of());

            result.add(DayArchiveDto.builder()
                    .date(date)
                    .dailyQuestion(dq != null ? UserDailyQuestionArchiveDto.from(dq) : null)
                    .interviews(interviews.stream()
                            .map(InterviewAnswerArchiveDto::from)
                            .collect(Collectors.toList()))
                    .build());
        }

        return result;
    }

    public DayArchiveDto getDayArchive(Member member, int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<UserDailyQuestion> dailyQuestions = userDailyQuestionRepository
                .findAllByMemberAndCreatedAtBetweenAndAnswerIsNotNull(member, start, end);

        //TODO: 인터뷰 피드백은 추후 개발. 우선은 빈 리스트 할당.
//        List<InterviewAnswer> interviewAnswers = interviewAnswerRepository
//                .findAllByMemberAndCreatedAtBetween(member, start, end);
        List<InterviewAnswer> interviewAnswers = new ArrayList<>();

        UserDailyQuestionArchiveDto dailyQuestionDto = dailyQuestions.isEmpty()
                ? null
                : UserDailyQuestionArchiveDto.from(dailyQuestions.get(0));

        List<InterviewAnswerArchiveDto> interviewDtos = interviewAnswers.stream()
                .map(InterviewAnswerArchiveDto::from)
                .toList();

        return DayArchiveDto.builder()
                .date(date)
                .dailyQuestion(dailyQuestionDto)
                .interviews(interviewDtos)
                .build();
    }

}
