package com.example.intelliview.controller;

import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.Question;
import com.example.intelliview.domain.UserDailyQuestion;
import com.example.intelliview.dto.CategoryRequest;
import com.example.intelliview.dto.DailyAnswerRequest;
import com.example.intelliview.dto.DailyQuestionResponse;
import com.example.intelliview.dto.FeedbackResponse;
import com.example.intelliview.dto.archive.ArchiveSummaryDto;
import com.example.intelliview.dto.archive.DayArchiveDto;
import com.example.intelliview.dto.user.CustomUserDetails;
import com.example.intelliview.repository.InterviewRepository;
import com.example.intelliview.repository.MemberRepository;
import com.example.intelliview.service.ArchiveService;
import com.example.intelliview.service.DailyQuestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archive")
public class ArchiveController {

    private final ArchiveService archiveService;
    private final InterviewRepository interviewRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/{year}/{month}")
    public ResponseEntity<ArchiveSummaryDto> getMonthArchive(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable int year, @PathVariable int month) {
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

        System.out.println("👉 [Get] 요청자: " + member.getUsername());

//        List<DayArchiveDto> response = archiveService.getMonthArchive(member, year, month);
        ArchiveSummaryDto response = archiveService.getMonthArchiveSummary(member, year, month);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{year}/{month}/{day}")
    public ResponseEntity<DayArchiveDto> getDayArchive(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day
    ) {
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
        DayArchiveDto response = archiveService.getDayArchive(member, year, month, day);
        return ResponseEntity.ok(response);
    }

}
