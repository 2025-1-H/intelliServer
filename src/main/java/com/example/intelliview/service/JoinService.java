package com.example.intelliview.service;

import com.example.intelliview.domain.Member;
import com.example.intelliview.dto.user.JoinDTO;
import com.example.intelliview.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JoinService {

    private final MemberRepository memberRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void joinProcess(JoinDTO joinDTO) {

        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();
        String email = joinDTO.getEmail();

        Boolean isExist = memberRepository.existsByEmail(email);
        if (isExist) {
            throw new RuntimeException("이미 존재하는 회원입니다.");
        }

        Member data = Member.builder()
                .username(username)
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .role("ROLE_ADMIN")
                .build();

        memberRepository.save(data);
    }
}
