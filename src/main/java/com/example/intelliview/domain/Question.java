package com.example.intelliview.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question")
public class Question extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String question;

    @Column(name = "model_answer", columnDefinition = "text")
    private String modelAnswer;

    private String category;
    private Integer difficulty;

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<UserDailyQuestion> userDailyQuestions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<InterviewAnswer> interviewAnswers = new ArrayList<>();
}
