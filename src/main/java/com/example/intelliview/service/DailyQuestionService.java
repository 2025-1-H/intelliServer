package com.example.intelliview.service;

import com.example.intelliview.domain.Category;
import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.Question;
import com.example.intelliview.domain.UserDailyQuestion;
import com.example.intelliview.dto.DailyQuestionResponse;
import com.example.intelliview.repository.DailyQuestionRepository;
import com.example.intelliview.dto.FeedbackResponse;
import com.example.intelliview.repository.MemberRepository;
import com.example.intelliview.repository.UserDailyQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyQuestionService {

    private final UserDailyQuestionRepository userDailyQuestionRepository;
    private final DailyQuestionRepository dailyQuestionRepository;
    private final MemberRepository memberRepository;

    public DailyQuestionResponse getTodayQuestion(Member member){
        //LocalDateTime 포맷을 맞추기 위한 코드
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX); // 23:59:59.999

        // 오늘 이미 생성된 질문이 있는지 확인
        // 있다면 해당 질문을 return 하고 없다면 새로운 질문을 return 한다.
        Optional<UserDailyQuestion> dailyQuestion = userDailyQuestionRepository.findByMemberIdAndCreatedAtBetween(member.getId(), start, end);

        if (dailyQuestion.isPresent()){
            return new DailyQuestionResponse(dailyQuestion.get().getQuestion());
        }
        else{
            // 30일 이내에 답한 질문은 제외한다.
            // 30일 이내에 답한 질문 Id 추출
            LocalDate fromDate = today.minusMonths(1);
            List<Long> questionIdsAnsweredInLastMonth = userDailyQuestionRepository.findQuestionIdsAnsweredInLastMonth(member.getId(), fromDate);

            Question newQuestion;
            Category userCategory = member.getCategory();
            //사용자가 카테고리를 설정했다면 로직 수행. 아니라면 예외처리(카테고리 등록하도록)
            if(userCategory != null) {
                //Exclude 할거 없으면 오류나서 따로 분리
                if (questionIdsAnsweredInLastMonth.isEmpty()) {
                    newQuestion = dailyQuestionRepository.findRandomByCategory(userCategory.name())
                            .orElseThrow(() -> new RuntimeException("선택 가능한 질문이 없습니다"));
                } else {
                    newQuestion = dailyQuestionRepository.findRandomByCategoryExcluding(userCategory.name(),questionIdsAnsweredInLastMonth)
                            .orElseThrow(() -> new RuntimeException("선택 가능한 질문이 없습니다"));
                }
            }else{
                throw new RuntimeException("카테고리를 먼저 등록해주세요.");
            }

            // 이전에 동일한 질문을 풀었던 기록이 있는지 확인 attemptCount 용도
            // repository 없이 할 수도 있을 것 같아서 일단 계속 고민해보기
            List<UserDailyQuestion> previousAttempts = userDailyQuestionRepository.findByMemberIdAndQuestionIdOrderByCreatedAtDesc(
                    member.getId(), newQuestion.getId());

            int attemptCount = 1;
            if (!previousAttempts.isEmpty()) {
                attemptCount = previousAttempts.get(0).getAttemptCount() + 1;
            }

            UserDailyQuestion history = UserDailyQuestion.builder()
                    .member(member)
                    .question(newQuestion)
                    .attemptCount(attemptCount)
                    .build();
            userDailyQuestionRepository.save(history);

            return new DailyQuestionResponse(newQuestion);
        }

    }

    public void submitAnswer(Member member, String answer) {
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.atStartOfDay();
        LocalDateTime end = now.atTime(LocalTime.MAX);

        UserDailyQuestion userDailyQuestion = userDailyQuestionRepository.findByMemberIdAndCreatedAtBetween(member.getId(), start, end)
                .orElseThrow(() -> new RuntimeException("오늘 받은 질문이 없습니다."));

        if (userDailyQuestion.getAnswer() != null){
            throw new RuntimeException("이미 답변한 질문입니다.");
        }

        userDailyQuestion.setAnswer(answer);
        userDailyQuestionRepository.save(userDailyQuestion);
    }

    public FeedbackResponse getFeedback(Member member) {
        LocalDate now = LocalDate.now();
        LocalDateTime start = now.atStartOfDay();
        LocalDateTime end = now.atTime(LocalTime.MAX);

        UserDailyQuestion userDailyQuestion = userDailyQuestionRepository.findByMemberIdAndCreatedAtBetween(member.getId(), start, end)
                .orElseThrow(() -> new RuntimeException("오늘 받은 질문이 없습니다."));

        if(userDailyQuestion.getAnswer() == null){
           throw new RuntimeException("아직 답변하지 않은 질문입니다.");
        }

        Question question = dailyQuestionRepository.findById(userDailyQuestion.getQuestion().getId())
                .orElseThrow(() ->new RuntimeException("질문을 찾을 수 없습니다."));

        return new FeedbackResponse(
                question.getModelAnswer(),
                userDailyQuestion.getAnswer(),
                userDailyQuestion.getAttemptCount()
        );
    }

    public void setUserCategory(Member member, Category category) {
        member.setCategory(category);
        memberRepository.save(member);
    }
}
