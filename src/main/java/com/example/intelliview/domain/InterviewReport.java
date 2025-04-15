package com.example.intelliview.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_report")
public class InterviewReport extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many InterviewReport → One Interview
    @ManyToOne
    @JoinColumn(name = "i_id", nullable = false)
    private Interview interview;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "video_url")
    private String videoUrl;

}