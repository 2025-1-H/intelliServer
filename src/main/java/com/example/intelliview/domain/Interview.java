package com.example.intelliview.domain;


import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "interview")
public class Interview extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    private String occupation;

    // qualification 컬럼은 text 타입: 상세 내용
    @Column(columnDefinition = "text")
    private String qualification;

    // 한 인터뷰는 여러 인터뷰 답변(InterviewAnswer)을 가질 수 있음
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL)
    private List<InterviewAnswer> interviewAnswers;

    // 한 인터뷰는 여러 인터뷰 리포트(InterviewReport)를 가질 수 있음
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL)
    private List<InterviewReport> interviewReports;
}
