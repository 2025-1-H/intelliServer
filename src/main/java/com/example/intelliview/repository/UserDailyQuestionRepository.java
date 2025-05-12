package com.example.intelliview.repository;

import com.example.intelliview.domain.UserDailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserDailyQuestionRepository extends JpaRepository<UserDailyQuestion,Long> {
    Optional<UserDailyQuestion> findByMemberIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT udq.question.id FROM UserDailyQuestion udq WHERE udq.member.id = :userId AND DATE(udq.createdAt) >= :fromDate")
    List<Long> findQuestionIdsAnsweredInLastMonth(@Param("userId") Long userId, @Param("fromDate") LocalDate fromDate);

    @Query("SELECT udq FROM UserDailyQuestion udq WHERE udq.member.id = :userId AND udq.question.id = :questionId ORDER BY udq.createdAt DESC")
    List<UserDailyQuestion> findByMemberIdAndQuestionIdOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("questionId") Long questionId);
}
