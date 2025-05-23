package com.example.intelliview.controller;

import com.example.intelliview.domain.Question;
import com.example.intelliview.dto.interview.InterviewInfoDto;
import com.example.intelliview.dto.interview.InterviewQuestionsDto;
import com.example.intelliview.service.InterviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interview")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/info")
    public ResponseEntity<Long> getInterviewInfo(@RequestBody InterviewInfoDto interviewInfoDto) {
        return ResponseEntity.ok(interviewService.getInterviewInfo(interviewInfoDto));
    }

    @GetMapping("/{id}/start")
    public ResponseEntity<InterviewQuestionsDto> startInterview(@PathVariable Long id) throws JsonProcessingException {
        return ResponseEntity.ok(interviewService.startInterview(id));
    }
}
