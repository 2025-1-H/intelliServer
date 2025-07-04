package com.example.intelliview.service;

import com.example.intelliview.domain.Interview;
import com.example.intelliview.domain.InterviewReport;
import com.example.intelliview.domain.InterviewStatus;
import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.Question;
import com.example.intelliview.domain.QuestionType;
import com.example.intelliview.dto.interview.InterviewInfoDto;
import com.example.intelliview.dto.interview.InterviewQuestionsDto.QuestionsResponseDto;
import com.example.intelliview.dto.interview.InterviewQuestionsDto.QuestionDto;
import com.example.intelliview.dto.interview.InterviewReportListDto.InterviewReportTitleDto;
import com.example.intelliview.dto.interview.InterviewReportListDto.InterviewReportTitleListDto;
import com.example.intelliview.dto.interview.InterviewReportResponseDto;
import com.example.intelliview.jwt.JWTUtil;
import com.example.intelliview.repository.InterviewReportRepository;
import com.example.intelliview.repository.InterviewRepository;
import com.example.intelliview.repository.MemberRepository;
import com.example.intelliview.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final BedrockService bedrockService;
    private final MemberRepository memberRepository;
    private final QuestionRepository questionRepository;
    private final InterviewReportRepository interviewReportRepository;
    private final JWTUtil jwtUtil;

    public Long getInterviewInfo(InterviewInfoDto interviewInfoDto, String token) {
        Member member = memberRepository.findByEmail(jwtUtil.getEmail(token));
        Interview interview = Interview.builder()
            .status(InterviewStatus.SCHEDULED)
            .occupation(interviewInfoDto.getOccupation())
            .githubUsername(member.getUsername())
            .qualification(interviewInfoDto.getQualification())
            .member(member)
            .createdAt(LocalDateTime.now())
            .build();
        interviewRepository.save(interview);
        return interview.getId();
    }

    public QuestionsResponseDto startInterview(Long id) throws IOException {
        Interview interview = interviewRepository.findById(id).orElseThrow();
        interview.updateStatus(InterviewStatus.IN_PROGRESS);
        List<QuestionDto> QuestionDtos = new ArrayList<>();
        ArrayList<Question> interviewQuestions = bedrockService.generateInterviewQuestions(interview);
        for (Question question : interviewQuestions) {
            question.setIsSolved(true);
            QuestionDtos.add(QuestionDto.builder()
                .category(String.valueOf(QuestionType.TECHNICAL))
                .question(question.getQuestion())
                .build());
        }
        for (Question question : questionRepository.findRandomUnsolvedProjectQuestions()) {
            question.setIsSolved(true);
            QuestionDtos.add(QuestionDto.builder()
                .category(String.valueOf(QuestionType.PROJECT))
                .question(question.getQuestion())
                .build());
        }
        return QuestionsResponseDto.builder()
            .questions(QuestionDtos)
            .build();
    }

    public InterviewReportResponseDto getInterviewReport(Long id) {
        InterviewReport interviewReport = interviewReportRepository.findByInterviewId(id);
        return InterviewReportResponseDto.builder()
            .videoUrl(interviewReport.getVideoUrl())
            .content(interviewReport.getContent())
            .build();
    }

    public InterviewReportTitleListDto getInterviewReportList(String token) {
        Member member = memberRepository.findByEmail(jwtUtil.getEmail(token));
        List<InterviewReportTitleDto> titleLists = new ArrayList<>();
        List<Interview> interviewList = interviewRepository.findByMember(member);
        for (Interview interview : interviewList) {
            InterviewReport report = interviewReportRepository.findByInterviewId(interview.getId());
            titleLists.add(InterviewReportTitleDto.builder()
                .date(interview.getCreatedAt())
                .occupation(interview.getOccupation())
                .id(report.getId())
                .build());
        }
        return InterviewReportTitleListDto.builder().reports(titleLists).build();
    }
}
