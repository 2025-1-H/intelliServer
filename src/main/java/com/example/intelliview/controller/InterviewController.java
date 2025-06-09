package com.example.intelliview.controller;

import com.example.intelliview.dto.interview.InterviewInfoDto;
import com.example.intelliview.dto.interview.InterviewQuestionsDto.QuestionsResponseDto;
import com.example.intelliview.dto.interview.InterviewReportResponseDto;
import com.example.intelliview.repository.InterviewRepository;
import com.example.intelliview.service.BedrockService;
import com.example.intelliview.service.InterviewService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interview")
public class InterviewController {

    private final InterviewService interviewService;
    private final BedrockService bedrockService;
    private final InterviewRepository interviewRepository;

    @PostMapping("/info")
    public ResponseEntity<Long> getInterviewInfo(@RequestBody InterviewInfoDto interviewInfoDto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return ResponseEntity.ok(interviewService.getInterviewInfo(interviewInfoDto, token));
    }

    @GetMapping("/{id}/start")
    public ResponseEntity<QuestionsResponseDto> startInterview(@PathVariable Long id)
        throws IOException {
        return ResponseEntity.ok(interviewService.startInterview(id));
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<String> analyzeInterviewVideo(@PathVariable Long id,
                                                        @RequestParam("file") MultipartFile videoFile)
            throws JsonProcessingException {
        String feedback = bedrockService.uploadToS3AndAnalyzeInterview(videoFile, id);
        return ResponseEntity.ok(feedback);
    }

    @PostMapping("/{interviewId}/project-questions")
    public ResponseEntity<Void> createProjectQuestions(@PathVariable Long interviewId) throws IOException {
        bedrockService.createProjectQuestions(interviewRepository.findById(interviewId).orElseThrow());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<InterviewReportResponseDto> getInterviewReport(@PathVariable Long id) {
        return ResponseEntity.ok(interviewService.getInterviewReport(id));
    }
}
