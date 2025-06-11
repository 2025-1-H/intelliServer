package com.example.intelliview.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question")
@Getter
public class Question extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String question;

    @Column(name = "model_answer", columnDefinition = "text")
    private String modelAnswer;

    @Enumerated(EnumType.STRING)
    private Category category;

    private Integer difficulty;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Builder.Default
    private Boolean isSolved = false;


    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<UserDailyQuestion> userDailyQuestions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<InterviewAnswer> interviewAnswers = new ArrayList<>();

    public void setIsSolved(Boolean isSolved) {this.isSolved = isSolved;}

}
