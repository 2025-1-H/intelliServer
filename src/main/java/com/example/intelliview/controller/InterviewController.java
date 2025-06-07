package com.example.intelliview.controller;

import com.example.intelliview.dto.interview.InterviewInfoDto;
import com.example.intelliview.dto.interview.InterviewQuestionsDto.QuestionsResponseDto;
import com.example.intelliview.repository.InterviewRepository;
import com.example.intelliview.service.BedrockService;
import com.example.intelliview.service.InterviewService;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.intelliview.service.BedrockService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interview")
public class InterviewController {

    private final InterviewService interviewService;
    private final BedrockService bedrockService;
    private final InterviewRepository interviewRepository;

    @PostMapping("/info")
    public ResponseEntity<Long> getInterviewInfo(@RequestBody InterviewInfoDto interviewInfoDto) {
        return ResponseEntity.ok(interviewService.getInterviewInfo(interviewInfoDto));
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
}