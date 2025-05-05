package com.example.intelliview.service;

import com.example.intelliview.domain.Interview;
import com.example.intelliview.domain.InterviewStatus;
import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.Question;
import com.example.intelliview.dto.interview.InterviewInfoDto;
import com.example.intelliview.dto.interview.InterviewQuestionsDto;
import com.example.intelliview.repository.InterviewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
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

    public Long getInterviewInfo(InterviewInfoDto interviewInfoDto) {
        Interview interview = Interview.builder()
            .status(InterviewStatus.SCHEDULED)
            .occupation(interviewInfoDto.getOccupation())
            .qualification(interviewInfoDto.getQualification())
            .member(new Member())
            .build();
        interviewRepository.save(interview);
        return interview.getId();
    }

    public InterviewQuestionsDto startInterview(Long id) throws JsonProcessingException {
        Interview interview = interviewRepository.findById(id).orElseThrow();
        interview.updateStatus(InterviewStatus.IN_PROGRESS);
        return InterviewQuestionsDto.builder()
            .questions(bedrockService.generateInterviewQuestions(interview))
            .build();
    }
}
