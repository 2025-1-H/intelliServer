package com.example.intelliview.repository;

import com.example.intelliview.domain.Member;
import com.example.intelliview.domain.Question;
import com.example.intelliview.domain.QuestionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM question " +
        "WHERE id IN ( " +
        "    SELECT id FROM question " +
        "    WHERE question_type = 'PROJECT' AND is_solved = FALSE " +
        "    ORDER BY RANDOM() " +
        "    LIMIT 3 " +
        ")",
        nativeQuery = true)
    List<Question> findRandomUnsolvedProjectQuestions(@Param("memberId") Long memberId);

}
