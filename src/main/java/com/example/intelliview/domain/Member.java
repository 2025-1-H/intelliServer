package com.example.intelliview.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
public class Member extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String salt;
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "daily_category")
    private DailyCategory dailyCategory;

    // 한 명의 회원은 여러 인터뷰 답변(InterviewAnswer)을 가질 수 있음
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<InterviewAnswer> interviewAnswers = new ArrayList<>();

    // 한 명의 회원은 여러 인터뷰 답변(InterviewAnswer)을 가질 수 있음
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<UserDailyQuestion> userDailyQuestions = new ArrayList<>();

}
