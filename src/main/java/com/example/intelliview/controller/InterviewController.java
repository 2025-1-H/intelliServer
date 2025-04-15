package com.example.intelliview.controller;

import com.example.intelliview.dto.interview.InterviewInfoDto;
import com.example.intelliview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/interview")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/info")
    public ResponseEntity<String> getInterviewInfo(@RequestBody InterviewInfoDto interviewInfoDto) {
        interviewService.getInterviewInfo(interviewInfoDto);
        return ResponseEntity.ok("정보 입력 완료");
    }
}
