package com.example.intelliview.controller;

import com.example.intelliview.domain.Category;
import com.example.intelliview.domain.Member;
import com.example.intelliview.dto.CategoryRequest;
import com.example.intelliview.dto.DailyAnswerRequest;
import com.example.intelliview.dto.DailyQuestionResponse;
import com.example.intelliview.dto.FeedbackResponse;
import com.example.intelliview.repository.DailyQuestionRepository;
import com.example.intelliview.repository.MemberRepository;
import com.example.intelliview.service.DailyQuestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/daily")
@RequiredArgsConstructor
public class DailyQuestionController {

    private final DailyQuestionService dailyQuestionService;
    private final MemberRepository memberRepository;

    @GetMapping("/question")
    public ResponseEntity<DailyQuestionResponse> getQuestion(@AuthenticationPrincipal Member member, HttpServletRequest request) {
        if (memberRepository.findById(1L).isEmpty()) {
            member = Member.builder()
                    .id(1L)
                    .email("test@gmail.com")
                    .username("test")
                    .build();
        }else{
            member = memberRepository.findById(1L).get();
        }

        System.out.println("👉 [Get] 요청자: " + member.getUsername());

        DailyQuestionResponse response = dailyQuestionService.getTodayQuestion(member);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/question")
    public ResponseEntity<String> submitAnswer(@AuthenticationPrincipal Member member, @RequestBody DailyAnswerRequest request){
        if (memberRepository.findById(1L).isEmpty()) {
            member = Member.builder()
                    .id(1L)
                    .email("test@gmail.com")
                    .username("test")
                    .build();
        }else{
            member = memberRepository.findById(1L).get();
        }
        System.out.println("👉 [Answer] 요청자: " + member.getUsername());
        dailyQuestionService.submitAnswer(member, request.getAnswer());
        return ResponseEntity.ok("답변 성공!");
    }

    @GetMapping("/feddback")
    public ResponseEntity<FeedbackResponse> getFeedback(@AuthenticationPrincipal Member member) {
        if (memberRepository.findById(1L).isEmpty()) {
            member = Member.builder()
                    .id(1L)
                    .email("test@gmail.com")
                    .username("test")
                    .build();
        }else{
            member = memberRepository.findById(1L).get();
        }

        FeedbackResponse response = dailyQuestionService.getFeedback(member);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/category")
    public ResponseEntity<String> setMemberCategory(@AuthenticationPrincipal Member member, @RequestBody CategoryRequest request){
        if (memberRepository.findById(1L).isEmpty()) {
            member = Member.builder()
                    .id(1L)
                    .email("test@gmail.com")
                    .username("test")
                    .build();
        }else{
            member = memberRepository.findById(1L).get();
        }

        dailyQuestionService.setUserCategory(member, request.getCategory());
        return ResponseEntity.ok("카테고리 설정 완료");
    }
}
