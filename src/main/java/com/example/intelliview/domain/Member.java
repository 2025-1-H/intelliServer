package com.example.intelliview.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "member")
@Getter
@Setter
public class Member extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String salt;
    private String role;
    private String githubUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Interview> interviews = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<InterviewAnswer> interviewAnswers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<UserDailyQuestion> userDailyQuestions = new ArrayList<>();

    public void setCategory(Category category) {
        this.category = category;
    }
}
