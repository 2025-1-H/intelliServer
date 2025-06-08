package com.example.intelliview.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Table(name = "interview_answer")
public class InterviewAnswer extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "i_id", nullable = false)
    private Interview interview;

    @ManyToOne
    @JoinColumn(name = "q_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "u_id", nullable = false)
    private Member member;

    @Column(columnDefinition = "text")
    private String answer;

    private Integer score;

    @Column(columnDefinition = "text")
    private String feedback;

}
