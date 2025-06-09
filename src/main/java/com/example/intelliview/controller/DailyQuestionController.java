package com.example.intelliview.controller;

import com.example.intelliview.domain.Category;
import com.example.intelliview.domain.Member;
import com.example.intelliview.dto.CategoryRequest;
import com.example.intelliview.dto.DailyAnswerRequest;
import com.example.intelliview.dto.DailyQuestionResponse;
import com.example.intelliview.dto.FeedbackResponse;
import com.example.intelliview.dto.user.CustomUserDetails;
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
    public ResponseEntity<DailyQuestionResponse> getQuestion(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request) {
        Member member = memberRepository.findByEmail(userDetails.getMember().getUsername());
        // 멤버 정보 전체 출력
        System.out.println("==== 인증된 Member 정보 ====");
        if (member != null) {

            System.out.println("ID: " + member.getId());
            System.out.println("Email: " + member.getEmail());
            System.out.println("Username: " + member.getUsername());
            System.out.println("Role: " + member.getRole());
        } else {
            System.out.println("member is null");
        }
        System.out.println("==========================");

//        if (memberRepository.findById(1L).isEmpty()) {
//            member = Member.builder()
//                    .id(1L)
//                    .email("test@gmail.com")
//                    .username("test")
//                    .build();
//        }else{
//            member = memberRepository.findById(1L).get();
//        }

        System.out.println("👉 [Get] 요청자: " + member.getUsername());

        DailyQuestionResponse response = dailyQuestionService.getTodayQuestion(member);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/question")
    public ResponseEntity<String> submitAnswer(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody DailyAnswerRequest request){
        Member member = memberRepository.findByEmail(userDetails.getMember().getUsername());
//        if (memberRepository.findById(1L).isEmpty()) {
//            member = Member.builder()
//                    .id(1L)
//                    .email("test@gmail.com")
//                    .username("test")
//                    .build();
//        }else{
//            member = memberRepository.findById(1L).get();
//        }
        System.out.println("👉 [Answer] 요청자: " + member.getUsername());
        dailyQuestionService.submitAnswer(member, request.getAnswer());
        return ResponseEntity.ok("답변 성공!");
    }

    @GetMapping("/feedback")
    public ResponseEntity<FeedbackResponse> getFeedback(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = memberRepository.findByEmail(userDetails.getMember().getUsername());
//        if (memberRepository.findById(1L).isEmpty()) {
//            member = Member.builder()
//                    .id(1L)
//                    .email("test@gmail.com")
//                    .username("test")
//                    .build();
//        }else{
//            member = memberRepository.findById(1L).get();
//        }

        FeedbackResponse response = dailyQuestionService.getFeedback(member);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/category")
    public ResponseEntity<String> setMemberCategory(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CategoryRequest request){
        Member member = memberRepository.findByEmail(userDetails.getMember().getUsername());
//        if (memberRepository.findById(1L).isEmpty()) {
//            member = Member.builder()
//                    .id(1L)
//                    .email("test@gmail.com")
//                    .username("test")
//                    .build();
//        }else{
//            member = memberRepository.findById(1L).get();
//        }

        dailyQuestionService.setUserCategory(member, request.getCategory());
        return ResponseEntity.ok("카테고리 설정 완료");
    }

    @PatchMapping("/category")
    public ResponseEntity<String> updateMemberCategory(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CategoryRequest request){
        Member member = memberRepository.findByEmail(userDetails.getMember().getUsername());
//        if (memberRepository.findById(1L).isEmpty()) {
//            member = Member.builder()
//                    .id(1L)
//                    .email("test@gmail.com")
//                    .username("test")
//                    .build();
//        }else{
//            member = memberRepository.findById(1L).get();
//        }

        dailyQuestionService.updateUserCategory(member, request.getCategory());
        return ResponseEntity.ok("카테고리 설정 완료");
    }
}
