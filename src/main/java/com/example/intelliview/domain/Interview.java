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
@Table(name = "interview")
public class Interview extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InterviewStatus status;

    private String occupation;

    @Column(columnDefinition = "text")
    private String qualification;

    @Builder.Default
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL)
    private List<InterviewAnswer> interviewAnswers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL)
    private List<InterviewReport> interviewReports = new ArrayList<>();
}
