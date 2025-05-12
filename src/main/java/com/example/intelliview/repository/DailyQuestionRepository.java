package com.example.intelliview.repository;

import com.example.intelliview.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DailyQuestionRepository extends JpaRepository<Question, Long> {
    //특정 카테고리중 1
    @Query(value = "SELECT * FROM question WHERE category = :category ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomByCategory(@Param("category") String category);

    // 특정 카테고리 + 처음이거나 30일이 지난 질문 중 1개
    @Query(value = "SELECT * FROM question WHERE category = :category AND id NOT IN :excludedIds ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomByCategoryExcluding(@Param("category") String category, @Param("excludedIds") List<Long> excludedIds);

    // 모든 질문 중 1개 선택
    @Query(value = "SELECT * FROM question ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Question> findAnyRandom();

    // 모든 질문 중 처음이거나 푼 지 30일 이상이 된 질문(excludedIds) 중 1개 선택
    @Query(value = "SELECT * FROM question WHERE id NOT IN :excludedIds ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomExcluding(@Param("excludedIds") List<Long> excludedIds);
}