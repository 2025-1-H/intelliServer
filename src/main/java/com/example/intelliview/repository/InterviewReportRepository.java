package com.example.intelliview.repository;

import com.example.intelliview.domain.InterviewReport;
import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.UserDailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {

    public InterviewReport findByInterviewId(Long interviewId);

    List<InterviewReport> findAllByInterview_MemberAndCreatedAtBetween(Member member, LocalDateTime start, LocalDateTime end);
}
