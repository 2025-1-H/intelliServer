package com.example.intelliview.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_daily_question")
public class UserDailyQuestion extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "q_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "u_id", nullable = false)
    private Member member;

    @Column(columnDefinition = "text")
    private String answer;

    @Column(name = "attempt_count")
    private Integer attemptCount;
}