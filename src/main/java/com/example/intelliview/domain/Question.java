package com.example.intelliview.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<UserDailyQuestion> userDailyQuestions = new ArrayList<>();

    // Question 하나는 여러 인터뷰 답변(InterviewAnswer)에서 사용될 수 있음
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<InterviewAnswer> interviewAnswers = new ArrayList<>();

    public Question() {}
}
