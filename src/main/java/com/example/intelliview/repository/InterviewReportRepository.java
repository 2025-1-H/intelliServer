package com.example.intelliview.repository;

import com.example.intelliview.domain.InterviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {

    public InterviewReport findByInterviewId(Long interviewId);
}
