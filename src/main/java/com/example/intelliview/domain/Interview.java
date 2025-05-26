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
@Getter
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

    @ManyToOne
    @JoinColumn(name = "u_id", nullable = false)
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL)
    private List<InterviewAnswer> interviewAnswers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL)
    private List<InterviewReport> interviewReports = new ArrayList<>();

    public void updateStatus(InterviewStatus status) {
        this.status = status;
    }
}
