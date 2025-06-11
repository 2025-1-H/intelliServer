package com.example.intelliview.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AnalysisAsyncService {
    private final BedrockService bedrockService;

    public AnalysisAsyncService(BedrockService bedrockService) {
        this.bedrockService = bedrockService;
    }

    @Async("customTaskExecutor")
    public void runAsync(MultipartFile videoFile, Long interviewId) {
        try {
            bedrockService.uploadToS3AndAnalyzeInterview(videoFile, interviewId);
        } catch (Exception e) {
            // 실패 로그
            System.err.println("면접 분석 중 오류 발생: " + e.getMessage());
        }
    }
}