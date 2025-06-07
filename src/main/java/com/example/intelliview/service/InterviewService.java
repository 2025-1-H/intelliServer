package com.example.intelliview.service;

import com.example.intelliview.domain.Interview;
import com.example.intelliview.domain.InterviewStatus;
import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.Question;
import com.example.intelliview.domain.QuestionType;
import com.example.intelliview.dto.interview.InterviewInfoDto;
import com.example.intelliview.dto.interview.InterviewQuestionsDto.QuestionsResponseDto;
import com.example.intelliview.dto.interview.InterviewQuestionsDto.QuestionDto;
import com.example.intelliview.repository.InterviewRepository;
import com.example.intelliview.repository.MemberRepository;
import com.example.intelliview.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
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

    public Long getInterviewInfo(InterviewInfoDto interviewInfoDto) {
        Member member = memberRepository.findById(1L).orElseThrow();
        Interview interview = Interview.builder()
            .status(InterviewStatus.SCHEDULED)
            .occupation(interviewInfoDto.getOccupation())
            .githubUsername(member.getUsername())
            .qualification(interviewInfoDto.getQualification())
            .member(member)
            .build();
        interviewRepository.save(interview);
        return interview.getId();
    }

    public QuestionsResponseDto startInterview(Long id) throws IOException {
        Interview interview = interviewRepository.findById(id).orElseThrow();
        interview.updateStatus(InterviewStatus.IN_PROGRESS);
        List<QuestionDto> QuestionDtos = new ArrayList<>();
        ArrayList<String> interviewQuestions = bedrockService.generateInterviewQuestions(interview);
        for (String question : interviewQuestions) {
            QuestionDtos.add(QuestionDto.builder()
                .category(String.valueOf(QuestionType.TECHNICAL))
                .question(question)
                .build());
        }
        for (Question question : questionRepository.findRandomUnsolvedProjectQuestions()) {
            QuestionDtos.add(QuestionDto.builder()
                .category(String.valueOf(QuestionType.PROJECT))
                .question(question.getQuestion())
                .build());
        }
        return QuestionsResponseDto.builder()
            .questions(QuestionDtos)
            .build();
    }
}
