package com.example.intelliview.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "account_kakao")
public class AccountKakao extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_key", nullable = false)
    private String kakaoKey;

    public AccountKakao() {}
}
