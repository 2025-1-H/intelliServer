package com.example.intelliview.service;

import com.example.intelliview.domain.Interview;
import com.example.intelliview.domain.InterviewStatus;
import com.example.intelliview.domain.Member;
import com.example.intelliview.dto.interview.InterviewInfoDto;
import com.example.intelliview.repository.InterviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterviewService {

    private final InterviewRepository interviewRepository;

    public void getInterviewInfo(InterviewInfoDto interviewInfoDto) {
        Interview interview = Interview.builder()
            .status(InterviewStatus.SCHEDULED)
            .occupation(interviewInfoDto.getOccupation())
            .qualification(interviewInfoDto.getQualification())
            .member(new Member())
            .build();
        interviewRepository.save(interview);
    }
}
